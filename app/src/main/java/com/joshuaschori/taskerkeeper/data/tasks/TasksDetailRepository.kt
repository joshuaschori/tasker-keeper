package com.joshuaschori.taskerkeeper.data.tasks

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.joshuaschori.taskerkeeper.data.TaskerKeeperDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TasksDetailRepository @Inject constructor(
    private val db: TaskerKeeperDatabase,
) {
    suspend fun addTaskAfter(parentCategoryId: Int, taskId: Int): Int {
        val newTaskId = db.taskDao().addTaskAfter(parentCategoryId, taskId).toInt()

        // TODO pass in remote source into repository that can send firebase data, abstractions to hide and secure
        FirebaseDatabase.getInstance().getReference()
            .child(newTaskId.toString()).setValue("")

        return newTaskId
    }

    suspend fun addTaskAfterUnchecked(parentCategoryId: Int, parentTaskId: Int?): Int {
        // if task with parentId as taskId is minimized, expand it
        if (parentTaskId != null) {
            if(!db.taskDao().verifyIsExpanded(parentTaskId)) {
                db.taskDao().updateTaskAsExpanded(parentTaskId)
            }
        }
        return db.taskDao().addTaskAfterUnchecked(parentCategoryId, parentTaskId).toInt()
    }

    suspend fun addTaskAtEnd(parentCategoryId: Int, parentTaskId: Int?): Int {
        // if task with parentId as taskId is minimized, expand it
        if (parentTaskId != null) {
            if(!db.taskDao().verifyIsExpanded(parentTaskId)) {
                db.taskDao().updateTaskAsExpanded(parentTaskId)
            }
        }
        return db.taskDao().addTaskAtEnd(parentCategoryId, parentTaskId).toInt()
    }

    suspend fun editTaskDescription(taskId: Int, descriptionChange: String) {
        db.taskDao().updateDescription(taskId, descriptionChange)

        // TODO firebase stuff
        FirebaseDatabase.getInstance().getReference()
            .child(Firebase.auth.currentUser!!.uid)
            .child(taskId.toString())
            .setValue(descriptionChange)
    }

    suspend fun expandTask(taskId: Int) {
        db.taskDao().updateTaskAsExpanded(taskId)
    }

    fun getTasks(parentCategoryId: Int) = db.taskDao().getTasks(parentCategoryId)

    suspend fun markTaskComplete(parentCategoryId: Int, taskId: Int, autoSort: Boolean) {
        db.taskDao().markTaskComplete(parentCategoryId, taskId, autoSort)
    }

    suspend fun markTaskIncomplete(parentCategoryId: Int, taskId: Int, autoSort: Boolean) {
        db.taskDao().markTaskIncomplete(parentCategoryId, taskId, autoSort)
    }

    suspend fun minimizeTask(taskId: Int) {
        db.taskDao().updateTaskAsMinimized(taskId)
    }

    suspend fun removeTask(parentCategoryId: Int, taskId: Int) {
        db.taskDao().removeTask(parentCategoryId, taskId)
    }

}