package com.example.marketplace.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.marketplace.model.DeclaredPermission
import com.example.marketplace.model.EcosystemType
import com.example.marketplace.model.ExtensionType
import com.example.marketplace.model.InstalledExtension
import com.example.marketplace.model.MarketplaceSource
import com.example.marketplace.model.MarketplaceSourceType
import com.example.marketplace.model.ValidationStatus
import org.json.JSONArray

@Entity(tableName = "marketplaces")
data class MarketplaceEntity(
  @PrimaryKey
  val id: String,
  val name: String,
  val sourceType: String,
  val location: String,
  val branch: String = "main",
  val authRef: String? = null,
  val ecosystem: String = EcosystemType.DROIDCODE_NATIVE.name,
  val isEnabled: Boolean = true,
  val lastRefreshTimestamp: Long = 0L,
  val validationStatus: String = ValidationStatus.PENDING.name,
  val errorDetails: String? = null,
  val etag: String? = null
) {
  fun toModel(): MarketplaceSource {
    return MarketplaceSource(
      id = id,
      name = name,
      sourceType = try { MarketplaceSourceType.valueOf(sourceType) } catch (_: Exception) { MarketplaceSourceType.DROIDCODE_NATIVE },
      location = location,
      branch = branch,
      authRef = authRef,
      ecosystem = try { EcosystemType.valueOf(ecosystem) } catch (_: Exception) { EcosystemType.DROIDCODE_NATIVE },
      isEnabled = isEnabled,
      lastRefreshTimestamp = lastRefreshTimestamp,
      validationStatus = try { ValidationStatus.valueOf(validationStatus) } catch (_: Exception) { ValidationStatus.PENDING },
      errorDetails = errorDetails,
      etag = etag
    )
  }

  companion object {
    fun fromModel(model: MarketplaceSource): MarketplaceEntity {
      return MarketplaceEntity(
        id = model.id,
        name = model.name,
        sourceType = model.sourceType.name,
        location = model.location,
        branch = model.branch,
        authRef = model.authRef,
        ecosystem = model.ecosystem.name,
        isEnabled = model.isEnabled,
        lastRefreshTimestamp = model.lastRefreshTimestamp,
        validationStatus = model.validationStatus.name,
        errorDetails = model.errorDetails,
        etag = model.etag
      )
    }
  }
}

@Entity(tableName = "installed_extensions")
data class InstalledExtensionEntity(
  @PrimaryKey
  val id: String,
  val name: String,
  val type: String,
  val version: String,
  val sourceMarketplaceId: String,
  val installPath: String,
  val isEnabled: Boolean = true,
  val grantedPermissionsJson: String = "[]",
  val manifestJson: String = "",
  val installedTimestamp: Long = System.currentTimeMillis(),
  val hasUpdate: Boolean = false,
  val availableUpdateVersion: String? = null
) {
  fun toModel(): InstalledExtension {
    val perms = mutableSetOf<DeclaredPermission>()
    try {
      val arr = JSONArray(grantedPermissionsJson)
      for (i in 0 until arr.length()) {
        perms.add(DeclaredPermission.valueOf(arr.getString(i)))
      }
    } catch (_: Exception) {}

    return InstalledExtension(
      id = id,
      name = name,
      type = try { ExtensionType.valueOf(type) } catch (_: Exception) { ExtensionType.SKILL },
      version = version,
      sourceMarketplaceId = sourceMarketplaceId,
      installPath = installPath,
      isEnabled = isEnabled,
      grantedPermissions = perms,
      manifestJson = manifestJson,
      installedTimestamp = installedTimestamp,
      hasUpdate = hasUpdate,
      availableUpdateVersion = availableUpdateVersion
    )
  }

  companion object {
    fun fromModel(model: InstalledExtension): InstalledExtensionEntity {
      val permArray = JSONArray()
      model.grantedPermissions.forEach { permArray.put(it.name) }
      return InstalledExtensionEntity(
        id = model.id,
        name = model.name,
        type = model.type.name,
        version = model.version,
        sourceMarketplaceId = model.sourceMarketplaceId,
        installPath = model.installPath,
        isEnabled = model.isEnabled,
        grantedPermissionsJson = permArray.toString(),
        manifestJson = model.manifestJson,
        installedTimestamp = model.installedTimestamp,
        hasUpdate = model.hasUpdate,
        availableUpdateVersion = model.availableUpdateVersion
      )
    }
  }
}

@Entity(tableName = "extension_executions")
data class ExtensionExecutionRecordEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val taskId: String,
  val extensionId: String,
  val capabilityName: String,
  val invocationTime: Long = System.currentTimeMillis(),
  val outcome: String,
  val durationMs: Long = 0,
  val sanitizedSummary: String = ""
)
