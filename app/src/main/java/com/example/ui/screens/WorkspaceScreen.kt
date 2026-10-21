package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgentStep
import com.example.data.model.FileChange
import com.example.data.model.ProjectFile
import com.example.data.model.StepStatus
import com.example.data.model.TaskStatus
import com.example.ui.components.DiffViewerDialog
import com.example.ui.theme.ClaudeBorder
import com.example.ui.theme.ClaudeBorderLight
import com.example.ui.theme.ClaudePureWhite
import com.example.ui.theme.ClaudeSurfaceSubtle
import com.example.ui.theme.ClaudeTerracotta
import com.example.ui.theme.ClaudeTerracottaDark
import com.example.ui.theme.ClaudeTerracottaLight
import com.example.ui.theme.ClaudeTextMuted
import com.example.ui.theme.ClaudeTextPrimary
import com.example.ui.theme.ClaudeTextSecondary
import com.example.ui.theme.ClaudeWarmBg
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
  viewModel: MainViewModel
) {
  val selectedProject by viewModel.selectedProject.collectAsState()
  val openFiles by viewModel.openFiles.collectAsState()
  val activeFile by viewModel.activeFile.collectAsState()
  val activeContent by viewModel.activeFileContent.collectAsState()
  val isDirty by viewModel.isContentDirty.collectAsState()
  val currentTask by viewModel.currentTask.collectAsState()
  val buildResult by viewModel.buildResult.collectAsState()
  val editorPrefs by viewModel.editorPreferences.collectAsState()

  var showFilesSheet by remember { mutableStateOf(false) }
  var showAgentSheet by remember { mutableStateOf(false) }
  var reviewingChange by remember { mutableStateOf<FileChange?>(null) }
  var showNewFileDialog by remember { mutableStateOf(false) }

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scope = rememberCoroutineScope()

  if (selectedProject == null) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(ClaudeWarmBg),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "Loading workspace...",
        style = MaterialTheme.typography.bodyMedium,
        color = ClaudeTextSecondary
      )
    }
    return
  }

  val project = selectedProject!!

  Scaffold(
    containerColor = ClaudePureWhite,
    topBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(ClaudeWarmBg)
          .border(1.dp, ClaudeBorderLight)
      ) {
        // Workspace Top Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Workspace",
              style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
              color = ClaudeTextPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = ClaudePureWhite,
              modifier = Modifier.border(1.dp, ClaudeBorder, RoundedCornerShape(6.dp))
            ) {
              Text(
                text = "${openFiles.size} open",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = FontFamily.Monospace,
                  fontSize = 10.sp,
                  color = ClaudeTextSecondary
                ),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Save button
            IconButton(
              onClick = { viewModel.saveActiveFile() },
              enabled = isDirty,
              modifier = Modifier
                .size(34.dp)
                .testTag("save_file_btn")
            ) {
              Icon(
                Icons.Default.Save,
                contentDescription = "Save file",
                tint = if (isDirty) ClaudeTerracotta else ClaudeTextMuted,
                modifier = Modifier.size(18.dp)
              )
            }

            // Build Check
            IconButton(
              onClick = { viewModel.runBuildCheck() },
              modifier = Modifier
                .size(34.dp)
                .testTag("build_check_btn")
            ) {
              Icon(
                Icons.Default.Build,
                contentDescription = "Verify Syntax",
                tint = ClaudeTextSecondary,
                modifier = Modifier.size(18.dp)
              )
            }

            // File Explorer Toggle
            IconButton(
              onClick = { showFilesSheet = true },
              modifier = Modifier
                .size(34.dp)
                .testTag("open_files_sheet_btn")
            ) {
              Icon(
                Icons.Default.FolderOpen,
                contentDescription = "Project files",
                tint = ClaudeTextSecondary,
                modifier = Modifier.size(18.dp)
              )
            }

            // Agent Drawer Button
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = ClaudeTerracottaLight,
              modifier = Modifier
                .clickable { showAgentSheet = true }
                .testTag("open_agent_sheet_btn")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  Icons.Default.Psychology,
                  contentDescription = "Coding Agent",
                  tint = ClaudeTerracotta,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Agent",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = ClaudeTerracotta
                  )
                )
              }
            }
          }
        }

        // Open Files Minimal Tabs
        if (openFiles.isNotEmpty()) {
          LazyRow(
            modifier = Modifier
              .fillMaxWidth()
              .background(ClaudeWarmBg)
              .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            items(openFiles) { file ->
              val isActive = activeFile?.path == file.path
              Surface(
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                color = if (isActive) ClaudePureWhite else Color.Transparent,
                modifier = Modifier
                  .border(
                    width = 1.dp,
                    color = if (isActive) ClaudeBorder else Color.Transparent,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                  )
                  .clickable { viewModel.openFile(file) }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontFamily = FontFamily.Monospace,
                      fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                      color = if (isActive) ClaudeTextPrimary else ClaudeTextSecondary
                    )
                  )
                  if (isActive && isDirty) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                      modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(ClaudeTerracotta)
                    )
                  }
                  Spacer(modifier = Modifier.width(6.dp))
                  Icon(
                    Icons.Default.Close,
                    contentDescription = "Close file",
                    modifier = Modifier
                      .size(13.dp)
                      .clickable { viewModel.closeFile(file) },
                    tint = ClaudeTextMuted
                  )
                }
              }
            }
          }
        }

        // Diagnostic notification banner
        if (buildResult != null) {
          val res = buildResult!!
          Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (res.isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = res.message,
                style = MaterialTheme.typography.bodySmall,
                color = if (res.isSuccess) Color(0xFF2E7D32) else Color(0xFFC62828),
                maxLines = 1
              )
              if (!res.isSuccess) {
                Text(
                  text = "Ask Agent to Fix",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = ClaudeTerracotta
                  ),
                  modifier = Modifier.clickable {
                    showAgentSheet = true
                    viewModel.startAgentTask("Fix the following build diagnostics:\n${res.diagnostics.joinToString("\n") { it.message }}")
                  }
                )
              }
            }
          }
        }
      }
    }
  ) { padding ->
    // Code Editor Surface
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(ClaudePureWhite)
    ) {
      if (activeFile == null) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Icon(
            Icons.Default.InsertDriveFile,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = ClaudeTextMuted
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No file open",
            style = MaterialTheme.typography.titleMedium,
            color = ClaudeTextPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Select a file from the explorer to view or edit.",
            style = MaterialTheme.typography.bodySmall,
            color = ClaudeTextSecondary
          )
          Spacer(modifier = Modifier.height(16.dp))
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = ClaudeSurfaceSubtle,
            modifier = Modifier
              .clickable { showFilesSheet = true }
              .border(1.dp, ClaudeBorder, RoundedCornerShape(10.dp))
          ) {
            Text(
              text = "Browse Files",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
              color = ClaudeTextPrimary,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
          }
        }
      } else {
        // Line-numbered Monospace Editor
        val scrollState = rememberScrollState()
        val lines = remember(activeContent) { activeContent.lines() }

        Row(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
        ) {
          // Line numbers column (controlled by Editor Settings)
          if (editorPrefs.showLineNumbers) {
            Column(
              modifier = Modifier
                .background(ClaudeWarmBg)
                .border(width = 1.dp, color = ClaudeBorderLight)
                .padding(horizontal = 8.dp, vertical = 8.dp),
              horizontalAlignment = Alignment.End
            ) {
              for (i in 1..lines.size.coerceAtLeast(1)) {
                Text(
                  text = "$i",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = (editorPrefs.fontSizeSp - 1).coerceAtLeast(10).sp,
                    lineHeight = (editorPrefs.fontSizeSp + 7).sp
                  ),
                  color = ClaudeTextMuted
                )
              }
            }
          }

          // Editable code field (configured with Editor Settings font size)
          Box(
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            BasicTextField(
              value = activeContent,
              onValueChange = { viewModel.updateActiveContent(it) },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("code_editor_input"),
              textStyle = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = editorPrefs.fontSizeSp.sp,
                lineHeight = (editorPrefs.fontSizeSp + 7).sp,
                color = ClaudeTextPrimary
              ),
              cursorBrush = SolidColor(ClaudeTerracotta),
              keyboardOptions = KeyboardOptions.Default.copy(
                autoCorrectEnabled = false,
                capitalization = KeyboardCapitalization.None
              )
            )
          }
        }
      }
    }
  }

  // File Explorer Bottom Sheet
  if (showFilesSheet) {
    ModalBottomSheet(
      onDismissRequest = { showFilesSheet = false },
      sheetState = sheetState,
      containerColor = ClaudePureWhite
    ) {
      ClaudeFileExplorerSheet(
        project = project,
        viewModel = viewModel,
        onOpenFile = { file ->
          viewModel.openFile(file)
          showFilesSheet = false
        },
        onCreateNewFile = {
          showNewFileDialog = true
        }
      )
    }
  }

  // AI Agent Panel Bottom Sheet
  if (showAgentSheet) {
    ModalBottomSheet(
      onDismissRequest = { showAgentSheet = false },
      sheetState = sheetState,
      containerColor = ClaudeWarmBg
    ) {
      ClaudeAgentPanelSheet(
        viewModel = viewModel,
        currentTask = currentTask,
        onReviewChange = { change ->
          reviewingChange = change
        }
      )
    }
  }

  // Diff Review Dialog
  if (reviewingChange != null) {
    DiffViewerDialog(
      change = reviewingChange!!,
      onApply = {
        viewModel.applyChange(reviewingChange!!)
        reviewingChange = null
      },
      onRevert = {
        viewModel.revertChange(reviewingChange!!)
        reviewingChange = null
      },
      onDismiss = { reviewingChange = null }
    )
  }

  // Create File Dialog
  if (showNewFileDialog) {
    ClaudeCreateFileDialog(
      onDismiss = { showNewFileDialog = false },
      onCreate = { relPath ->
        scope.launch {
          try {
            val file = viewModel.fileManager.createFile(project, relPath, "")
            val projFile = ProjectFile(
              name = file.name,
              path = file.absolutePath,
              relativePath = relPath,
              isDirectory = false,
              extension = file.extension
            )
            viewModel.openFile(projFile)
            showFilesSheet = false
            showNewFileDialog = false
          } catch (_: Exception) {}
        }
      }
    )
  }
}

@Composable
fun ClaudeFileExplorerSheet(
  project: com.example.data.model.Project,
  viewModel: MainViewModel,
  onOpenFile: (ProjectFile) -> Unit,
  onCreateNewFile: () -> Unit
) {
  var files by remember { mutableStateOf<List<ProjectFile>>(emptyList()) }
  val scope = rememberCoroutineScope()

  LaunchedEffect(project) {
    files = viewModel.fileManager.listFiles(project)
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight(0.70f)
      .padding(20.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Project Files",
        style = MaterialTheme.typography.titleLarge,
        color = ClaudeTextPrimary
      )

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = ClaudePureWhite,
        modifier = Modifier
          .border(1.dp, ClaudeBorder, RoundedCornerShape(8.dp))
          .clickable { onCreateNewFile() }
          .testTag("explorer_new_file_btn")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = ClaudeTerracotta)
          Spacer(modifier = Modifier.width(4.dp))
          Text("New File", style = MaterialTheme.typography.labelMedium, color = ClaudeTextPrimary)
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    LazyColumn(modifier = Modifier.weight(1f)) {
      items(files) { file ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenFile(file) }
            .padding(vertical = 10.dp, horizontal = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
              contentDescription = null,
              tint = if (file.isDirectory) ClaudeTerracotta else ClaudeTextSecondary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = file.name,
              style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
              color = ClaudeTextPrimary
            )
          }

          if (!file.isDirectory) {
            IconButton(
              onClick = {
                scope.launch {
                  viewModel.fileManager.deleteFile(project, file.relativePath)
                  files = viewModel.fileManager.listFiles(project)
                }
              },
              modifier = Modifier.size(24.dp)
            ) {
              Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                modifier = Modifier.size(15.dp),
                tint = ClaudeTextMuted
              )
            }
          }
        }
        Divider(color = ClaudeBorderLight, thickness = 0.5.dp)
      }
    }
  }
}

@Composable
fun ClaudeAgentPanelSheet(
  viewModel: MainViewModel,
  currentTask: com.example.data.model.AgentTask?,
  onReviewChange: (FileChange) -> Unit
) {
  var promptText by remember { mutableStateOf("") }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight(0.85f)
      .padding(20.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Default.Psychology,
          contentDescription = null,
          tint = ClaudeTerracotta,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Coding Agent",
          style = MaterialTheme.typography.titleLarge,
          color = ClaudeTextPrimary
        )
      }

      if (currentTask != null && (
        currentTask.status == TaskStatus.PLANNING ||
        currentTask.status == TaskStatus.INSPECTING ||
        currentTask.status == TaskStatus.EDITING ||
        currentTask.status == TaskStatus.VERIFYING
      )) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = ClaudePureWhite,
          modifier = Modifier
            .border(1.dp, Color(0xFFC62828), RoundedCornerShape(8.dp))
            .clickable { viewModel.cancelAgentTask() }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFC62828))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Stop", style = MaterialTheme.typography.labelSmall, color = Color(0xFFC62828))
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Task Execution Timeline in clean document style
    if (currentTask != null) {
      val task = currentTask
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = ClaudePureWhite,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .border(1.dp, ClaudeBorder, RoundedCornerShape(14.dp))
      ) {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          item {
            Text(
              text = task.userPrompt,
              style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
              color = ClaudeTextPrimary
            )
            Divider(color = ClaudeBorderLight, modifier = Modifier.padding(top = 8.dp))
          }

          // Approval Banner
          val approvalStep = task.steps.firstOrNull { it.status == StepStatus.REQUIRES_APPROVAL }
          if (approvalStep != null) {
            item {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = ClaudeTerracottaLight,
                modifier = Modifier
                  .fillMaxWidth()
                  .border(1.dp, ClaudeTerracotta.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text(
                    text = "Authorization Required",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ClaudeTerracottaDark
                  )
                  Text(
                    text = approvalStep.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = ClaudeTextPrimary
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                      onClick = { viewModel.rejectAgentAction() },
                      modifier = Modifier.testTag("agent_reject_action_btn")
                    ) {
                      Text("Reject")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                      onClick = { viewModel.approveAgentAction() },
                      colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta),
                      modifier = Modifier.testTag("agent_approve_action_btn")
                    ) {
                      Text("Approve & Run")
                    }
                  }
                }
              }
            }
          }

          // Pending changes review button
          if (task.pendingChanges.isNotEmpty()) {
            item {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = ClaudeSurfaceSubtle,
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text(
                    text = "Proposed File Modifications (${task.pendingChanges.size})",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = ClaudeTextPrimary
                  )
                  task.pendingChanges.forEach { change ->
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        text = change.relativePath,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = ClaudeTextPrimary
                      )
                      Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ClaudePureWhite,
                        modifier = Modifier
                          .border(1.dp, ClaudeBorder, RoundedCornerShape(6.dp))
                          .clickable { onReviewChange(change) }
                          .testTag("review_diff_${change.relativePath}")
                      ) {
                        Row(
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Icon(Icons.Default.Difference, contentDescription = null, modifier = Modifier.size(12.dp), tint = ClaudeTerracotta)
                          Spacer(modifier = Modifier.width(4.dp))
                          Text("Review Diff", style = MaterialTheme.typography.labelSmall, color = ClaudeTextPrimary)
                        }
                      }
                    }
                  }
                }
              }
            }
          }

          // Step timeline items
          items(task.steps) { step ->
            ClaudeAgentStepItem(step = step)
          }

          if (task.summary != null) {
            item {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = ClaudeWarmBg,
                modifier = Modifier
                  .fillMaxWidth()
                  .border(1.dp, ClaudeBorder, RoundedCornerShape(8.dp))
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text(
                    text = "Agent Summary",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = ClaudeTerracotta
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = task.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClaudeTextPrimary
                  )
                }
              }
            }
          }
        }
      }
    } else {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Ask the agent to build features, inspect files, or debug code.",
          style = MaterialTheme.typography.bodyMedium,
          color = ClaudeTextSecondary
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Minimal Claude Prompt Bar
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = ClaudePureWhite,
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, ClaudeBorder, RoundedCornerShape(16.dp))
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        BasicTextField(
          value = promptText,
          onValueChange = { promptText = it },
          modifier = Modifier
            .weight(1f)
            .testTag("agent_prompt_input"),
          textStyle = MaterialTheme.typography.bodyMedium.copy(color = ClaudeTextPrimary),
          cursorBrush = SolidColor(ClaudeTerracotta),
          decorationBox = { innerTextField ->
            if (promptText.isEmpty()) {
              Text(
                text = "Ask coding agent...",
                style = MaterialTheme.typography.bodyMedium.copy(color = ClaudeTextMuted)
              )
            }
            innerTextField()
          }
        )

        IconButton(
          onClick = {
            if (promptText.isNotBlank()) {
              viewModel.startAgentTask(promptText.trim())
              promptText = ""
            }
          },
          enabled = promptText.isNotBlank(),
          modifier = Modifier
            .size(32.dp)
            .testTag("agent_send_prompt_btn")
        ) {
          Icon(
            Icons.Default.Send,
            contentDescription = "Send",
            tint = if (promptText.isNotBlank()) ClaudeTerracotta else ClaudeTextMuted,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

@Composable
fun ClaudeAgentStepItem(step: AgentStep) {
  var expanded by remember { mutableStateOf(false) }

  Surface(
    shape = RoundedCornerShape(8.dp),
    color = ClaudeWarmBg,
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, ClaudeBorderLight, RoundedCornerShape(8.dp))
      .clickable { expanded = !expanded }
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = step.title,
          style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
          color = ClaudeTextPrimary
        )

        Text(
          text = step.status.name,
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 9.sp,
            color = when (step.status) {
              StepStatus.COMPLETED -> Color(0xFF2E7D32)
              StepStatus.FAILED -> Color(0xFFC62828)
              StepStatus.REQUIRES_APPROVAL -> ClaudeTerracotta
              else -> ClaudeTextSecondary
            }
          )
        )
      }

      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = step.description,
        style = MaterialTheme.typography.bodySmall,
        color = ClaudeTextSecondary
      )

      if (expanded && step.toolOutput != null) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = step.toolOutput,
          style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = ClaudeTerracottaDark
          )
        )
      }
    }
  }
}

@Composable
fun ClaudeCreateFileDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
  var path by remember { mutableStateOf("") }
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = ClaudePureWhite,
    title = { Text("Create New File", style = MaterialTheme.typography.titleLarge, color = ClaudeTextPrimary) },
    text = {
      OutlinedTextField(
        value = path,
        onValueChange = { path = it },
        label = { Text("Relative File Path") },
        placeholder = { Text("e.g. src/Utils.kt or test.py") },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = ClaudeTerracotta,
          unfocusedBorderColor = ClaudeBorder
        )
      )
    },
    confirmButton = {
      Button(
        onClick = { if (path.isNotBlank()) onCreate(path.trim()) },
        enabled = path.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta)
      ) {
        Text("Create")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = ClaudeTextSecondary) }
    }
  )
}
