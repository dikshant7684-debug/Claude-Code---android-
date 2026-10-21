package com.example.marketplace.adapter

import com.example.marketplace.model.EcosystemType
import com.example.marketplace.model.MarketplaceSource

class MarketplaceAdapterRegistry(
  val adapters: List<MarketplaceAdapter> = listOf(
    ClaudeCodeMarketplaceAdapter(),
    OpenAiCodexMarketplaceAdapter(),
    DroidCodeNativeMarketplaceAdapter(),
    GenericGitManifestAdapter()
  )
) {

  fun getAdapter(source: MarketplaceSource): MarketplaceAdapter {
    // 1. Check matching ecosystem directly
    val exactEcosystem = adapters.firstOrNull { it.ecosystem == source.ecosystem }
    if (exactEcosystem != null && exactEcosystem.canHandle(source)) {
      return exactEcosystem
    }

    // 2. Check by location / heuristics
    for (adapter in adapters) {
      if (adapter !is GenericGitManifestAdapter && adapter.canHandle(source)) {
        return adapter
      }
    }

    // 3. Fallback to generic adapter
    return adapters.first { it is GenericGitManifestAdapter }
  }

  fun getAdapterForEcosystem(ecosystem: EcosystemType): MarketplaceAdapter {
    return adapters.firstOrNull { it.ecosystem == ecosystem }
      ?: adapters.first { it is GenericGitManifestAdapter }
  }
}
