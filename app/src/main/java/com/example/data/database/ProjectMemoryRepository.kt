package com.example.data.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class ProjectMemoryRepository(
  private val projectMemoryDao: ProjectMemoryDao
) {

  fun getMemoriesFlow(projectId: String): Flow<List<ProjectMemoryEntity>> {
    return projectMemoryDao.getMemoriesForProjectFlow(projectId)
  }

  suspend fun getMemories(projectId: String): List<ProjectMemoryEntity> = withContext(Dispatchers.IO) {
    projectMemoryDao.getMemoriesForProject(projectId)
  }

  /**
   * Retrieves memories relevant to the query/prompt using word and tag matching,
   * bounded to top relevant results to avoid context bloating.
   */
  suspend fun getRelevantMemories(projectId: String, query: String, maxLimit: Int = 5): List<ProjectMemoryEntity> = withContext(Dispatchers.IO) {
    val allMemories = projectMemoryDao.getMemoriesForProject(projectId)
    if (allMemories.isEmpty()) return@withContext emptyList()

    val queryTokens = query.lowercase().split(Regex("[\\s,;:.!?]+")).filter { it.length > 2 }.toSet()
    if (queryTokens.isEmpty()) return@withContext allMemories.take(maxLimit)

    val scored = allMemories.map { memory ->
      var score = memory.importance
      val contentLower = memory.content.lowercase()
      val keyLower = memory.key.lowercase()
      val tagsLower = memory.tags.lowercase()

      for (token in queryTokens) {
        if (keyLower.contains(token)) score += 2.0f
        if (tagsLower.contains(token)) score += 1.5f
        if (contentLower.contains(token)) score += 1.0f
      }
      Pair(memory, score)
    }

    val top = scored.sortedByDescending { it.second }
      .take(maxLimit)
      .map { it.first }

    // Update access times
    top.forEach {
      projectMemoryDao.updateAccessTime(it.id)
    }

    top
  }

  suspend fun recordMemory(
    projectId: String,
    category: String,
    key: String,
    content: String,
    importance: Float = 0.5f,
    tags: String = ""
  ) = withContext(Dispatchers.IO) {
    // Redact secrets before saving
    val safeContent = redactSecrets(content)
    val existing = projectMemoryDao.getMemoryByKey(projectId, key)
    val memory = if (existing != null) {
      existing.copy(
        content = safeContent,
        category = category,
        importance = maxOf(existing.importance, importance),
        lastAccessedTimestamp = System.currentTimeMillis(),
        tags = if (tags.isNotBlank()) tags else existing.tags
      )
    } else {
      ProjectMemoryEntity(
        id = UUID.randomUUID().toString(),
        projectId = projectId,
        category = category,
        key = key,
        content = safeContent,
        importance = importance,
        tags = tags
      )
    }
    projectMemoryDao.insertMemory(memory)
  }

  suspend fun deleteMemory(id: String) = withContext(Dispatchers.IO) {
    projectMemoryDao.deleteMemory(id)
  }

  suspend fun clearMemoriesForProject(projectId: String) = withContext(Dispatchers.IO) {
    projectMemoryDao.clearMemoriesForProject(projectId)
  }

  private fun redactSecrets(input: String): String {
    var result = input
    // Redact API keys, tokens, auth headers
    val patterns = listOf(
      Regex("(?i)(?:api[_-]?key|secret|token|password|bearer)\\s*[:=]\\s*['\"]?([a-zA-Z0-9_\\-]{16,})['\"]?"),
      Regex("(sk-[a-zA-Z0-9]{20,})"),
      Regex("(AIza[0-9A-Za-z-_]{35})")
    )
    for (pat in patterns) {
      result = pat.replace(result, "[REDACTED_SECRET]")
    }
    return result
  }
}
