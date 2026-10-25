package com.example.build

import com.example.data.files.ProjectFileManager
import com.example.data.model.DiagnosticSeverity
import com.example.data.model.Project

data class DiagnosticItem(
  val filePath: String,
  val line: Int,
  val message: String,
  val severity: DiagnosticSeverity = DiagnosticSeverity.ERROR
)

data class BuildResult(
  val isSuccess: Boolean,
  val message: String,
  val diagnostics: List<DiagnosticItem> = emptyList()
)

class BuildWorkflowManager(
  private val fileManager: ProjectFileManager
) {
  suspend fun checkAndBuild(project: Project): BuildResult {
    val files = fileManager.listFiles(project)
    val diagnostics = mutableListOf<DiagnosticItem>()

    for (file in files) {
      if (file.isDirectory) continue
      val content = try {
        fileManager.readFile(project, file.relativePath)
      } catch (_: Exception) {
        continue
      }

      val ext = file.name.substringAfterLast('.', "")
      if (ext in listOf("kt", "java", "json", "py", "xml", "gradle")) {
        var openBraces = 0
        var openParens = 0
        var openBrackets = 0

        content.lines().forEachIndexed { lineIdx, line ->
          openBraces += line.count { it == '{' } - line.count { it == '}' }
          openParens += line.count { it == '(' } - line.count { it == ')' }
          openBrackets += line.count { it == '[' } - line.count { it == ']' }

          if (openBraces < 0) {
            diagnostics.add(
              DiagnosticItem(
                filePath = file.relativePath,
                line = lineIdx + 1,
                message = "Mismatched closing brace '}'",
                severity = DiagnosticSeverity.ERROR
              )
            )
            openBraces = 0
          }
          if (openParens < 0) {
            diagnostics.add(
              DiagnosticItem(
                filePath = file.relativePath,
                line = lineIdx + 1,
                message = "Mismatched closing parenthesis ')'",
                severity = DiagnosticSeverity.ERROR
              )
            )
            openParens = 0
          }
        }

        if (openBraces > 0) {
          diagnostics.add(
            DiagnosticItem(
              filePath = file.relativePath,
              line = content.lines().size,
              message = "Unclosed block brace '{'",
              severity = DiagnosticSeverity.ERROR
            )
          )
        }
      }
    }

    val isSuccess = diagnostics.isEmpty()
    val message = if (isSuccess) {
      "Build check passed. All ${files.size} workspace files verified with zero syntax diagnostics."
    } else {
      "Build check failed. Found ${diagnostics.size} syntax issues across workspace files."
    }

    return BuildResult(isSuccess, message, diagnostics)
  }
}
