package com.joshuaschori.taskerkeeper.data

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TasksRepository @Inject constructor(
    private val db: TaskerKeeperDatabase,
) {
    suspend fun addTaskAfter(taskId: Int): Int {
        val newTaskId = db.taskDao().addTaskAfter(taskId).toInt()

        // TODO pass in remote source into repository that can send firebase data, abstractions to hide and secure
        FirebaseDatabase.getInstance().getReference().child(newTaskId.toString()).setValue("")

        return newTaskId
    }

    suspend fun addTaskAfterUnchecked(parentId: Int?): Int {
        // if task with parentId as taskId is minimized, expand it
        if (parentId != null) {
            if(!db.taskDao().verifyExpanded(parentId)) {
                db.taskDao().updateTaskAsExpanded(parentId)
            }
        }
        return db.taskDao().addTaskAfterUnchecked(parentId).toInt()
    }

    suspend fun addTaskAtEnd(parentId: Int?): Int {
        // if task with parentId as taskId is minimized, expand it
        if (parentId != null) {
            if(!db.taskDao().verifyExpanded(parentId)) {
                db.taskDao().updateTaskAsExpanded(parentId)
            }
        }
        return db.taskDao().addTaskAtEnd(parentId).toInt()
    }

    suspend fun editTask(taskId: Int, textChange: String) {
        db.taskDao().updateTaskString(taskId, textChange)

        // TODO firebase stuff
        FirebaseDatabase.getInstance().getReference()
            .child(Firebase.auth.currentUser!!.uid)
            .child(taskId.toString())
            .setValue(textChange)
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