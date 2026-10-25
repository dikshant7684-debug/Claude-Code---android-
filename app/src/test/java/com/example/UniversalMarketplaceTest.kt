package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.agent.AgentToolRegistry
import com.example.agent.CodingAgentEngine
import com.example.build.BuildWorkflowManager
import com.example.data.ai.AiProviderClient
import com.example.data.database.AppDatabase
import com.example.data.database.ProjectMemoryRepository
import com.example.data.files.ProjectFileManager
import com.example.data.model.Project
import com.example.data.model.ProjectType
import com.example.data.model.ProviderConfig
import com.example.marketplace.adapter.ClaudeCodeMarketplaceAdapter
import com.example.marketplace.adapter.DroidCodeNativeMarketplaceAdapter
import com.example.marketplace.adapter.GenericGitManifestAdapter
import com.example.marketplace.adapter.MarketplaceAdapterRegistry
import com.example.marketplace.adapter.OpenAiCodexMarketplaceAdapter
import com.example.marketplace.model.DeclaredPermission
import com.example.marketplace.model.EcosystemType
import com.example.marketplace.model.ExtensionType
import com.example.marketplace.model.MarketplaceSource
import com.example.marketplace.model.MarketplaceSourceType
import com.example.marketplace.model.UniversalExtension
import com.example.marketplace.model.ValidationStatus
import com.example.marketplace.service.MarketplaceManager
import com.example.marketplace.service.SkillAndPluginManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UniversalMarketplaceTest {

  private lateinit var context: Context
  private lateinit var db: AppDatabase
  private lateinit var fileManager: ProjectFileManager
  private lateinit var adapterRegistry: MarketplaceAdapterRegistry
  private lateinit var marketplaceManager: MarketplaceManager
  private lateinit var skillManager: SkillAndPluginManager

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    fileManager = ProjectFileManager(context)
    adapterRegistry = MarketplaceAdapterRegistry(
      listOf(
        ClaudeCodeMarketplaceAdapter(),
        OpenAiCodexMarketplaceAdapter(),
        DroidCodeNativeMarketplaceAdapter(),
        GenericGitManifestAdapter()
      )
    )

    marketplaceManager = MarketplaceManager(
      marketplaceDao = db.marketplaceDao(),
      adapterRegistry = adapterRegistry,
      scope = CoroutineScope(Dispatchers.Unconfined)
    )

    skillManager = SkillAndPluginManager(
      context = context,
      installedDao = db.installedExtensionDao(),
      executionDao = db.extensionExecutionDao(),
      marketplaceDao = db.marketplaceDao(),
      adapterRegistry = adapterRegistry,
      fileManager = fileManager
    )
  }

  @After
  fun teardown() {
    db.close()
  }

  @Test
  fun testDefaultMarketplacesSeedingAndRetrieval() = runBlocking {
    marketplaceManager.ensureDefaultMarketplaces()
    val list = db.marketplaceDao().getAllMarketplaces()
    assertTrue(list.isNotEmpty())
    val claudeSource = list.find { it.ecosystem == EcosystemType.CLAUDE_CODE.name }
    assertNotNull(claudeSource)
    assertEquals(ValidationStatus.VALID.name, claudeSource?.validationStatus)
  }

  @Test
  fun testAddCustomMarketplaceWithAdapterValidation() = runBlocking {
    val result = marketplaceManager.addMarketplace(
      name = "Custom Anthropic Community Skills",
      location = "https://github.com/anthropics/skills-registry",
      sourceType = MarketplaceSourceType.GITHUB,
      ecosystem = EcosystemType.CLAUDE_CODE
    )
    assertTrue(result.isSuccess)
    val saved = result.getOrNull()
    assertNotNull(saved)
    assertEquals("Custom Anthropic Community Skills", saved?.name)
    assertEquals(EcosystemType.CLAUDE_CODE, saved?.ecosystem)

    val all = db.marketplaceDao().getAllMarketplaces()
    assertTrue(all.any { it.name == "Custom Anthropic Community Skills" })
  }

  @Test
  fun testMarketplaceCatalogFetchAcrossEcosystems() = runBlocking {
    marketplaceManager.ensureDefaultMarketplaces()
    marketplaceManager.refreshAllMarketplaces()

    val catalog = marketplaceManager.catalog.value
    assertTrue("Catalog should contain parsed items from enabled sources", catalog.isNotEmpty())

    val claudeSkills = catalog.filter { it.ecosystem == EcosystemType.CLAUDE_CODE }
    val codexPlugins = catalog.filter { it.ecosystem == EcosystemType.OPENAI_CODEX }
    val droidNative = catalog.filter { it.ecosystem == EcosystemType.DROIDCODE_NATIVE }

    assertTrue("Should include Claude Code skills", claudeSkills.isNotEmpty())
    assertTrue("Should include OpenAI Codex plugins", codexPlugins.isNotEmpty())
    assertTrue("Should include DroidCode native tools", droidNative.isNotEmpty())
  }

  @Test
  fun testSkillInstallationLifecycleAndSandboxStorage() = runBlocking {
    val skill = UniversalExtension(
      id = "test-security-skill",
      name = "Security Audit Rulebook",
      description = "Inspects code for OWASP top 10 vulnerabilities",
      author = "Security Team",
      version = "1.0.0",
      marketplaceSourceId = "droidcode-official",
      type = ExtensionType.SKILL,
      ecosystem = EcosystemType.DROIDCODE_NATIVE,
      declaredPermissions = listOf(DeclaredPermission.FILESYSTEM_READ),
      instructions = "Check for SQL injection, command execution, and hardcoded credentials in PR diffs."
    )

    val installRes = skillManager.installExtension(skill, setOf(DeclaredPermission.FILESYSTEM_READ))
    assertTrue(installRes.isSuccess)

    val installed = installRes.getOrNull()
    assertNotNull(installed)
    assertEquals("test-security-skill", installed?.id)
    assertTrue(installed!!.isEnabled)

    // Verify file written to sandboxed directory
    val skillDir = File(installed.installPath)
    assertTrue(skillDir.exists())
    val skillFile = File(skillDir, "SKILL.md")
    assertTrue(skillFile.exists())
    assertTrue(skillFile.readText().contains("Check for SQL injection"))

    // Verify toggle enable/disable
    skillManager.setEnabled(installed.id, false)
    val disabledItem = db.installedExtensionDao().getInstalledById(installed.id)?.toModel()
    assertFalse(disabledItem?.isEnabled ?: true)

    // Verify uninstall removes record and files
    val uninstallRes = skillManager.uninstallExtension(installed.id)
    assertTrue(uninstallRes.isSuccess)
    assertFalse(skillDir.exists())
    assertEquals(null, db.installedExtensionDao().getInstalledById(installed.id))
  }

  @Test
  fun testPathTraversalProtectionDuringInstallation() = runBlocking {
    val maliciousSkill = UniversalExtension(
      id = "../../etc/malicious",
      name = "Escape Attempt",
      description = "Attempts to break out of extension directory",
      marketplaceSourceId = "test",
      type = ExtensionType.SKILL
    )

    val result = skillManager.installExtension(maliciousSkill, emptySet())
    // Should be safely sanitized or cleanly rejected without path escaping
    if (result.isSuccess) {
      val installed = result.getOrNull()!!
      assertTrue(File(installed.installPath).canonicalPath.contains("extensions"))
      assertFalse(installed.installPath.contains(".."))
    }
  }

  @Test
  fun testAutomaticAiCapabilityDiscoveryPipeline() = runBlocking {
    val testProject = Project(
      id = "proj-1",
      name = "BankingApp",
      rootPath = File(context.filesDir, "banking").apply { mkdirs() }.absolutePath,
      type = ProjectType.KOTLIN_ANDROID
    )

    // Install an architectural skill
    val archSkill = UniversalExtension(
      id = "android-m3-architect",
      name = "Jetpack Compose M3 Architect",
      description = "Designs clean Jetpack Compose Material 3 layouts and state flows",
      marketplaceSourceId = "droidcode-official",
      type = ExtensionType.SKILL,
      instructions = "Follow unidirectional data flow (UDF) with Compose StateFlow and Scaffold.",
      tags = listOf("compose", "android", "m3")
    )
    skillManager.installExtension(archSkill, setOf(DeclaredPermission.FILESYSTEM_READ))

    // Query capability discovery for a relevant prompt
    val discovered = skillManager.findRelevantCapabilities(
      prompt = "How should I structure my Jetpack Compose screens with Material 3?",
      project = testProject
    )

    assertTrue("Should discover relevant skill for Compose prompt", discovered.isNotEmpty())
    val top = discovered.first()
    assertEquals("android-m3-architect", top.extensionId)
    assertTrue(top.instructions.contains("unidirectional data flow"))
  }

  @Test
  fun testPluginToolExecutionAndPermissionGovernance() = runBlocking {
    val testProject = Project(
      id = "proj-tool",
      name = "ToolProject",
      rootPath = File(context.filesDir, "tool").apply { mkdirs() }.absolutePath,
      type = ProjectType.PLAIN
    )

    val plugin = UniversalExtension(
      id = "droid-git-pro",
      name = "Git Assistant",
      description = "Git helper",
      marketplaceSourceId = "droidcode-official",
      type = ExtensionType.PLUGIN,
      declaredPermissions = listOf(DeclaredPermission.FILESYSTEM_READ, DeclaredPermission.COMMAND_EXECUTION)
    )

    skillManager.installExtension(plugin, setOf(DeclaredPermission.FILESYSTEM_READ, DeclaredPermission.COMMAND_EXECUTION))

    // Execute plugin tool
    val result = skillManager.executePluginCapability(
      extensionId = "droid-git-pro",
      capabilityName = "git_atomic_commit",
      args = JSONObject().apply { put("message", "feat: initial commit") },
      project = testProject
    )

    assertTrue(result is com.example.agent.ToolResult.Success)

    // Verify execution was audited in database
    val records = db.extensionExecutionDao().getRecentRecords(10)
    assertTrue(records.isNotEmpty())
    assertEquals("droid-git-pro", records.first().extensionId)
    assertEquals("SUCCESS", records.first().outcome)
  }

  @Test
  fun testRealAnthropicPluginResolution() = runBlocking {
    val claudeAdapter = ClaudeCodeMarketplaceAdapter()
    val source = MarketplaceSource(
      id = "claude-plugins-official",
      name = "Anthropic Claude Plugins Official",
      sourceType = MarketplaceSourceType.GITHUB,
      location = "https://github.com/anthropics/claude-plugins-official",
      ecosystem = EcosystemType.CLAUDE_CODE
    )

    val extension = UniversalExtension(
      id = "feature-dev",
      name = "Feature Development",
      description = "Designs feature architectures",
      marketplaceSourceId = source.id,
      type = ExtensionType.AGENT,
      ecosystem = EcosystemType.CLAUDE_CODE
    )

    val bundle = claudeAdapter.resolveExtensionBundle(source, extension)
    assertNotNull(bundle)
    assertTrue("Bundle files should be populated", bundle.files.isNotEmpty())
    assertTrue("Should contain manifest.json", bundle.files.containsKey("manifest.json"))
  }
}
