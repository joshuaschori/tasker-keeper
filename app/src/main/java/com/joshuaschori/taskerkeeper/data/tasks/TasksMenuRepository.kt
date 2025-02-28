package com.joshuaschori.taskerkeeper.data.tasks

import com.joshuaschori.taskerkeeper.data.TaskerKeeperDatabase
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TasksMenuRepository @Inject constructor(
    private val db: TaskerKeeperDatabase,
) {
    suspend fun addTaskCategoryAtEnd() {
        db.taskCategoryDao().addTaskCategoryAtEnd()
    }

    suspend fun editTaskCategoryTitle(categoryId: Int, titleChange: String) {
        db.taskCategoryDao().updateTaskCategoryTitle(categoryId, titleChange)
    }

    fun getTaskCategories() = db.taskCategoryDao().getTaskCategories()
}