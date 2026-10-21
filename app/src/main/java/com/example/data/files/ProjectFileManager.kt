package com.example.data.files

import android.content.Context
import com.example.data.model.Project
import com.example.data.model.ProjectFile
import com.example.data.model.ProjectType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.os.Environment
import java.io.File
import java.io.IOException
import java.util.UUID

class ProjectFileManager(private val context: Context) {

  val internalClaudeDir: File by lazy {
    File(context.filesDir, "Claude code").apply { if (!exists()) mkdirs() }
  }

  val externalClaudeDir: File? by lazy {
    try {
      context.getExternalFilesDir(null)?.let { File(it, "Claude code").apply { if (!exists()) mkdirs() } }
    } catch (_: Exception) {
      null
    }
  }

  private val baseProjectsDir: File by lazy {
    val dir = File(internalClaudeDir, "projects")
    if (!dir.exists()) {
      dir.mkdirs()
      // Migrate legacy projects directory if present
      val legacyDir = File(context.filesDir, "projects")
      if (legacyDir.exists() && legacyDir.isDirectory) {
        legacyDir.listFiles()?.forEach { file ->
          file.copyRecursively(File(dir, file.name), overwrite = true)
        }
      }
    }
    dir
  }

  /**
   * Systematically creates the "Claude code" folder in internal and external storage
   * with standard workspace subdirectories (projects, exports, backups, logs).
   */
  fun initializeClaudeStorageDirectories(): String {
    val logs = mutableListOf<String>()
    try {
      // 1. Internal Storage
      val intProjects = File(internalClaudeDir, "projects").apply { mkdirs() }
      val intExports = File(internalClaudeDir, "exports").apply { mkdirs() }
      val intBackups = File(internalClaudeDir, "backups").apply { mkdirs() }
      val intLogs = File(internalClaudeDir, "logs").apply { mkdirs() }
      File(internalClaudeDir, "README.txt").writeText(
        "Claude Code Mobile Workspace\n" +
        "Internal storage root for projects, exports, and diagnostics.\n" +
        "- projects/ : Active project workspaces\n" +
        "- exports/  : Exported bundles and source archives\n" +
        "- backups/  : Automatic code and preference snapshots\n" +
        "- logs/     : Autonomous agent session and task logs\n"
      )
      logs.add("Internal storage: ${internalClaudeDir.absolutePath}")

      // 2. External App Storage
      externalClaudeDir?.let { extDir ->
        File(extDir, "projects").mkdirs()
        File(extDir, "exports").mkdirs()
        File(extDir, "backups").mkdirs()
        File(extDir, "README.txt").writeText(
          "Claude Code External Storage\n" +
          "User projects and exports systematically mirrored here.\n"
        )
        logs.add("External app storage: ${extDir.absolutePath}")
      }

      // 3. Public Documents Storage (accessible via file manager)
      try {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
          val publicDocs = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Claude code")
          if (publicDocs.exists() || publicDocs.mkdirs()) {
            File(publicDocs, "projects").mkdirs()
            File(publicDocs, "exports").mkdirs()
            File(publicDocs, "README.txt").writeText("Claude Code Public Workspaces\n")
            logs.add("Public Documents: ${publicDocs.absolutePath}")
          }
        }
      } catch (_: Exception) {
        // Handled gracefully on modern scoped storage
      }
    } catch (e: Exception) {
      return "Initialized with warning: ${e.message}"
    }
    return "Initialized: ${logs.joinToString(" | ")}"
  }

  suspend fun listProjects(): List<Project> = withContext(Dispatchers.IO) {
    val dirs = baseProjectsDir.listFiles { file -> file.isDirectory } ?: emptyArray()
    dirs.map { dir ->
      val projectType = detectProjectType(dir)
      val descFile = File(dir, ".droidcode_desc")
      val desc = if (descFile.exists()) descFile.readText() else "Local workspace"
      Project(
        id = dir.name,
        name = dir.name,
        rootPath = dir.absolutePath,
        type = projectType,
        description = desc,
        lastModified = dir.lastModified()
      )
    }.sortedByDescending { it.lastModified }
  }

  suspend fun createProject(name: String, type: ProjectType, description: String = ""): Project =
    withContext(Dispatchers.IO) {
      val sanitizedName = name.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "workspace_${System.currentTimeMillis()}" }
      val projectDir = File(baseProjectsDir, sanitizedName)
      if (projectDir.exists()) {
        throw IllegalArgumentException("A workspace named '$sanitizedName' already exists.")
      }
      projectDir.mkdirs()

      // Save description
      File(projectDir, ".droidcode_desc").writeText(description.ifBlank { "Created with DroidCode" })

      // Seed starter files according to project type
      seedProjectTemplate(projectDir, sanitizedName, type)

      Project(
        id = projectDir.name,
        name = sanitizedName,
        rootPath = projectDir.absolutePath,
        type = type,
        description = description,
        lastModified = projectDir.lastModified()
      )
    }

  suspend fun deleteProject(project: Project): Boolean = withContext(Dispatchers.IO) {
    val dir = File(project.rootPath)
    if (dir.exists() && dir.canonicalPath.startsWith(baseProjectsDir.canonicalPath)) {
      dir.deleteRecursively()
    } else {
      false
    }
  }

  /**
   * Resolves a relative path safely against the project root.
   * Throws SecurityException if a path traversal attempt is detected.
   */
  fun resolveSafeFile(project: Project, relativePath: String): File {
    val cleanRelative = relativePath.trim().removePrefix("/")
    val targetFile = File(File(project.rootPath), cleanRelative)
    val rootCanonical = File(project.rootPath).canonicalPath
    val targetCanonical = targetFile.canonicalPath

    if (!targetCanonical.startsWith(rootCanonical)) {
      throw SecurityException("Path traversal attempt detected: '$relativePath' escapes project root.")
    }
    return targetFile
  }

  suspend fun listFiles(project: Project, relativeDir: String = ""): List<ProjectFile> =
    withContext(Dispatchers.IO) {
      val dir = resolveSafeFile(project, relativeDir)
      if (!dir.exists() || !dir.isDirectory) return@withContext emptyList()

      val children = dir.listFiles() ?: emptyArray()
      children.filter { !it.name.startsWith(".droidcode_backup") }
        .map { file ->
          val relPath = file.relativeTo(File(project.rootPath)).path
          ProjectFile(
            name = file.name,
            path = file.absolutePath,
            relativePath = relPath,
            isDirectory = file.isDirectory,
            sizeBytes = if (file.isFile) file.length() else 0L,
            lastModified = file.lastModified(),
            extension = file.extension.lowercase()
          )
        }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }

  suspend fun readFile(
    project: Project,
    relativePath: String,
    maxLines: Int = 1200,
    offset: Int = 0
  ): String = withContext(Dispatchers.IO) {
    val file = resolveSafeFile(project, relativePath)
    if (!file.exists()) throw IOException("File not found: $relativePath")
    if (file.isDirectory) throw IOException("Cannot read directory as file: $relativePath")

    val lines = file.readLines(Charsets.UTF_8)
    val slice = lines.drop(offset).take(maxLines)
    slice.joinToString("\n")
  }

  suspend fun writeFile(
    project: Project,
    relativePath: String,
    content: String
  ): File = withContext(Dispatchers.IO) {
    val file = resolveSafeFile(project, relativePath)
    file.parentFile?.mkdirs()

    // Create backup if original exists
    if (file.exists()) {
      val backupDir = File(File(project.rootPath), ".droidcode_backup").apply { mkdirs() }
      val backupFile = File(backupDir, "${System.currentTimeMillis()}_${file.name}.bak")
      file.copyTo(backupFile, overwrite = true)
    }

    // Atomic write via temp file
    val tempFile = File.createTempFile("droidcode_write_", ".tmp", file.parentFile)
    tempFile.writeText(content, Charsets.UTF_8)
    if (!tempFile.renameTo(file)) {
      // Fallback
      file.writeText(content, Charsets.UTF_8)
      tempFile.delete()
    }
    file
  }

  suspend fun createFile(
    project: Project,
    relativePath: String,
    initialContent: String = ""
  ): File = withContext(Dispatchers.IO) {
    val file = resolveSafeFile(project, relativePath)
    if (file.exists()) throw IOException("File already exists: $relativePath")
    file.parentFile?.mkdirs()
    file.writeText(initialContent, Charsets.UTF_8)
    file
  }

  suspend fun createDirectory(project: Project, relativeDir: String): File =
    withContext(Dispatchers.IO) {
      val dir = resolveSafeFile(project, relativeDir)
      dir.mkdirs()
      dir
    }

  suspend fun deleteFile(project: Project, relativePath: String): Boolean =
    withContext(Dispatchers.IO) {
      val file = resolveSafeFile(project, relativePath)
      if (file.exists()) {
        if (file.isDirectory) file.deleteRecursively() else file.delete()
      } else {
        false
      }
    }

  suspend fun renameFile(project: Project, oldRelPath: String, newRelPath: String): Boolean =
    withContext(Dispatchers.IO) {
      val oldFile = resolveSafeFile(project, oldRelPath)
      val newFile = resolveSafeFile(project, newRelPath)
      if (!oldFile.exists()) throw IOException("Source file does not exist: $oldRelPath")
      if (newFile.exists()) throw IOException("Target file already exists: $newRelPath")
      newFile.parentFile?.mkdirs()
      oldFile.renameTo(newFile)
    }

  suspend fun searchFiles(
    project: Project,
    query: String,
    isRegex: Boolean = false,
    maxResults: Int = 50
  ): List<SearchResult> = withContext(Dispatchers.IO) {
    val results = mutableListOf<SearchResult>()
    val root = File(project.rootPath)
    val regex = if (isRegex) {
      try { Regex(query) } catch (_: Exception) { Regex(Regex.escape(query)) }
    } else null

    root.walkTopDown()
      .filter { !it.name.startsWith(".") && it.isFile }
      .forEach { file ->
        if (results.size >= maxResults) return@forEach
        val relPath = file.relativeTo(root).path
        try {
          val lines = file.readLines(Charsets.UTF_8)
          lines.forEachIndexed { index, line ->
            if (results.size >= maxResults) return@forEachIndexed
            val matched = if (regex != null) {
              regex.containsMatchIn(line)
            } else {
              line.contains(query, ignoreCase = true)
            }
            if (matched) {
              results.add(
                SearchResult(
                  relativePath = relPath,
                  lineNumber = index + 1,
                  lineContent = line.trim()
                )
              )
            }
          }
        } catch (_: Exception) {
          // Skip binary or unreadable files
        }
      }
    results
  }

  suspend fun getProjectOverview(project: Project): String = withContext(Dispatchers.IO) {
    val root = File(project.rootPath)
    val sb = StringBuilder()
    sb.appendLine("Project: ${project.name} (${project.type.displayName})")
    sb.appendLine("Path: ${project.rootPath}")
    sb.appendLine("Directory Structure:")

    root.walkTopDown()
      .maxDepth(3)
      .filter { !it.name.startsWith(".") }
      .take(40)
      .forEach { file ->
        val depth = file.relativeTo(root).path.count { it == File.separatorChar }
        val indent = "  ".repeat(depth)
        val prefix = if (file.isDirectory) "📁 " else "📄 "
        sb.appendLine("$indent$prefix${file.name}")
      }
    sb.toString()
  }

  private fun detectProjectType(dir: File): ProjectType {
    return when {
      File(dir, "build.gradle.kts").exists() || File(dir, "build.gradle").exists() -> ProjectType.KOTLIN_ANDROID
      File(dir, "requirements.txt").exists() || File(dir, "main.py").exists() -> ProjectType.PYTHON
      File(dir, "package.json").exists() -> ProjectType.NODE_JS
      File(dir, "Cargo.toml").exists() -> ProjectType.RUST
      File(dir, "go.mod").exists() -> ProjectType.GO
      File(dir, "main.sh").exists() -> ProjectType.SHELL
      else -> ProjectType.PLAIN
    }
  }


  private fun seedProjectTemplate(dir: File, name: String, type: ProjectType) {
    when (type) {
      ProjectType.KOTLIN_ANDROID -> {
        File(dir, "README.md").writeText(
          "# $name\n\nAndroid & Kotlin project created with DroidCode on mobile.\n\n## Features\n- Kotlin Compose UI\n- Clean Architecture\n"
        )
        val srcDir = File(dir, "src/main/kotlin/com/example").apply { mkdirs() }
        File(srcDir, "Main.kt").writeText(
          """package com.example

fun main() {
    println("Welcome to $name developed directly on Android with DroidCode!")
}
""".trimIndent()
        )
        File(dir, "build.gradle.kts").writeText(
          """plugins {
    kotlin("jvm") version "2.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
}
""".trimIndent()
        )
      }

      ProjectType.PYTHON -> {
        File(dir, "README.md").writeText(
          "# $name\n\nPython utility created with DroidCode.\n\n## Run\n`python main.py`\n"
        )
        File(dir, "main.py").writeText(
          """import sys

def main():
    print("Running $name from DroidCode mobile workspace!")
    print(f"Python interpreter: {sys.version}")

if __name__ == "__main__":
    main()
""".trimIndent()
        )
        File(dir, "requirements.txt").writeText("# Add Python dependencies here\nrequests>=2.31.0\n")
      }

      ProjectType.NODE_JS -> {
        File(dir, "README.md").writeText("# $name\n\nNode.js project initialized on Android.\n")
        File(dir, "package.json").writeText(
          """{
  "name": "${name.lowercase()}",
  "version": "1.0.0",
  "description": "Created with DroidCode",
  "main": "index.js",
  "scripts": {
    "start": "node index.js"
  }
}
""".trimIndent()
        )
        File(dir, "index.js").writeText(
          """console.log("Hello from $name running via DroidCode on Android!");
""".trimIndent()
        )
      }

      ProjectType.SHELL -> {
        File(dir, "README.md").writeText("# $name\n\nShell automation toolkit.\n")
        File(dir, "main.sh").writeText(
          """#!/bin/sh
echo "Executing $name script on Android..."
echo "Current directory: $(pwd)"
echo "Date: $(date)"
""".trimIndent()
        )
      }

      else -> {
        File(dir, "README.md").writeText(
          "# $name\n\nProject initialized in DroidCode native workspace.\n"
        )
        File(dir, "notes.txt").writeText("Project notes and specifications.\n")
      }
    }
  }
}

data class SearchResult(
  val relativePath: String,
  val lineNumber: Int,
  val lineContent: String
)
