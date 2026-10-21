package com.example.marketplace.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketplaceDao {

  @Query("SELECT * FROM marketplaces ORDER BY name ASC")
  fun getAllMarketplacesFlow(): Flow<List<MarketplaceEntity>>

  @Query("SELECT * FROM marketplaces ORDER BY name ASC")
  suspend fun getAllMarketplaces(): List<MarketplaceEntity>

  @Query("SELECT * FROM marketplaces WHERE isEnabled = 1")
  suspend fun getEnabledMarketplaces(): List<MarketplaceEntity>

  @Query("SELECT * FROM marketplaces WHERE id = :id LIMIT 1")
  suspend fun getMarketplaceById(id: String): MarketplaceEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMarketplace(marketplace: MarketplaceEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMarketplaces(marketplaces: List<MarketplaceEntity>)

  @Update
  suspend fun updateMarketplace(marketplace: MarketplaceEntity)

  @Query("UPDATE marketplaces SET isEnabled = :isEnabled WHERE id = :id")
  suspend fun setEnabled(id: String, isEnabled: Boolean)

  @Query("UPDATE marketplaces SET lastRefreshTimestamp = :timestamp, validationStatus = :status, errorDetails = :error WHERE id = :id")
  suspend fun updateRefreshStatus(id: String, timestamp: Long, status: String, error: String?)

  @Query("DELETE FROM marketplaces WHERE id = :id")
  suspend fun deleteMarketplace(id: String)
}

@Dao
interface InstalledExtensionDao {

  @Query("SELECT * FROM installed_extensions ORDER BY installedTimestamp DESC")
  fun getAllInstalledFlow(): Flow<List<InstalledExtensionEntity>>

  @Query("SELECT * FROM installed_extensions ORDER BY installedTimestamp DESC")
  suspend fun getAllInstalled(): List<InstalledExtensionEntity>

  @Query("SELECT * FROM installed_extensions WHERE isEnabled = 1")
  suspend fun getEnabledInstalled(): List<InstalledExtensionEntity>

  @Query("SELECT * FROM installed_extensions WHERE id = :id LIMIT 1")
  suspend fun getInstalledById(id: String): InstalledExtensionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertInstalled(extension: InstalledExtensionEntity)

  @Update
  suspend fun updateInstalled(extension: InstalledExtensionEntity)

  @Query("UPDATE installed_extensions SET isEnabled = :isEnabled WHERE id = :id")
  suspend fun setEnabled(id: String, isEnabled: Boolean)

  @Query("UPDATE installed_extensions SET grantedPermissionsJson = :permsJson WHERE id = :id")
  suspend fun updatePermissions(id: String, permsJson: String)

  @Query("UPDATE installed_extensions SET hasUpdate = :hasUpdate, availableUpdateVersion = :updateVersion WHERE id = :id")
  suspend fun setUpdateStatus(id: String, hasUpdate: Boolean, updateVersion: String?)

  @Query("DELETE FROM installed_extensions WHERE id = :id")
  suspend fun deleteInstalled(id: String)
}

@Dao
interface ExtensionExecutionDao {

  @Insert
  suspend fun insertRecord(record: ExtensionExecutionRecordEntity)

  @Query("SELECT * FROM extension_executions WHERE extensionId = :extensionId ORDER BY invocationTime DESC LIMIT :limit")
  suspend fun getRecordsForExtension(extensionId: String, limit: Int = 50): List<ExtensionExecutionRecordEntity>

  @Query("SELECT * FROM extension_executions ORDER BY invocationTime DESC LIMIT :limit")
  suspend fun getRecentRecords(limit: Int = 100): List<ExtensionExecutionRecordEntity>
}
