package com.joshuaschori.taskerkeeper.data.tasks

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskCategoryDao {

    @Transaction
    suspend fun addTaskCategoryAtEnd(): Long {
        val categoryCount = getCategoryCount()
        return insertTaskCategory(
            TaskCategoryEntity(
                title = "",
                listOrder = categoryCount
            )
        )
    }

    @Query("SELECT * FROM task_categories ORDER BY list_order ASC")
    fun getTaskCategories(): Flow<List<TaskCategoryEntity>>

    @Query("SELECT COUNT(list_order) FROM task_categories")
    suspend fun getCategoryCount(): Int

    @Query("UPDATE task_categories SET title = :titleChange WHERE category_id = :categoryId")
    suspend fun updateTaskCategoryTitle(categoryId: Int, titleChange: String)

    @Insert
    fun insertTaskCategory(taskCategoryEntity: TaskCategoryEntity): Long
}