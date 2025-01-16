package com.example.taskerkeeper.data

import com.example.taskerkeeper.tasks.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TasksRepository @Inject constructor(
    val db: TaskerKeeperDatabase,
) {
    suspend fun addTaskAfter(taskId: Int) {
        db.taskDao().addTaskAfter(taskId)
    }

    suspend fun addTaskAfterUnchecked(parentId: Int?) {
        db.taskDao().addTaskAfterUnchecked(parentId)
    }

    suspend fun addTaskAtEnd(parentId: Int?) {
        db.taskDao().addTaskAtEnd(parentId)
    }

    suspend fun editTask(taskId: Int, textChange: String) {
        db.taskDao().updateTaskString(taskId, textChange)
    }

    suspend fun expandTask(taskId: Int) {
        db.taskDao().updateTaskAsExpanded(taskId)
    }

    fun getAllTasks() = db.taskDao().getAllTasks()

    suspend fun markTaskComplete(taskId: Int, autoSort: Boolean) {
        db.taskDao().markTaskComplete(taskId, autoSort)
    }

    suspend fun markTaskIncomplete(taskId: Int, autoSort: Boolean) {
        db.taskDao().markTaskIncomplete(taskId, autoSort)
    }

    suspend fun minimizeTask(taskId: Int) {
        db.taskDao().updateTaskAsMinimized(taskId)
    }

    suspend fun removeTask(taskId: Int) {
        db.taskDao().removeTask(taskId)
    }

}