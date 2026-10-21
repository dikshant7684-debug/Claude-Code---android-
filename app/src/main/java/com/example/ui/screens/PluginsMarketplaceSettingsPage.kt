package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marketplace.model.DeclaredPermission
import com.example.marketplace.model.EcosystemType
import com.example.marketplace.model.ExtensionType
import com.example.marketplace.model.InstalledExtension
import com.example.marketplace.model.MarketplaceSource
import com.example.marketplace.model.MarketplaceSourceType
import com.example.marketplace.model.UniversalExtension
import com.example.marketplace.model.ValidationStatus
import com.example.ui.theme.ClaudeBorder
import com.example.ui.theme.ClaudeBorderLight
import com.example.ui.theme.ClaudePureWhite
import com.example.ui.theme.ClaudeSurfaceSubtle
import com.example.ui.theme.ClaudeTextPrimary
import com.example.ui.theme.ClaudeTextSecondary
import com.example.ui.theme.ClaudeWarmBg
import com.example.ui.theme.DroidOrange
import com.example.ui.theme.DroidOrangeContainer
import com.example.ui.viewmodel.MainViewModel

enum class MarketplaceTab(val label: String) {
  INSTALLED("Installed"),
  DISCOVER("Discover"),
  MARKETPLACES("Marketplaces"),
  UPDATES("Updates"),
  PERMISSIONS("Permissions"),
  IMPORT("Import")
}

@Composable
fun PluginsMarketplaceSettingsPage(viewModel: MainViewModel) {
  var selectedTab by remember { mutableStateOf(MarketplaceTab.INSTALLED) }

  val installedList by viewModel.installedExtensions.collectAsState()
  val catalogList by viewModel.marketplaceCatalog.collectAsState()
  val marketplacesList by viewModel.marketplaces.collectAsState()
  val isRefreshing by viewModel.isRefreshingMarketplaces.collectAsState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ClaudeWarmBg)
  ) {
    // Top Tab Selector Bar
    Surface(
      color = ClaudePureWhite,
      border = BorderStroke(1.dp, ClaudeBorderLight),
      modifier = Modifier.fillMaxWidth()
    ) {
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        items(MarketplaceTab.values()) { tab ->
          val isSelected = tab == selectedTab
          val bg = if (isSelected) DroidOrangeContainer else Color.Transparent
          val textColor = if (isSelected) DroidOrange else ClaudeTextSecondary

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(bg)
              .clickable { selectedTab = tab }
              .padding(horizontal = 12.dp, vertical = 8.dp)
              .testTag("marketplace_tab_${tab.name.lowercase()}"),
            contentAlignment = Alignment.Center
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                text = tab.label,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontSize = 13.sp,
                  fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = textColor
              )

              if (tab == MarketplaceTab.UPDATES) {
                val updatesCount = installedList.count { it.hasUpdate }
                if (updatesCount > 0) {
                  Box(
                    modifier = Modifier
                      .size(16.dp)
                      .clip(CircleShape)
                      .background(DroidOrange),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "$updatesCount",
                      style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color.White)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Tab Content Area
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      when (selectedTab) {
        MarketplaceTab.INSTALLED -> InstalledExtensionsSection(
          installedList = installedList,
          viewModel = viewModel,
          onNavigateToDiscover = { selectedTab = MarketplaceTab.DISCOVER }
        )
        MarketplaceTab.DISCOVER -> DiscoverCatalogSection(
          catalogList = catalogList,
          installedList = installedList,
          marketplacesList = marketplacesList,
          isRefreshing = isRefreshing,
          viewModel = viewModel
        )
        MarketplaceTab.MARKETPLACES -> MarketplacesManagerSection(
          marketplacesList = marketplacesList,
          isRefreshing = isRefreshing,
          viewModel = viewModel
        )
        MarketplaceTab.UPDATES -> ExtensionUpdatesSection(
          installedList = installedList,
          viewModel = viewModel
        )
        MarketplaceTab.PERMISSIONS -> ExtensionPermissionsSection(
          installedList = installedList,
          viewModel = viewModel
        )
        MarketplaceTab.IMPORT -> ImportExtensionSection(
          viewModel = viewModel,
          onImportSuccess = { selectedTab = MarketplaceTab.INSTALLED }
        )
      }
    }
  }
}

// =========================================================================
// SECTION A: INSTALLED EXTENSIONS
// =========================================================================

@Composable
fun InstalledExtensionsSection(
  installedList: List<InstalledExtension>,
  viewModel: MainViewModel,
  onNavigateToDiscover: () -> Unit
) {
  val context = LocalContext.current

  if (installedList.isEmpty()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = Icons.Default.Extension,
        contentDescription = null,
        tint = ClaudeTextSecondary.copy(alpha = 0.5f),
        modifier = Modifier.size(54.dp)
      )
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "No Extensions Installed",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = ClaudeTextPrimary
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Browse the Discover catalog to install skills, specialized agents, plugins, and MCP tool providers.",
        style = MaterialTheme.typography.bodySmall,
        color = ClaudeTextSecondary,
        modifier = Modifier.padding(horizontal = 24.dp)
      )
      Spacer(modifier = Modifier.height(16.dp))
      Button(
        onClick = onNavigateToDiscover,
        colors = ButtonDefaults.buttonColors(containerColor = DroidOrange)
      ) {
        Text("Browse Discover Catalog", color = Color.White)
      }
    }
  } else {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Installed (${installedList.size})",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = ClaudeTextPrimary
          )
          TextButton(
            onClick = {
              viewModel.checkForExtensionUpdates()
              Toast.makeText(context, "Checking for extension updates...", Toast.LENGTH_SHORT).show()
            }
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Check Updates", fontSize = 12.sp)
          }
        }
      }

      items(installedList, key = { it.id }) { item ->
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = ClaudePureWhite,
          border = BorderStroke(1.dp, ClaudeBorderLight),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Top
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = ClaudeTextPrimary
                  )
                  // Type badge
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = ClaudeSurfaceSubtle,
                    border = BorderStroke(0.5.dp, ClaudeBorder)
                  ) {
                    Text(
                      text = item.type.displayName,
                      style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                      color = ClaudeTextSecondary,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "v${item.version} · ${item.grantedPermissions.size} permissions granted",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                  color = ClaudeTextSecondary
                )
              }

              // Active toggle
              Switch(
                checked = item.isEnabled,
                onCheckedChange = { viewModel.toggleExtension(item.id, it) },
                colors = SwitchDefaults.colors(checkedThumbColor = DroidOrange, checkedTrackColor = DroidOrangeContainer),
                modifier = Modifier.size(36.dp)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = ClaudeBorderLight.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (item.isEnabled) "● Active in agent tasks" else "○ Disabled",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 11.sp,
                  color = if (item.isEnabled) Color(0xFF2E7D32) else ClaudeTextSecondary
                )
              )

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                  onClick = {
                    viewModel.uninstallExtension(item.id) { success ->
                      if (success) Toast.makeText(context, "Uninstalled '${item.name}'", Toast.LENGTH_SHORT).show()
                    }
                  }
                ) {
                  Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Uninstall", color = Color(0xFFC62828), fontSize = 12.sp)
                }
              }
            }
          }
        }
      }
    }
  }
}

// =========================================================================
// SECTION B: DISCOVER EXTENSIONS
// =========================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscoverCatalogSection(
  catalogList: List<UniversalExtension>,
  installedList: List<InstalledExtension>,
  marketplacesList: List<MarketplaceSource>,
  isRefreshing: Boolean,
  viewModel: MainViewModel
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedTypeFilter by remember { mutableStateOf<ExtensionType?>(null) }
  var selectedEcosystemFilter by remember { mutableStateOf<EcosystemType?>(null) }
  var selectedMarketplaceFilter by remember { mutableStateOf<String?>(null) }
  var inspectingExtension by remember { mutableStateOf<UniversalExtension?>(null) }

  val context = LocalContext.current

  val filteredList = catalogList.filter { ext ->
    val matchesQuery = searchQuery.isBlank() ||
      ext.name.contains(searchQuery, ignoreCase = true) ||
      ext.description.contains(searchQuery, ignoreCase = true) ||
      ext.author.contains(searchQuery, ignoreCase = true) ||
      ext.tags.any { it.contains(searchQuery, ignoreCase = true) }

    val matchesType = selectedTypeFilter == null || ext.type == selectedTypeFilter
    val matchesEco = selectedEcosystemFilter == null || ext.ecosystem == selectedEcosystemFilter
    val matchesMkt = selectedMarketplaceFilter == null || ext.marketplaceSourceId == selectedMarketplaceFilter

    matchesQuery && matchesType && matchesEco && matchesMkt
  }

  Column(modifier = Modifier.fillMaxSize()) {
    // Search Box
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search skills, agents, plugins, tools...", fontSize = 13.sp) },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ClaudeTextSecondary, modifier = Modifier.size(18.dp)) },
      trailingIcon = {
        if (searchQuery.isNotBlank()) {
          IconButton(onClick = { searchQuery = "" }) {
            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
          }
        }
      },
      singleLine = true,
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = DroidOrange,
        unfocusedBorderColor = ClaudeBorder,
        focusedContainerColor = ClaudePureWhite,
        unfocusedContainerColor = ClaudePureWhite
      ),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("marketplace_search_input")
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Filter Chips: Ecosystem & Type
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      item {
        FilterPill(
          label = "All Ecosystems",
          isSelected = selectedEcosystemFilter == null,
          onClick = { selectedEcosystemFilter = null }
        )
      }
      items(EcosystemType.values()) { eco ->
        FilterPill(
          label = eco.displayName,
          isSelected = selectedEcosystemFilter == eco,
          onClick = { selectedEcosystemFilter = if (selectedEcosystemFilter == eco) null else eco }
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Type Filter Chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      item {
        FilterPill(
          label = "All Types",
          isSelected = selectedTypeFilter == null,
          onClick = { selectedTypeFilter = null }
        )
      }
      items(ExtensionType.values()) { type ->
        FilterPill(
          label = type.displayName,
          isSelected = selectedTypeFilter == type,
          onClick = { selectedTypeFilter = if (selectedTypeFilter == type) null else type }
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Catalog List or Loading
    if (isRefreshing && catalogList.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = DroidOrange, modifier = Modifier.size(32.dp))
      }
    } else if (filteredList.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No extensions match your filters.", color = ClaudeTextSecondary, style = MaterialTheme.typography.bodyMedium)
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredList, key = { it.id }) { item ->
          val isInstalled = installedList.any { it.id == item.id }

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = ClaudePureWhite,
            border = BorderStroke(1.dp, ClaudeBorderLight),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { inspectingExtension = item }
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Text(
                      text = item.name,
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                      color = ClaudeTextPrimary
                    )
                    // Ecosystem Pill
                    Surface(
                      shape = RoundedCornerShape(4.dp),
                      color = Color(item.ecosystem.badgeColorHex).copy(alpha = 0.12f)
                    ) {
                      Text(
                        text = item.ecosystem.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                          fontSize = 9.sp,
                          fontWeight = FontWeight.SemiBold,
                          color = Color(item.ecosystem.badgeColorHex)
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                  }

                  Text(
                    text = "by ${item.author} · v${item.version} · ${item.type.displayName}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = ClaudeTextSecondary
                  )
                }

                // Install or Installed button
                if (isInstalled) {
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFE8F5E9),
                    border = BorderStroke(1.dp, Color(0xFFC8E6C9))
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                      Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(12.dp))
                      Text("Installed", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                    }
                  }
                } else {
                  Button(
                    onClick = {
                      viewModel.installExtension(item) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                      }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DroidOrange),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                  ) {
                    Text("Install", fontSize = 12.sp, color = Color.White)
                  }
                }
              }

              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                color = ClaudeTextPrimary.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )

              if (item.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                  verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  item.tags.take(4).forEach { tag ->
                    Surface(
                      shape = RoundedCornerShape(4.dp),
                      color = ClaudeSurfaceSubtle
                    ) {
                      Text(
                        text = "#$tag",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = ClaudeTextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Extension Details Modal
  if (inspectingExtension != null) {
    val ext = inspectingExtension!!
    val isInstalled = installedList.any { it.id == ext.id }

    AlertDialog(
      onDismissRequest = { inspectingExtension = null },
      title = {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = ext.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = Color(ext.ecosystem.badgeColorHex).copy(alpha = 0.12f)
            ) {
              Text(
                text = ext.ecosystem.displayName,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color(ext.ecosystem.badgeColorHex)),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
          Text(text = "Author: ${ext.author} · Version: ${ext.version} · ${ext.license} License", fontSize = 11.sp, color = ClaudeTextSecondary)
        }
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(text = ext.description, style = MaterialTheme.typography.bodyMedium)
          Spacer(modifier = Modifier.height(12.dp))

          Text(text = "Declared Permissions:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
          if (ext.declaredPermissions.isEmpty()) {
            Text(text = "None required.", fontSize = 12.sp, color = ClaudeTextSecondary)
          } else {
            ext.declaredPermissions.forEach { perm ->
              Text(text = "• ${perm.displayName}: ${perm.description}", fontSize = 11.sp, color = ClaudeTextSecondary)
            }
          }

          if (ext.instructions.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Capability Overview:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Surface(
              color = ClaudeSurfaceSubtle,
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
              Text(
                text = ext.instructions.take(300) + if (ext.instructions.length > 300) "..." else "",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                color = ClaudeTextPrimary,
                modifier = Modifier.padding(8.dp)
              )
            }
          }
        }
      },
      confirmButton = {
        if (!isInstalled) {
          Button(
            onClick = {
              viewModel.installExtension(ext) { success, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                inspectingExtension = null
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = DroidOrange)
          ) {
            Text("Install Extension", color = Color.White)
          }
        } else {
          Button(
            onClick = {
              viewModel.uninstallExtension(ext.id) {
                Toast.makeText(context, "Uninstalled '${ext.name}'", Toast.LENGTH_SHORT).show()
                inspectingExtension = null
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
          ) {
            Text("Uninstall", color = Color.White)
          }
        }
      },
      dismissButton = {
        TextButton(onClick = { inspectingExtension = null }) {
          Text("Close", color = ClaudeTextSecondary)
        }
      },
      containerColor = ClaudePureWhite
    )
  }
}

// =========================================================================
// SECTION C: MARKETPLACES MANAGER
// =========================================================================

@Composable
fun MarketplacesManagerSection(
  marketplacesList: List<MarketplaceSource>,
  isRefreshing: Boolean,
  viewModel: MainViewModel
) {
  var showAddDialog by remember { mutableStateOf(false) }
  val context = LocalContext.current

  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Configured Sources (${marketplacesList.size})",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = ClaudeTextPrimary
      )

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
          onClick = { viewModel.refreshAllMarketplaces() },
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Sync All", fontSize = 12.sp)
        }

        Button(
          onClick = { showAddDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = DroidOrange),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Add Source", fontSize = 12.sp, color = Color.White)
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(marketplacesList, key = { it.id }) { mkt ->
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = ClaudePureWhite,
          border = BorderStroke(1.dp, ClaudeBorderLight),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Top
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = mkt.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = ClaudeTextPrimary
                  )
                  // Validation status indicator
                  val statusColor = when (mkt.validationStatus) {
                    ValidationStatus.VALID -> Color(0xFF2E7D32)
                    ValidationStatus.INVALID -> Color(0xFFC62828)
                    ValidationStatus.PENDING -> Color(0xFFED6C02)
                    ValidationStatus.ERROR -> Color(0xFFC62828)
                  }
                  Box(
                    modifier = Modifier
                      .size(8.dp)
                      .clip(CircleShape)
                      .background(statusColor)
                  )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = mkt.location,
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                  color = ClaudeTextSecondary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )

                Text(
                  text = "${mkt.sourceType.displayName} · ${mkt.ecosystem.displayName}",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                  color = ClaudeTextSecondary
                )
              }

              Switch(
                checked = mkt.isEnabled,
                onCheckedChange = { viewModel.toggleMarketplace(mkt.id, it) },
                colors = SwitchDefaults.colors(checkedThumbColor = DroidOrange, checkedTrackColor = DroidOrangeContainer),
                modifier = Modifier.size(36.dp)
              )
            }

            if (mkt.errorDetails != null) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Error: ${mkt.errorDetails}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = Color(0xFFC62828))
              )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              TextButton(
                onClick = { viewModel.deleteMarketplace(mkt.id) }
              ) {
                Text("Remove", color = Color(0xFFC62828), fontSize = 11.sp)
              }
            }
          }
        }
      }
    }
  }

  if (showAddDialog) {
    AddMarketplaceDialog(
      onDismiss = { showAddDialog = false },
      onConfirm = { name, loc, type, eco ->
        viewModel.addCustomMarketplace(name, loc, type, eco) { success, msg ->
          Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
          if (success) showAddDialog = false
        }
      }
    )
  }
}

// =========================================================================
// SECTION D: EXTENSION UPDATES
// =========================================================================

@Composable
fun ExtensionUpdatesSection(
  installedList: List<InstalledExtension>,
  viewModel: MainViewModel
) {
  val context = LocalContext.current
  val updateable = installedList.filter { it.hasUpdate }

  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Available Updates (${updateable.size})",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = ClaudeTextPrimary
      )

      Button(
        onClick = { viewModel.checkForExtensionUpdates() },
        colors = ButtonDefaults.buttonColors(containerColor = DroidOrange),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
      ) {
        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Check Now", fontSize = 12.sp, color = Color.White)
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (updateable.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(36.dp))
          Spacer(modifier = Modifier.height(8.dp))
          Text("All installed extensions are up to date.", color = ClaudeTextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(updateable) { item ->
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = ClaudePureWhite,
            border = BorderStroke(1.dp, ClaudeBorderLight),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(text = item.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Text(
                  text = "Installed: v${item.version} → Available: v${item.availableUpdateVersion ?: "Latest"}",
                  fontSize = 11.sp,
                  color = ClaudeTextSecondary
                )
              }

              Button(
                onClick = {
                  Toast.makeText(context, "Updating '${item.name}'...", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DroidOrange),
                shape = RoundedCornerShape(6.dp)
              ) {
                Text("Update", fontSize = 12.sp, color = Color.White)
              }
            }
          }
        }
      }
    }
  }
}

// =========================================================================
// SECTION E: PERMISSIONS MANAGER
// =========================================================================

@Composable
fun ExtensionPermissionsSection(
  installedList: List<InstalledExtension>,
  viewModel: MainViewModel
) {
  Column(modifier = Modifier.fillMaxSize()) {
    Text(
      text = "Security & Permission Governance",
      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
      color = ClaudeTextPrimary
    )
    Text(
      text = "Review and toggle declared permissions per extension. Dangerous operations are restricted unless authorized.",
      style = MaterialTheme.typography.bodySmall,
      color = ClaudeTextSecondary
    )

    Spacer(modifier = Modifier.height(12.dp))

    if (installedList.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No installed extensions to govern.", color = ClaudeTextSecondary)
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(installedList, key = { it.id }) { item ->
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = ClaudePureWhite,
            border = BorderStroke(1.dp, ClaudeBorderLight),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "${item.name} (${item.type.displayName})",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = ClaudeTextPrimary
              )
              Spacer(modifier = Modifier.height(8.dp))

              DeclaredPermission.values().forEach { perm ->
                val isGranted = item.grantedPermissions.contains(perm)

                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = perm.displayName,
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = if (perm.isDangerous) Color(0xFFC62828) else ClaudeTextPrimary
                      )
                    )
                    Text(text = perm.description, fontSize = 10.sp, color = ClaudeTextSecondary)
                  }

                  Switch(
                    checked = isGranted,
                    onCheckedChange = { granted ->
                      val updated = if (granted) item.grantedPermissions + perm else item.grantedPermissions - perm
                      viewModel.updateExtensionPermissions(item.id, updated)
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = DroidOrange, checkedTrackColor = DroidOrangeContainer),
                    modifier = Modifier.size(32.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

// =========================================================================
// SECTION F: IMPORT EXTENSION
// =========================================================================

@Composable
fun ImportExtensionSection(
  viewModel: MainViewModel,
  onImportSuccess: () -> Unit
) {
  var importUrl by remember { mutableStateOf("") }
  var selectedType by remember { mutableStateOf(MarketplaceSourceType.GITHUB) }
  var selectedEco by remember { mutableStateOf(EcosystemType.CLAUDE_CODE) }
  var isImporting by remember { mutableStateOf(false) }
  val context = LocalContext.current

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(8.dp)
  ) {
    Text(
      text = "Import Custom Package or Repository",
      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
      color = ClaudeTextPrimary
    )
    Text(
      text = "Import a public GitHub/GitLab repository, raw manifest URL, or community skill repository directly into DroidCode.",
      style = MaterialTheme.typography.bodySmall,
      color = ClaudeTextSecondary
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
      value = importUrl,
      onValueChange = { importUrl = it },
      label = { Text("Repository or Manifest URL") },
      placeholder = { Text("https://github.com/owner/claude-skill-repo") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
      colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DroidOrange)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
      onClick = {
        if (importUrl.isBlank()) {
          Toast.makeText(context, "Please enter a valid URL.", Toast.LENGTH_SHORT).show()
        } else {
          isImporting = true
          viewModel.addCustomMarketplace(
            name = importUrl.substringAfterLast("/").removeSuffix(".git"),
            location = importUrl,
            sourceType = selectedType,
            ecosystem = selectedEco
          ) { success, msg ->
            isImporting = false
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            if (success) onImportSuccess()
          }
        }
      },
      colors = ButtonDefaults.buttonColors(containerColor = DroidOrange),
      modifier = Modifier.fillMaxWidth()
    ) {
      if (isImporting) {
        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
      }
      Text("Validate & Import Extension Source", color = Color.White)
    }
  }
}

// =========================================================================
// HELPER COMPONENTS & DIALOGS
// =========================================================================

@Composable
fun FilterPill(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val bg by animateColorAsState(if (isSelected) DroidOrangeContainer else ClaudePureWhite, label = "pill_bg")
  val borderCol by animateColorAsState(if (isSelected) DroidOrange else ClaudeBorderLight, label = "pill_border")
  val textCol = if (isSelected) DroidOrange else ClaudeTextSecondary

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = bg,
    border = BorderStroke(1.dp, borderCol),
    modifier = Modifier.clickable { onClick() }
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        fontSize = 11.sp,
        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
      ),
      color = textCol,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
    )
  }
}

@Composable
fun AddMarketplaceDialog(
  onDismiss: () -> Unit,
  onConfirm: (name: String, location: String, type: MarketplaceSourceType, eco: EcosystemType) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var location by remember { mutableStateOf("") }
  var selectedType by remember { mutableStateOf(MarketplaceSourceType.GITHUB) }
  var selectedEco by remember { mutableStateOf(EcosystemType.CLAUDE_CODE) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Marketplace Source", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Marketplace Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = location,
          onValueChange = { location = it },
          label = { Text("Location (URL or Git Repo)") },
          placeholder = { Text("https://github.com/org/skills") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(name, location, selectedType, selectedEco) },
        colors = ButtonDefaults.buttonColors(containerColor = DroidOrange)
      ) {
        Text("Save & Validate", color = Color.White)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    },
    containerColor = ClaudePureWhite
  )
}
