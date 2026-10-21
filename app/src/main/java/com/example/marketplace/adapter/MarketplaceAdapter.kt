package com.example.marketplace.adapter

import com.example.marketplace.model.EcosystemType
import com.example.marketplace.model.ExtensionBundle
import com.example.marketplace.model.InstalledExtension
import com.example.marketplace.model.MarketplaceSource
import com.example.marketplace.model.MarketplaceValidationResult
import com.example.marketplace.model.UniversalExtension

data class UpdateInfo(
  val extensionId: String,
  val currentVersion: String,
  val latestVersion: String,
  val changelog: String = "",
  val downloadUrl: String = ""
)

interface MarketplaceAdapter {
  val ecosystem: EcosystemType

  fun canHandle(source: MarketplaceSource): Boolean

  suspend fun validate(source: MarketplaceSource): MarketplaceValidationResult

  suspend fun fetchCatalog(source: MarketplaceSource): List<UniversalExtension>

  suspend fun resolveExtensionBundle(source: MarketplaceSource, extension: UniversalExtension): ExtensionBundle

  suspend fun checkForUpdates(installed: InstalledExtension, source: MarketplaceSource): UpdateInfo?
}
