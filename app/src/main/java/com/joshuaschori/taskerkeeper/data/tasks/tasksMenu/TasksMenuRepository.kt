package com.joshuaschori.taskerkeeper.data.tasks.tasksMenu

import com.joshuaschori.taskerkeeper.data.TaskerKeeperDatabase
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TasksMenuRepository @Inject constructor(
    private val db: TaskerKeeperDatabase,
) {
    suspend fun addTaskCategoryAtEnd() {
        db.tasksListDao().addTaskCategoryAtEnd()
    }

    suspend fun editTaskCategoryTitle(categoryId: Int, titleChange: String) {
        db.tasksListDao().updateTaskCategoryTitle(categoryId, titleChange)
    }

    fun getTaskCategories() = db.tasksListDao().getTaskCategories()
}