package com.example.agent

import com.example.build.BuildWorkflowManager
import com.example.data.files.DiffUtil
import com.example.data.files.ProjectFileManager
import com.example.data.model.FileChange
import com.example.data.model.Project
import com.example.marketplace.service.SkillAndPluginManager
import org.json.JSONObject
import java.io.File

sealed class ToolResult {
  data class Success(val output: String, val proposedChange: FileChange? = null) : ToolResult()
  data class Error(val message: String) : ToolResult()
  data class RequiresApproval(
    val explanation: String,
    val toolName: String,
    val args: JSONObject,
    val proposedChange: FileChange? = null
  ) : ToolResult()
}

class AgentToolRegistry(
  private val fileManager: ProjectFileManager,
  private val buildManager: BuildWorkflowManager,
  private val skillManager: SkillAndPluginManager? = null
) {

  fun getSystemPrompt(project: Project): String {
    return """You are Claude Code, an expert autonomous software engineering agent running directly on an Android smartphone.
Your goal is to inspect code, plan solutions, edit source files, run checks, and help developers build real software natively on Android.

CURRENT WORKSPACE:
Name: ${project.name}
Type: ${project.type.displayName}
Root Path: ${project.rootPath}

AVAILABLE TOOLS:
To use a tool, respond with a JSON block in this exact format:
<tool_call>
{
  "name": "tool_name",
  "arguments": {
    "key": "value"
  }
}
</tool_call>

Tools:
1. inspect_workspace - Get an overview of the workspace directory structure and files.
   arguments: {}

2. list_directory - List files and directories in a given folder.
   arguments: {"relative_path": "src/..."}

3. read_file - Read the contents of a file (UTF-8).
   arguments: {"relative_path": "main.py", "max_lines": 1000, "offset": 0}

4. write_file - Write or overwrite a file with full contents. Creates an automatic diff for user review.
   arguments: {"relative_path": "main.py", "content": "..."}

5. apply_patch - Replace a specific unique target block of code with replacement content.
   arguments: {"relative_path": "main.py", "target_content": "old code", "replacement_content": "new code"}

6. search_files - Search for a keyword or regex across the codebase.
   arguments: {"query": "fun main", "is_regex": false}

7. inspect_syntax_error - Check workspace syntax and inspect code diagnostics.
   arguments: {}

8. verify_code - Run syntax verification checks on all workspace files.
   arguments: {}

9. run_plugin_capability - Execute an installed and enabled plugin tool.
   arguments: {"extension_id": "droid-git-pro", "capability_name": "git_atomic_commit", "arguments": {}}

GUIDELINES:
- When asked to write, generate, or improve an AI prompt, output the complete generated prompt inside a fenced block tagged with ```prompt ... ``` so it renders as a dedicated copyable prompt block.
- When writing code, use the appropriate language tag (e.g. ```kotlin, ```python, ```json, ```yaml).
- When providing shell/terminal commands, use ```bash.
- For conversations, questions, and explanations, respond directly in clean markdown with clear paragraphs and lists.
- Only invoke tools if the user explicitly asks to inspect, read, edit, or modify files in the workspace.
- First, inspect or read relevant files to understand context before making edits.
- Never guess file contents; always use read_file or search_files first.
- Only invoke one tool at a time, wait for the tool result, then decide your next step.
- When done, summarize your solution clearly without invoking any further tools.
"""
  }

  suspend fun executeTool(
    name: String,
    args: JSONObject,
    project: Project,
    autoApproveWrites: Boolean,
    autoApproveCommands: Boolean = false
  ): ToolResult {
    return try {
      when (name) {
        "inspect_workspace", "inspect_project" -> {
          val overview = fileManager.getProjectOverview(project)
          ToolResult.Success(overview)
        }

        "list_directory" -> {
          val relPath = args.optString("relative_path", "")
          val files = fileManager.listFiles(project, relPath)
          val formatted = if (files.isEmpty()) {
            "Directory '$relPath' is empty."
          } else {
            files.joinToString("\n") { file ->
              val icon = if (file.isDirectory) "📁" else "📄"
              val size = if (file.isDirectory) "" else " (${file.sizeBytes} bytes)"
              "$icon ${file.name}$size"
            }
          }
          ToolResult.Success(formatted)
        }

        "read_file" -> {
          val relPath = args.optString("relative_path")
          if (relPath.isBlank()) return ToolResult.Error("relative_path is required.")
          val maxLines = args.optInt("max_lines", 1200)
          val offset = args.optInt("offset", 0)
          val content = fileManager.readFile(project, relPath, maxLines, offset)
          ToolResult.Success(content)
        }

        "write_file" -> {
          val relPath = args.optString("relative_path")
          val newContent = args.optString("content")
          if (relPath.isBlank()) return ToolResult.Error("relative_path is required.")

          val file = fileManager.resolveSafeFile(project, relPath)
          val origContent = if (file.exists()) file.readText(Charsets.UTF_8) else ""
          val hunks = DiffUtil.computeDiff(origContent, newContent)
          val change = FileChange(
            filePath = file.absolutePath,
            relativePath = relPath,
            originalContent = origContent,
            proposedContent = newContent,
            diffHunks = hunks,
            isApproved = autoApproveWrites
          )

          if (!autoApproveWrites) {
            ToolResult.RequiresApproval(
              explanation = "Proposed write to '$relPath' (${hunks.size} diff hunks).",
              toolName = "write_file",
              args = args,
              proposedChange = change
            )
          } else {
            fileManager.writeFile(project, relPath, newContent)
            ToolResult.Success("File written successfully: $relPath", change.copy(isApplied = true))
          }
        }

        "apply_patch" -> {
          val relPath = args.optString("relative_path")
          val targetContent = args.optString("target_content")
          val replacementContent = args.optString("replacement_content")
          if (relPath.isBlank() || targetContent.isEmpty()) {
            return ToolResult.Error("relative_path and target_content are required.")
          }

          val file = fileManager.resolveSafeFile(project, relPath)
          if (!file.exists()) {
            return ToolResult.Error("File does not exist: $relPath")
          }

          val origContent = file.readText(Charsets.UTF_8)
          val patchResult = DiffUtil.applyBlockReplacement(origContent, targetContent, replacementContent)
          if (patchResult.isFailure) {
            return ToolResult.Error("Patch failed: ${patchResult.exceptionOrNull()?.message}")
          }

          val newContent = patchResult.getOrThrow()
          val hunks = DiffUtil.computeDiff(origContent, newContent)
          val change = FileChange(
            filePath = file.absolutePath,
            relativePath = relPath,
            originalContent = origContent,
            proposedContent = newContent,
            diffHunks = hunks,
            isApproved = autoApproveWrites
          )

          if (!autoApproveWrites) {
            ToolResult.RequiresApproval(
              explanation = "Proposed patch on '$relPath'",
              toolName = "apply_patch",
              args = args,
              proposedChange = change
            )
          } else {
            fileManager.writeFile(project, relPath, newContent)
            ToolResult.Success("Patch applied successfully to $relPath", change.copy(isApplied = true))
          }
        }

        "search_files" -> {
          val query = args.optString("query")
          val isRegex = args.optBoolean("is_regex", false)
          if (query.isBlank()) return ToolResult.Error("query parameter is required.")
          val results = fileManager.searchFiles(project, query, isRegex)
          if (results.isEmpty()) {
            ToolResult.Success("No matches found for '$query'.")
          } else {
            val formatted = results.joinToString("\n") {
              "${it.relativePath}:${it.lineNumber}: ${it.lineContent}"
            }
            ToolResult.Success("Found ${results.size} matches:\n$formatted")
          }
        }

        "inspect_syntax_error", "inspect_build_error" -> {
          val result = buildManager.checkAndBuild(project)
          val text = buildString {
            appendLine("Status: ${if (result.isSuccess) "PASSED" else "FAILED"}")
            appendLine(result.message)
            if (result.diagnostics.isNotEmpty()) {
              appendLine("\nDiagnostics:")
              result.diagnostics.forEach { diag ->
                val loc = if (diag.line > 0) " (${diag.filePath}:${diag.line})" else ""
                appendLine("[${diag.severity}] $loc ${diag.message}")
              }
            }
          }
          ToolResult.Success(text)
        }

        "verify_code", "run_tests" -> {
          val result = buildManager.checkAndBuild(project)
          ToolResult.Success("Verification check result: ${result.message}\nIssues found: ${result.diagnostics.size}")
        }

        "run_plugin_capability" -> {
          val extId = args.optString("extension_id")
          val capName = args.optString("capability_name")
          val capArgs = args.optJSONObject("arguments") ?: JSONObject()
          if (skillManager != null) {
            skillManager.executePluginCapability(extId, capName, capArgs, project)
          } else {
            ToolResult.Error("Plugin runtime is not active.")
          }
        }

        else -> {
          if (skillManager != null) {
            skillManager.executePluginCapability(name, name, args, project)
          } else {
            ToolResult.Error("Unknown tool: $name")
          }
        }
      }
    } catch (e: Exception) {
      ToolResult.Error("Tool execution error: ${e.message}")
    }
  }
}
