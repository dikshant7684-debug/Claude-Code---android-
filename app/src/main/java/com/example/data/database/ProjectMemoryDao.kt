package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectMemoryDao {

  @Query("SELECT * FROM project_memories WHERE projectId = :projectId ORDER BY importance DESC, lastAccessedTimestamp DESC")
  fun getMemoriesForProjectFlow(projectId: String): Flow<List<ProjectMemoryEntity>>

  @Query("SELECT * FROM project_memories WHERE projectId = :projectId ORDER BY importance DESC, lastAccessedTimestamp DESC")
  suspend fun getMemoriesForProject(projectId: String): List<ProjectMemoryEntity>

  @Query("SELECT * FROM project_memories WHERE projectId = :projectId AND (category = :category OR :category = '') ORDER BY importance DESC")
  suspend fun getMemoriesByCategory(projectId: String, category: String): List<ProjectMemoryEntity>

  @Query("SELECT * FROM project_memories WHERE projectId = :projectId AND key = :key LIMIT 1")
  suspend fun getMemoryByKey(projectId: String, key: String): ProjectMemoryEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMemory(memory: ProjectMemoryEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMemories(memories: List<ProjectMemoryEntity>)

  @Update
  suspend fun updateMemory(memory: ProjectMemoryEntity)

  @Query("UPDATE project_memories SET lastAccessedTimestamp = :timestamp WHERE id = :id")
  suspend fun updateAccessTime(id: String, timestamp: Long = System.currentTimeMillis())

  @Query("DELETE FROM project_memories WHERE id = :id")
  suspend fun deleteMemory(id: String)

  @Query("DELETE FROM project_memories WHERE projectId = :projectId")
  suspend fun clearMemoriesForProject(projectId: String)

  @Query("DELETE FROM project_memories")
  suspend fun clearAll()
}
