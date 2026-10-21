package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import com.example.marketplace.persistence.ExtensionExecutionDao
import com.example.marketplace.persistence.ExtensionExecutionRecordEntity
import com.example.marketplace.persistence.InstalledExtensionDao
import com.example.marketplace.persistence.InstalledExtensionEntity
import com.example.marketplace.persistence.MarketplaceDao
import com.example.marketplace.persistence.MarketplaceEntity

/**
 * Main Room Database for Claude Code, managing persistent chat messages,
 * project memories, and installed universal marketplace plugins/skills.
 */
@Database(
  entities = [
    ChatMessageEntity::class,
    ProjectMemoryEntity::class,
    MarketplaceEntity::class,
    InstalledExtensionEntity::class,
    ExtensionExecutionRecordEntity::class
  ],
  version = 3,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

  abstract fun chatMessageDao(): ChatMessageDao
  abstract fun projectMemoryDao(): ProjectMemoryDao
  abstract fun marketplaceDao(): MarketplaceDao
  abstract fun installedExtensionDao(): InstalledExtensionDao
  abstract fun extensionExecutionDao(): ExtensionExecutionDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "claude_code_chat.db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
