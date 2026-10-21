package com.example.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Categorized, persistent project memory isolated by projectId.
 * Stores non-secret architectural insights, conventions, build commands, and past decisions.
 */
@Entity(
  tableName = "project_memories",
  indices = [Index(value = ["projectId"]), Index(value = ["projectId", "key"], unique = true)]
)
data class ProjectMemoryEntity(
  @PrimaryKey
  val id: String,
  val projectId: String,
  val category: String, // ARCHITECTURE, CONVENTION, BUILD_INSTRUCTION, DECISION, LIMITATION, TASK_SUMMARY
  val key: String,
  val content: String,
  val timestamp: Long = System.currentTimeMillis(),
  val lastAccessedTimestamp: Long = System.currentTimeMillis(),
  val importance: Float = 0.5f,
  val tags: String = ""
)
