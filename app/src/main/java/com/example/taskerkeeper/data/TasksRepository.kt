package com.example.taskerkeeper.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TasksRepository @Inject constructor(
    val db: TaskerKeeperDatabase,
    subtasksRepository: SubtasksRepository
) {
    suspend fun addTaskAfterUnchecked() {
        db.taskDao().addTaskAfterUnchecked()
    }

    suspend fun addTaskAtEnd() {
        db.taskDao().addTaskAtEnd()
    }

    suspend fun addTaskAtOrder(taskOrder: Int) {
        db.taskDao().addTaskAtOrder(taskOrder)
    }

    suspend fun editTask(taskOrder: Int, textChange: String) {
        db.taskDao().updateTaskString(taskOrder, textChange)
    }

    suspend fun expandTask(taskOrder: Int) {
        db.taskDao().updateTaskAsExpanded(taskOrder)
    }

    fun getAll() = db.taskDao().getAll()

    suspend fun markTaskComplete(taskOrder: Int, autoSort: Boolean) {
        db.taskDao().markTaskComplete(taskOrder, autoSort)
    }

    suspend fun markTaskIncomplete(taskOrder: Int, autoSort: Boolean) {
        db.taskDao().markTaskIncomplete(taskOrder, autoSort)
    }

    suspend fun minimizeTask(taskOrder: Int) {
        db.taskDao().updateTaskAsMinimized(taskOrder)
    }

    suspend fun removeTaskAtOrder(taskOrder: Int) {
        db.taskDao().removeTaskAtOrder(taskOrder)
    }

}