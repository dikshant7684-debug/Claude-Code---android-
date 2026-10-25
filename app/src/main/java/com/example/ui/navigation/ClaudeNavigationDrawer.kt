package com.example.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ConversationSession

/**
 * Premium, minimal Claude-inspired Navigation Drawer for Android.
 * Features warm off-white (light) / charcoal (dark) palettes,
 * Chat/Code mode switcher, organized Starred & Recent chats, and bottom account section.
 */
@Composable
fun ClaudeNavigationDrawer(
  isDark: Boolean,
  isCodeMode: Boolean,
  onModeToggle: (Boolean) -> Unit,
  onCloseDrawer: () -> Unit,
  onNewChat: () -> Unit,
  currentConversationId: String?,
  conversations: List<ConversationSession>,
  onSelectConversation: (ConversationSession) -> Unit,
  onToggleStar: (String) -> Unit,
  onDeleteConversation: (String) -> Unit,
  onNavigateToSettings: () -> Unit,
  onNavigateToProjects: () -> Unit,
  onNavigateToAskAi: () -> Unit,
  onNavigateToArtifacts: () -> Unit,
  onToggleTheme: () -> Unit,
  onClearHistory: () -> Unit,
  modifier: Modifier = Modifier
) {
  val drawerBg = if (isDark) Color(0xFF191919) else Color(0xFFF7F6F0)
  val surfaceBg = if (isDark) Color(0xFF242424) else Color(0xFFFFFFFF)
  val borderCol = if (isDark) Color(0xFF333333) else Color(0xFFE8E6DE)
  val textPrimary = if (isDark) Color(0xFFF2F2F2) else Color(0xFF252525)
  val textSecondary = if (isDark) Color(0xFFA0A0A0) else Color(0xFF77766F)
  val textMuted = if (isDark) Color(0xFF6E6E6E) else Color(0xFFA5A39B)
  val accentTerracotta = Color(0xFFD95B26)
  val accentContainer = if (isDark) Color(0xFF3B2618) else Color(0xFFFFE0D1)

  val starredList = remember(conversations) { conversations.filter { it.isStarred } }
  val recentList = remember(conversations) { conversations.filter { !it.isStarred }.sortedByDescending { it.lastUpdated } }

  Surface(
    modifier = modifier
      .fillMaxHeight()
      .fillMaxWidth(0.85f)
      .widthIn(max = 340.dp)
      .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
      .testTag("claude_nav_drawer"),
    color = drawerBg,
    shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
    shadowElevation = 8.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.statusBars)
    ) {
      // 1. TOP HEADER (Branding, New Chat '+', Close button)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Application Logo
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(surfaceBg)
              .border(1.dp, borderCol, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Image(
              painter = painterResource(id = R.drawable.cat_logo),
              contentDescription = "Claude Code Logo",
              modifier = Modifier.size(24.dp).clip(CircleShape)
            )
          }

          Text(
            text = "Claude Code",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp,
              fontFamily = FontFamily.SansSerif
            ),
            color = textPrimary
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Circular '+' New Chat Button
          Surface(
            shape = CircleShape,
            color = surfaceBg,
            border = BorderStroke(1.dp, borderCol),
            modifier = Modifier
              .size(32.dp)
              .clickable {
                onNewChat()
                onCloseDrawer()
              }
              .testTag("drawer_new_chat_btn")
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                Icons.Default.Add,
                contentDescription = "New Chat",
                tint = accentTerracotta,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          // Close Drawer Button
          IconButton(
            onClick = onCloseDrawer,
            modifier = Modifier
              .size(32.dp)
              .testTag("drawer_close_btn")
          ) {
            Icon(
              Icons.Default.Close,
              contentDescription = "Close Drawer",
              tint = textSecondary,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      // 2. SEGMENTED CHAT / CODE MODE SWITCH
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = surfaceBg,
        border = BorderStroke(1.dp, borderCol),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp)
        ) {
          // Chat Mode Segment
          val chatBg by animateColorAsState(
            targetValue = if (!isCodeMode) accentContainer else Color.Transparent,
            animationSpec = tween(200),
            label = "chat_bg"
          )
          val chatTextColor = if (!isCodeMode) accentTerracotta else textSecondary

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(chatBg)
              .clickable { onModeToggle(false) }
              .padding(vertical = 7.dp)
              .testTag("drawer_mode_chat"),
            contentAlignment = Alignment.Center
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                Icons.Default.ChatBubbleOutline,
                contentDescription = null,
                tint = chatTextColor,
                modifier = Modifier.size(15.dp)
              )
              Text(
                text = "Chat",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = if (!isCodeMode) FontWeight.Bold else FontWeight.Medium,
                  fontSize = 13.sp
                ),
                color = chatTextColor
              )
            }
          }

          // Code Mode Segment
          val codeBg by animateColorAsState(
            targetValue = if (isCodeMode) accentContainer else Color.Transparent,
            animationSpec = tween(200),
            label = "code_bg"
          )
          val codeTextColor = if (isCodeMode) accentTerracotta else textSecondary

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(codeBg)
              .clickable { onModeToggle(true) }
              .padding(vertical = 7.dp)
              .testTag("drawer_mode_code"),
            contentAlignment = Alignment.Center
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                Icons.Default.Code,
                contentDescription = null,
                tint = codeTextColor,
                modifier = Modifier.size(15.dp)
              )
              Text(
                text = "Code",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = if (isCodeMode) FontWeight.Bold else FontWeight.Medium,
                  fontSize = 13.sp
                ),
                color = codeTextColor
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      Divider(color = borderCol, thickness = 1.dp)

      // 3. SCROLLABLE CONTENT (Primary Nav, Starred, Recents)
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        // PRIMARY NAVIGATION (Order: Chats, Projects, Ask AI, Artifacts)
        item {
          DrawerNavItem(
            title = "Chats",
            icon = Icons.Default.ChatBubbleOutline,
            isSelected = !isCodeMode,
            textColor = textPrimary,
            selectedColor = accentContainer,
            accentColor = accentTerracotta,
            onClick = {
              onModeToggle(false)
              onCloseDrawer()
            }
          )
        }

        item {
          DrawerNavItem(
            title = "Projects",
            icon = Icons.Default.FolderOpen,
            isSelected = false,
            textColor = textPrimary,
            selectedColor = accentContainer,
            accentColor = accentTerracotta,
            onClick = {
              onNavigateToProjects()
              onCloseDrawer()
            }
          )
        }

        item {
          DrawerNavItem(
            title = "Ask AI",
            icon = Icons.Default.AutoAwesome,
            isSelected = false,
            textColor = textPrimary,
            selectedColor = accentContainer,
            accentColor = accentTerracotta,
            onClick = {
              onNavigateToAskAi()
              onCloseDrawer()
            }
          )
        }

        item {
          DrawerNavItem(
            title = "Artifacts",
            icon = Icons.Default.Widgets,
            isSelected = false,
            textColor = textPrimary,
            selectedColor = accentContainer,
            accentColor = accentTerracotta,
            onClick = {
              onNavigateToArtifacts()
              onCloseDrawer()
            }
          )
        }

        // 4. STARRED SECTION (Above Recents)
        if (starredList.isNotEmpty()) {
          item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(13.dp)
              )
              Text(
                text = "STARRED",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  letterSpacing = 0.6.sp
                ),
                color = textMuted
              )
            }
          }

          items(starredList, key = { "starred_${it.id}" }) { conv ->
            ConversationRow(
              conversation = conv,
              isActive = conv.id == currentConversationId,
              textPrimary = textPrimary,
              textSecondary = textSecondary,
              surfaceBg = surfaceBg,
              accentContainer = accentContainer,
              accentColor = accentTerracotta,
              onSelect = {
                onSelectConversation(conv)
                onCloseDrawer()
              },
              onToggleStar = { onToggleStar(conv.id) },
              onDelete = { onDeleteConversation(conv.id) }
            )
          }
        }

        // 5. RECENT CONVERSATIONS SECTION
        item {
          Spacer(modifier = Modifier.height(14.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "RECENTS",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.6.sp
              ),
              color = textMuted
            )
            Text(
              text = "${recentList.size}",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
              color = textMuted
            )
          }
        }

        if (recentList.isEmpty() && starredList.isEmpty()) {
          item {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color.Transparent,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp)
            ) {
              Text(
                text = "No recent chats yet. Tap '+' above to begin.",
                style = MaterialTheme.typography.bodySmall,
                color = textMuted
              )
            }
          }
        } else {
          items(recentList, key = { "recent_${it.id}" }) { conv ->
            ConversationRow(
              conversation = conv,
              isActive = conv.id == currentConversationId,
              textPrimary = textPrimary,
              textSecondary = textSecondary,
              surfaceBg = surfaceBg,
              accentContainer = accentContainer,
              accentColor = accentTerracotta,
              onSelect = {
                onSelectConversation(conv)
                onCloseDrawer()
              },
              onToggleStar = { onToggleStar(conv.id) },
              onDelete = { onDeleteConversation(conv.id) }
            )
          }
        }
      }

      // 6. BOTTOM PROFILE & ACCOUNT SECTION (Fixed)
      Divider(color = borderCol, thickness = 1.dp)

      BottomAccountSection(
        isDark = isDark,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        surfaceBg = surfaceBg,
        borderCol = borderCol,
        accentTerracotta = accentTerracotta,
        onNavigateToSettings = {
          onNavigateToSettings()
          onCloseDrawer()
        },
        onNavigateToWorkspace = {
          onModeToggle(true)
          onCloseDrawer()
        },
        onToggleTheme = onToggleTheme,
        onClearHistory = {
          onClearHistory()
          onCloseDrawer()
        }
      )
    }
  }
}

// =========================================================================
// REUSABLE SUB-COMPONENTS
// =========================================================================

@Composable
private fun DrawerNavItem(
  title: String,
  icon: ImageVector,
  isSelected: Boolean,
  textColor: Color,
  selectedColor: Color,
  accentColor: Color,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = if (isSelected) selectedColor else Color.Transparent,
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("drawer_nav_${title.lowercase().replace(" ", "_")}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 9.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected) accentColor else textColor.copy(alpha = 0.7f),
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
          fontSize = 14.sp
        ),
        color = textColor
      )
    }
  }
}

@Composable
private fun ConversationRow(
  conversation: ConversationSession,
  isActive: Boolean,
  textPrimary: Color,
  textSecondary: Color,
  surfaceBg: Color,
  accentContainer: Color,
  accentColor: Color,
  onSelect: () -> Unit,
  onToggleStar: () -> Unit,
  onDelete: () -> Unit
) {
  var showMenu by remember { mutableStateOf(false) }

  Surface(
    shape = RoundedCornerShape(8.dp),
    color = if (isActive) accentContainer else Color.Transparent,
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onSelect() }
      .testTag("conv_item_${conversation.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = conversation.title.ifBlank { "Untitled Chat" },
          style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 13.sp
          ),
          color = textPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        if (conversation.previewText.isNotBlank()) {
          Text(
            text = conversation.previewText,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onToggleStar,
          modifier = Modifier.size(24.dp)
        ) {
          Icon(
            imageVector = if (conversation.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = if (conversation.isStarred) "Unstar" else "Star",
            tint = if (conversation.isStarred) Color(0xFFF59E0B) else textSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(15.dp)
          )
        }

        Box {
          IconButton(
            onClick = { showMenu = true },
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = "Options",
              tint = textSecondary.copy(alpha = 0.6f),
              modifier = Modifier.size(14.dp)
            )
          }

          DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
          ) {
            DropdownMenuItem(
              text = { Text("Delete") },
              leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(16.dp)) },
              onClick = {
                showMenu = false
                onDelete()
              }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun BottomAccountSection(
  isDark: Boolean,
  textPrimary: Color,
  textSecondary: Color,
  surfaceBg: Color,
  borderCol: Color,
  accentTerracotta: Color,
  onNavigateToSettings: () -> Unit,
  onNavigateToWorkspace: () -> Unit,
  onToggleTheme: () -> Unit,
  onClearHistory: () -> Unit
) {
  var showAccountMenu by remember { mutableStateOf(false) }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .windowInsetsPadding(WindowInsets.navigationBars)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier
        .weight(1f)
        .clickable { showAccountMenu = true }
        .testTag("drawer_account_row")
    ) {
      // User Avatar Circle
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(accentTerracotta.copy(alpha = 0.15f))
          .border(1.dp, accentTerracotta.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "D",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          ),
          color = accentTerracotta
        )
      }

      Column {
        Text(
          text = "dikshant7684",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
          ),
          color = textPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(Color(0xFF2E7D32))
          )
          Text(
            text = "Pro Plan · Active",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = textSecondary
          )
        }
      }
    }

    Box {
      IconButton(
        onClick = { showAccountMenu = true },
        modifier = Modifier.size(32.dp).testTag("drawer_account_more_btn")
      ) {
        Icon(
          Icons.Default.MoreVert,
          contentDescription = "Account Options",
          tint = textSecondary,
          modifier = Modifier.size(18.dp)
        )
      }

      DropdownMenu(
        expanded = showAccountMenu,
        onDismissRequest = { showAccountMenu = false }
      ) {
        DropdownMenuItem(
          text = { Text("Settings") },
          leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp)) },
          onClick = {
            showAccountMenu = false
            onNavigateToSettings()
          }
        )
        DropdownMenuItem(
          text = { Text("Code Workspace") },
          leadingIcon = { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp)) },
          onClick = {
            showAccountMenu = false
            onNavigateToWorkspace()
          }
        )
        DropdownMenuItem(
          text = { Text(if (isDark) "Light Mode" else "Dark Mode") },
          leadingIcon = {
            Icon(
              if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
          },
          onClick = {
            showAccountMenu = false
            onToggleTheme()
          }
        )
        Divider()
        DropdownMenuItem(
          text = { Text("Clear Chat History", color = Color(0xFFC62828)) },
          leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(18.dp)) },
          onClick = {
            showAccountMenu = false
            onClearHistory()
          }
        )
      }
    }
  }
}
