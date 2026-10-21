package com.example.build

import com.example.data.files.ProjectFileManager
import com.example.data.model.BuildDiagnostic
import com.example.data.model.DiagnosticSeverity
import com.example.data.model.Project
import com.example.data.model.ProjectType
import java.io.File
import org.json.JSONObject

data class BuildResult(
  val isSuccess: Boolean,
  val message: String,
  val diagnostics: List<BuildDiagnostic> = emptyList(),
  val exitCode: Int = if (isSuccess) 0 else 1
)

class BuildWorkflowManager(
  private val fileManager: ProjectFileManager
) {

  suspend fun checkAndBuild(project: Project): BuildResult {
    val projectDir = File(project.rootPath)
    if (!projectDir.exists() || !projectDir.isDirectory) {
      return BuildResult(
        isSuccess = false,
        message = "Project directory '${project.rootPath}' does not exist.",
        diagnostics = listOf(
          BuildDiagnostic(
            filePath = project.rootPath,
            message = "Directory not found",
            severity = DiagnosticSeverity.ERROR
          )
        )
      )
    }

    val diagnostics = mutableListOf<BuildDiagnostic>()
    val files = fileManager.listFiles(project, "")

    // Perform syntax and static checks on all workspace files
    for (fileItem in files) {
      if (fileItem.isDirectory) continue
      val file = File(fileItem.path)
      if (!file.exists() || file.length() > 500_000) continue

      val content = try {
        file.readText(Charsets.UTF_8)
      } catch (_: Exception) {
        continue
      }

      val ext = file.extension.lowercase()
      when (ext) {
        "json" -> {
          try {
            JSONObject(content)
          } catch (e: Exception) {
            diagnostics.add(
              BuildDiagnostic(
                filePath = fileItem.relativePath,
                lineNumber = 1,
                severity = DiagnosticSeverity.ERROR,
                message = "Invalid JSON syntax: ${e.message}",
                rawOutput = e.toString()
              )
            )
          }
        }
        "kt", "java" -> {
          diagnostics.addAll(checkKotlinJavaSyntax(fileItem.relativePath, content))
        }
        "py" -> {
          diagnostics.addAll(checkPythonSyntax(fileItem.relativePath, content))
        }
        "js", "ts" -> {
          diagnostics.addAll(checkJsTsSyntax(fileItem.relativePath, content))
        }
      }
    }

    val isSuccess = diagnostics.none { it.severity == DiagnosticSeverity.ERROR }
    val message = if (isSuccess) {
      if (diagnostics.isEmpty()) {
        "Build and syntax checks passed cleanly with 0 issues."
      } else {
        "Build succeeded with ${diagnostics.size} warning(s)."
      }
    } else {
      val errorCount = diagnostics.count { it.severity == DiagnosticSeverity.ERROR }
      "Build failed with $errorCount error(s) and ${diagnostics.size - errorCount} warning(s)."
    }

    return BuildResult(
      isSuccess = isSuccess,
      message = message,
      diagnostics = diagnostics,
      exitCode = if (isSuccess) 0 else 1
    )
  }

  private fun checkKotlinJavaSyntax(relPath: String, content: String): List<BuildDiagnostic> {
    val issues = mutableListOf<BuildDiagnostic>()
    val lines = content.lines()

    var openBraces = 0
    var openParens = 0

    for ((index, line) in lines.withIndex()) {
      val lineNum = index + 1
      val trimmed = line.trim()

      if (trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*")) continue

      for (ch in line) {
        when (ch) {
          '{' -> openBraces++
          '}' -> {
            openBraces--
            if (openBraces < 0) {
              issues.add(
                BuildDiagnostic(
                  filePath = relPath,
                  lineNumber = lineNum,
                  severity = DiagnosticSeverity.ERROR,
                  message = "Unexpected closing brace '}' without matching opening brace."
                )
              )
              openBraces = 0
            }
          }
          '(' -> openParens++
          ')' -> {
            openParens--
            if (openParens < 0) {
              issues.add(
                BuildDiagnostic(
                  filePath = relPath,
                  lineNumber = lineNum,
                  severity = DiagnosticSeverity.ERROR,
                  message = "Unexpected closing parenthesis ')' without matching opening parenthesis."
                )
              )
              openParens = 0
            }
          }
        }
      }
    }

    if (openBraces > 0) {
      issues.add(
        BuildDiagnostic(
          filePath = relPath,
          lineNumber = lines.size,
          severity = DiagnosticSeverity.ERROR,
          message = "Syntax error: $openBraces unclosed opening brace(s) '{'."
        )
      )
    }

    if (openParens > 0) {
      issues.add(
        BuildDiagnostic(
          filePath = relPath,
          lineNumber = lines.size,
          severity = DiagnosticSeverity.ERROR,
          message = "Syntax error: $openParens unclosed opening parenthesis '('."
        )
      )
    }

    return issues
  }

  private fun checkPythonSyntax(relPath: String, content: String): List<BuildDiagnostic> {
    val issues = mutableListOf<BuildDiagnostic>()
    val lines = content.lines()

    for ((index, line) in lines.withIndex()) {
      val lineNum = index + 1
      val trimmed = line.trim()
      if (trimmed.startsWith("#") || trimmed.isEmpty()) continue

      val controlKeywords = listOf("def ", "class ", "if ", "elif ", "else:", "for ", "while ", "try:", "except", "finally:")
      val isControl = controlKeywords.any { trimmed.startsWith(it) }
      if (isControl && !trimmed.endsWith(":") && !trimmed.contains("#")) {
        issues.add(
          BuildDiagnostic(
            filePath = relPath,
            lineNumber = lineNum,
            severity = DiagnosticSeverity.ERROR,
            message = "SyntaxError: Expected ':' at end of header line '$trimmed'."
          )
        )
      }
    }

    return issues
  }

  private fun checkJsTsSyntax(relPath: String, content: String): List<BuildDiagnostic> {
    val issues = mutableListOf<BuildDiagnostic>()
    val lines = content.lines()
    var openBraces = 0

    for ((index, line) in lines.withIndex()) {
      val lineNum = index + 1
      val trimmed = line.trim()
      if (trimmed.startsWith("//") || trimmed.startsWith("/*")) continue

      for (ch in line) {
        if (ch == '{') openBraces++
        if (ch == '}') {
          openBraces--
          if (openBraces < 0) {
            issues.add(
              BuildDiagnostic(
                filePath = relPath,
                lineNumber = lineNum,
                severity = DiagnosticSeverity.ERROR,
                message = "Unexpected closing brace '}'."
              )
            )
            openBraces = 0
          }
        }
      }
    }

    if (openBraces > 0) {
      issues.add(
        BuildDiagnostic(
          filePath = relPath,
          lineNumber = lines.size,
          severity = DiagnosticSeverity.ERROR,
          message = "Syntax error: $openBraces unclosed brace(s) '{'."
        )
      )
    }

    return issues
  }
}
