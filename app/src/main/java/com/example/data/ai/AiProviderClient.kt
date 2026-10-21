package com.example.data.ai

import com.example.data.model.ProviderConfig
import com.example.data.model.ProviderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class AiStreamChunk {
  data class ContentDelta(val text: String) : AiStreamChunk()
  data class ToolCallDelta(val toolName: String, val argumentDelta: String) : AiStreamChunk()
  data class Error(val message: String) : AiStreamChunk()
  object Done : AiStreamChunk()
}

data class ChatMessage(
  val role: String, // system, user, assistant, tool
  val content: String,
  val toolCallId: String? = null,
  val toolName: String? = null
)

class AiProviderClient {

  private val client: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(120, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  /**
   * Tests connection to the configured provider and returns response time or error.
   */
  suspend fun testConnection(config: ProviderConfig): Result<String> = withContext(Dispatchers.IO) {
    try {
      val startTime = System.currentTimeMillis()
      val testPrompt = listOf(
        ChatMessage("user", "Respond with 'OK' only.")
      )
      val response = sendNonStreaming(config, testPrompt, maxTokens = 10)
      val elapsed = System.currentTimeMillis() - startTime
      if (response.isSuccess) {
        Result.success("Connection successful! Latency: ${elapsed}ms")
      } else {
        Result.failure(response.exceptionOrNull() ?: Exception("Unknown error"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Fetches the list of available models from the configured endpoint.
   * Supports OpenAI-compatible (/models, /v1/models), Ollama (/api/tags), and Anthropic (/v1/models).
   */
  suspend fun fetchModels(config: ProviderConfig): Result<List<String>> = withContext(Dispatchers.IO) {
    val cleanBaseUrl = config.baseUrl.trim().removeSuffix("/")
    if (cleanBaseUrl.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Endpoint URL cannot be empty"))
    }

    val candidateUrls = mutableListOf<String>()
    if (config.providerType == ProviderType.ANTHROPIC) {
      if (!cleanBaseUrl.endsWith("/models")) candidateUrls.add("$cleanBaseUrl/models")
      else candidateUrls.add(cleanBaseUrl)
      if (!cleanBaseUrl.endsWith("/v1/models")) candidateUrls.add("$cleanBaseUrl/v1/models")
    } else {
      if (cleanBaseUrl.endsWith("/models") || cleanBaseUrl.endsWith("/tags")) {
        candidateUrls.add(cleanBaseUrl)
      } else {
        candidateUrls.add("$cleanBaseUrl/models")
        if (!cleanBaseUrl.endsWith("/v1")) {
          candidateUrls.add("$cleanBaseUrl/v1/models")
        }
        candidateUrls.add("$cleanBaseUrl/api/tags")
      }
    }

    var lastException: Exception? = null
    for (url in candidateUrls) {
      try {
        val requestBuilder = Request.Builder()
          .url(url)
          .get()
          .header("Accept", "application/json")

        if (config.providerType == ProviderType.ANTHROPIC) {
          if (config.apiKey.isNotBlank()) {
            requestBuilder.header("x-api-key", config.apiKey.trim())
          }
          requestBuilder.header("anthropic-version", "2023-06-01")
        } else {
          if (config.apiKey.isNotBlank()) {
            requestBuilder.header("Authorization", "Bearer ${config.apiKey.trim()}")
          }
        }

        val response = client.newCall(requestBuilder.build()).execute()
        if (response.isSuccessful) {
          val responseBody = response.body?.string() ?: ""
          val models = parseModelsList(responseBody)
          if (models.isNotEmpty()) {
            return@withContext Result.success(models)
          } else {
            lastException = Exception("Response from $url contained no models")
          }
        } else {
          val errBody = response.body?.string()?.take(150) ?: ""
          lastException = Exception("HTTP ${response.code} from $url: $errBody")
        }
      } catch (e: Exception) {
        lastException = e
      }
    }

    Result.failure(lastException ?: Exception("Could not fetch models from endpoint"))
  }

  private fun parseModelsList(jsonString: String): List<String> {
    val results = mutableListOf<String>()
    try {
      val trimmed = jsonString.trim()
      if (trimmed.startsWith("[")) {
        val arr = JSONArray(trimmed)
        for (i in 0 until arr.length()) {
          val item = arr.opt(i)
          if (item is String && item.isNotBlank()) {
            results.add(item)
          } else if (item is JSONObject) {
            val id = item.optString("id").ifBlank { item.optString("name") }
            if (id.isNotBlank()) results.add(id)
          }
        }
      } else {
        val root = JSONObject(trimmed)
        // 1. Standard OpenAI / Anthropic format: { "data": [ { "id": "model_id" } ] }
        if (root.has("data")) {
          val dataArr = root.optJSONArray("data")
          if (dataArr != null) {
            for (i in 0 until dataArr.length()) {
              val obj = dataArr.optJSONObject(i)
              if (obj != null) {
                val id = obj.optString("id").ifBlank { obj.optString("name") }
                if (id.isNotBlank()) results.add(id)
              }
            }
          }
        }
        // 2. Ollama / Gemini format: { "models": [ { "name": "llama3.2" } ] }
        if (results.isEmpty() && root.has("models")) {
          val modelsArr = root.optJSONArray("models")
          if (modelsArr != null) {
            for (i in 0 until modelsArr.length()) {
              val obj = modelsArr.optJSONObject(i)
              if (obj != null) {
                val name = obj.optString("name").ifBlank { obj.optString("id") }
                if (name.isNotBlank()) {
                  val cleanName = name.removePrefix("models/")
                  results.add(cleanName)
                }
              }
            }
          }
        }
      }
    } catch (_: Exception) {}
    return results.distinct()
  }

  suspend fun sendNonStreaming(
    config: ProviderConfig,
    messages: List<ChatMessage>,
    maxTokens: Int = config.maxTokens,
    systemPrompt: String? = null
  ): Result<String> = withContext(Dispatchers.IO) {
    try {
      when (config.providerType) {
        ProviderType.ANTHROPIC -> callAnthropic(config, messages, maxTokens, systemPrompt, stream = false)
        else -> callOpenAiCompatible(config, messages, maxTokens, systemPrompt, stream = false)
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun sendStreaming(
    config: ProviderConfig,
    messages: List<ChatMessage>,
    systemPrompt: String? = null
  ): Flow<AiStreamChunk> = flow {
    try {
      when (config.providerType) {
        ProviderType.ANTHROPIC -> streamAnthropic(config, messages, systemPrompt)
        else -> streamOpenAiCompatible(config, messages, systemPrompt)
      }
    } catch (e: Exception) {
      emit(AiStreamChunk.Error(e.message ?: "Stream error occurred"))
    }
  }.flowOn(Dispatchers.IO)

  private suspend fun callOpenAiCompatible(
    config: ProviderConfig,
    messages: List<ChatMessage>,
    maxTokens: Int,
    systemPrompt: String?,
    stream: Boolean
  ): Result<String> = withContext(Dispatchers.IO) {
    val url = if (config.baseUrl.endsWith("/chat/completions")) {
      config.baseUrl
    } else {
      "${config.baseUrl.trimEnd('/')}/chat/completions"
    }

    val jsonBody = JSONObject().apply {
      put("model", config.modelId)
      put("temperature", config.temperature.toDouble())
      put("max_tokens", maxTokens)
      put("stream", stream)

      val msgsArray = JSONArray()
      if (!systemPrompt.isNullOrBlank()) {
        msgsArray.put(JSONObject().apply {
          put("role", "system")
          put("content", systemPrompt)
        })
      }
      for (msg in messages) {
        msgsArray.put(JSONObject().apply {
          put("role", msg.role)
          put("content", msg.content)
        })
      }
      put("messages", msgsArray)
    }

    val requestBuilder = Request.Builder()
      .url(url)
      .post(jsonBody.toString().toRequestBody(jsonMediaType))

    if (config.apiKey.isNotBlank()) {
      requestBuilder.addHeader("Authorization", "Bearer ${config.apiKey.trim()}")
    }

    val response: Response = client.newCall(requestBuilder.build()).execute()
    if (!response.isSuccessful) {
      val errBody = response.body?.string() ?: ""
      return@withContext Result.failure(
        IOException("API Error [HTTP ${response.code}]: $errBody")
      )
    }

    val resText = response.body?.string() ?: ""
    val jsonRes = JSONObject(resText)
    val choices = jsonRes.optJSONArray("choices")
    if (choices != null && choices.length() > 0) {
      val message = choices.getJSONObject(0).optJSONObject("message")
      val content = message?.optString("content") ?: ""
      Result.success(content)
    } else {
      Result.failure(IOException("Empty response from AI provider"))
    }
  }

  private suspend fun kotlinx.coroutines.flow.FlowCollector<AiStreamChunk>.streamOpenAiCompatible(
    config: ProviderConfig,
    messages: List<ChatMessage>,
    systemPrompt: String?
  ) {
    val url = if (config.baseUrl.endsWith("/chat/completions")) {
      config.baseUrl
    } else {
      "${config.baseUrl.trimEnd('/')}/chat/completions"
    }

    val jsonBody = JSONObject().apply {
      put("model", config.modelId)
      put("temperature", config.temperature.toDouble())
      put("max_tokens", config.maxTokens)
      put("stream", true)

      val msgsArray = JSONArray()
      if (!systemPrompt.isNullOrBlank()) {
        msgsArray.put(JSONObject().apply {
          put("role", "system")
          put("content", systemPrompt)
        })
      }
      for (msg in messages) {
        msgsArray.put(JSONObject().apply {
          put("role", msg.role)
          put("content", msg.content)
        })
      }
      put("messages", msgsArray)
    }

    val requestBuilder = Request.Builder()
      .url(url)
      .post(jsonBody.toString().toRequestBody(jsonMediaType))

    if (config.apiKey.isNotBlank()) {
      requestBuilder.addHeader("Authorization", "Bearer ${config.apiKey.trim()}")
    }

    val response = client.newCall(requestBuilder.build()).execute()
    if (!response.isSuccessful) {
      val errBody = response.body?.string() ?: ""
      emit(AiStreamChunk.Error("HTTP ${response.code}: $errBody"))
      return
    }

    val reader = BufferedReader(response.body?.charStream() ?: return)
    var line: String?
    while (reader.readLine().also { line = it } != null) {
      val currentLine = line ?: break
      if (currentLine.startsWith("data: ")) {
        val data = currentLine.removePrefix("data: ").trim()
        if (data == "[DONE]") {
          emit(AiStreamChunk.Done)
          break
        }
        try {
          val chunkJson = JSONObject(data)
          val choices = chunkJson.optJSONArray("choices")
          if (choices != null && choices.length() > 0) {
            val delta = choices.getJSONObject(0).optJSONObject("delta")
            val content = delta?.optString("content")
            if (!content.isNullOrEmpty()) {
              emit(AiStreamChunk.ContentDelta(content))
            }
          }
        } catch (_: Exception) {
          // ignore malformed heartbeat lines
        }
      }
    }
    emit(AiStreamChunk.Done)
  }

  private suspend fun callAnthropic(
    config: ProviderConfig,
    messages: List<ChatMessage>,
    maxTokens: Int,
    systemPrompt: String?,
    stream: Boolean
  ): Result<String> = withContext(Dispatchers.IO) {
    val url = if (config.baseUrl.endsWith("/messages")) {
      config.baseUrl
    } else {
      "${config.baseUrl.trimEnd('/')}/messages"
    }

    val jsonBody = JSONObject().apply {
      put("model", config.modelId)
      put("max_tokens", maxTokens)
      put("stream", stream)
      if (!systemPrompt.isNullOrBlank()) {
        put("system", systemPrompt)
      }

      val msgsArray = JSONArray()
      for (msg in messages) {
        if (msg.role == "system") continue // handled in system param
        msgsArray.put(JSONObject().apply {
          put("role", if (msg.role == "assistant") "assistant" else "user")
          put("content", msg.content)
        })
      }
      put("messages", msgsArray)
    }

    val requestBuilder = Request.Builder()
      .url(url)
      .addHeader("x-api-key", config.apiKey.trim())
      .addHeader("anthropic-version", "2023-06-01")
      .post(jsonBody.toString().toRequestBody(jsonMediaType))

    val response = client.newCall(requestBuilder.build()).execute()
    if (!response.isSuccessful) {
      val err = response.body?.string() ?: ""
      return@withContext Result.failure(IOException("Anthropic API [HTTP ${response.code}]: $err"))
    }

    val resText = response.body?.string() ?: ""
    val json = JSONObject(resText)
    val contentArray = json.optJSONArray("content")
    if (contentArray != null && contentArray.length() > 0) {
      val text = contentArray.getJSONObject(0).optString("text")
      Result.success(text)
    } else {
      Result.failure(IOException("Empty response from Anthropic"))
    }
  }

  private suspend fun kotlinx.coroutines.flow.FlowCollector<AiStreamChunk>.streamAnthropic(
    config: ProviderConfig,
    messages: List<ChatMessage>,
    systemPrompt: String?
  ) {
    val url = if (config.baseUrl.endsWith("/messages")) {
      config.baseUrl
    } else {
      "${config.baseUrl.trimEnd('/')}/messages"
    }

    val jsonBody = JSONObject().apply {
      put("model", config.modelId)
      put("max_tokens", config.maxTokens)
      put("stream", true)
      if (!systemPrompt.isNullOrBlank()) {
        put("system", systemPrompt)
      }

      val msgsArray = JSONArray()
      for (msg in messages) {
        if (msg.role == "system") continue
        msgsArray.put(JSONObject().apply {
          put("role", if (msg.role == "assistant") "assistant" else "user")
          put("content", msg.content)
        })
      }
      put("messages", msgsArray)
    }

    val requestBuilder = Request.Builder()
      .url(url)
      .addHeader("x-api-key", config.apiKey.trim())
      .addHeader("anthropic-version", "2023-06-01")
      .post(jsonBody.toString().toRequestBody(jsonMediaType))

    val response = client.newCall(requestBuilder.build()).execute()
    if (!response.isSuccessful) {
      val err = response.body?.string() ?: ""
      emit(AiStreamChunk.Error("HTTP ${response.code}: $err"))
      return
    }

    val reader = BufferedReader(response.body?.charStream() ?: return)
    var line: String?
    while (reader.readLine().also { line = it } != null) {
      val currentLine = line ?: break
      if (currentLine.startsWith("data: ")) {
        val data = currentLine.removePrefix("data: ").trim()
        try {
          val eventJson = JSONObject(data)
          val type = eventJson.optString("type")
          if (type == "content_block_delta") {
            val delta = eventJson.optJSONObject("delta")
            val text = delta?.optString("text")
            if (!text.isNullOrEmpty()) {
              emit(AiStreamChunk.ContentDelta(text))
            }
          } else if (type == "message_stop") {
            emit(AiStreamChunk.Done)
            break
          }
        } catch (_: Exception) {
          // ignore heartbeat
        }
      }
    }
    emit(AiStreamChunk.Done)
  }
}
