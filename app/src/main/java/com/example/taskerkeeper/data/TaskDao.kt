package com.example.taskerkeeper.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY is_checked ASC")
    fun getAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE taskId IN (:userIds)")
    suspend fun loadAllByIds(userIds: IntArray): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE task_string LIKE :first LIMIT 1")
    suspend fun findByTask(first: String): TaskEntity

    @Insert
    suspend fun insertAll(vararg tasks: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)
}