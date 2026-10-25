package com.example.marketplace.service

import com.example.marketplace.adapter.MarketplaceAdapterRegistry
import com.example.marketplace.model.EcosystemType
import com.example.marketplace.model.MarketplaceSource
import com.example.marketplace.model.MarketplaceSourceType
import com.example.marketplace.model.UniversalExtension
import com.example.marketplace.model.ValidationStatus
import com.example.marketplace.persistence.MarketplaceDao
import com.example.marketplace.persistence.MarketplaceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MarketplaceManager(
  private val marketplaceDao: MarketplaceDao,
  private val adapterRegistry: MarketplaceAdapterRegistry = MarketplaceAdapterRegistry(),
  private val scope: CoroutineScope
) {

  val marketplacesFlow: Flow<List<MarketplaceSource>> = marketplaceDao.getAllMarketplacesFlow()
    .map { entities -> entities.map { it.toModel() } }

  private val _catalog = MutableStateFlow<List<UniversalExtension>>(emptyList())
  val catalog: StateFlow<List<UniversalExtension>> = _catalog.asStateFlow()

  private val _isRefreshing = MutableStateFlow(false)
  val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

  private val _lastRefreshMessage = MutableStateFlow<String?>(null)
  val lastRefreshMessage: StateFlow<String?> = _lastRefreshMessage.asStateFlow()

  init {
    scope.launch(Dispatchers.IO) {
      ensureDefaultMarketplaces()
      refreshAllMarketplaces()
    }
  }

  suspend fun ensureDefaultMarketplaces() = withContext(Dispatchers.IO) {
    val officialClaude = MarketplaceSource(
      id = "claude-plugins-official",
      name = "Anthropic Claude Plugins Official",
      sourceType = MarketplaceSourceType.GITHUB,
      location = "https://github.com/anthropics/claude-plugins-official",
      branch = "main",
      ecosystem = EcosystemType.CLAUDE_CODE,
      isEnabled = true,
      validationStatus = ValidationStatus.VALID
    )

    // Ensure the requested official repo is always present as default
    if (marketplaceDao.getMarketplaceById(officialClaude.id) == null) {
      marketplaceDao.insertMarketplace(MarketplaceEntity.fromModel(officialClaude))
    }

    val existing = marketplaceDao.getAllMarketplaces()
    if (existing.size <= 1) {
      val defaults = listOf(
        MarketplaceSource(
          id = "droidcode-official",
          name = "DroidCode Official Marketplace",
          sourceType = MarketplaceSourceType.DROIDCODE_NATIVE,
          location = "https://raw.githubusercontent.com/droidcode-org/marketplace/main/droidcode-marketplace.json",
          ecosystem = EcosystemType.DROIDCODE_NATIVE,
          isEnabled = true,
          validationStatus = ValidationStatus.VALID
        ),
        MarketplaceSource(
          id = "openai-codex-hub",
          name = "Codex Developer Extensions",
          sourceType = MarketplaceSourceType.HTTPS_MANIFEST,
          location = "https://raw.githubusercontent.com/openai/codex-plugins/main/ai-plugin.json",
          ecosystem = EcosystemType.OPENAI_CODEX,
          isEnabled = true,
          validationStatus = ValidationStatus.VALID
        )
      )
      marketplaceDao.insertMarketplaces(defaults.map { MarketplaceEntity.fromModel(it) })
    }
  }

  suspend fun addMarketplace(
    name: String,
    location: String,
    sourceType: MarketplaceSourceType,
    ecosystem: EcosystemType,
    branch: String = "main"
  ): Result<MarketplaceSource> = withContext(Dispatchers.IO) {
    try {
      val cleanLoc = location.trim()
      if (cleanLoc.isBlank()) {
        return@withContext Result.failure(IllegalArgumentException("Marketplace location cannot be empty."))
      }

      val id = "mkt_${UUID.randomUUID().toString().take(8)}"
      val source = MarketplaceSource(
        id = id,
        name = name.ifBlank { "Custom Marketplace" },
        sourceType = sourceType,
        location = cleanLoc,
        branch = branch.ifBlank { "main" },
        ecosystem = ecosystem,
        isEnabled = true,
        validationStatus = ValidationStatus.PENDING
      )

      val adapter = adapterRegistry.getAdapter(source)
      val validation = adapter.validate(source)

      val verified = source.copy(
        validationStatus = if (validation.isValid) ValidationStatus.VALID else ValidationStatus.INVALID,
        errorDetails = if (validation.isValid) null else validation.message,
        ecosystem = validation.detectedEcosystem
      )

      marketplaceDao.insertMarketplace(MarketplaceEntity.fromModel(verified))
      refreshAllMarketplaces()
      Result.success(verified)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun updateMarketplace(source: MarketplaceSource) = withContext(Dispatchers.IO) {
    marketplaceDao.updateMarketplace(MarketplaceEntity.fromModel(source))
    refreshAllMarketplaces()
  }

  suspend fun toggleMarketplace(id: String, isEnabled: Boolean) = withContext(Dispatchers.IO) {
    marketplaceDao.setEnabled(id, isEnabled)
    refreshAllMarketplaces()
  }

  suspend fun deleteMarketplace(id: String) = withContext(Dispatchers.IO) {
    marketplaceDao.deleteMarketplace(id)
    _catalog.value = _catalog.value.filter { it.marketplaceSourceId != id }
  }

  suspend fun refreshMarketplace(id: String): Result<Int> = withContext(Dispatchers.IO) {
    try {
      val entity = marketplaceDao.getMarketplaceById(id) ?: return@withContext Result.failure(Exception("Marketplace not found."))
      val source = entity.toModel()
      val adapter = adapterRegistry.getAdapter(source)
      val items = adapter.fetchCatalog(source)

      marketplaceDao.updateRefreshStatus(
        id = id,
        timestamp = System.currentTimeMillis(),
        status = ValidationStatus.VALID.name,
        error = null
      )

      val currentOther = _catalog.value.filter { it.marketplaceSourceId != id }
      _catalog.value = (currentOther + items).distinctBy { it.id }

      Result.success(items.size)
    } catch (e: Exception) {
      marketplaceDao.updateRefreshStatus(
        id = id,
        timestamp = System.currentTimeMillis(),
        status = ValidationStatus.ERROR.name,
        error = e.message
      )
      Result.failure(e)
    }
  }

  suspend fun refreshAllMarketplaces() = withContext(Dispatchers.IO) {
    _isRefreshing.value = true
    _lastRefreshMessage.value = "Synchronizing marketplaces..."
    try {
      val sources = marketplaceDao.getEnabledMarketplaces().map { it.toModel() }
      val allExtensions = mutableListOf<UniversalExtension>()

      for (source in sources) {
        try {
          val adapter = adapterRegistry.getAdapter(source)
          val items = adapter.fetchCatalog(source)
          allExtensions.addAll(items)
          marketplaceDao.updateRefreshStatus(
            id = source.id,
            timestamp = System.currentTimeMillis(),
            status = ValidationStatus.VALID.name,
            error = null
          )
        } catch (e: Exception) {
          marketplaceDao.updateRefreshStatus(
            id = source.id,
            timestamp = System.currentTimeMillis(),
            status = ValidationStatus.ERROR.name,
            error = e.message
          )
        }
      }

      _catalog.value = allExtensions.distinctBy { it.id }
      _lastRefreshMessage.value = "Marketplaces refreshed (${allExtensions.size} extensions available)"
    } finally {
      _isRefreshing.value = false
    }
  }

  fun getAdapterRegistry(): MarketplaceAdapterRegistry = adapterRegistry
}
