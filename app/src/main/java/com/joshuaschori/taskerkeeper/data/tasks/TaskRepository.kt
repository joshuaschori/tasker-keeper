package com.joshuaschori.taskerkeeper.data.tasks

import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.joshuaschori.taskerkeeper.data.TaskerKeeperDatabase
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.Task
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailState
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val db: TaskerKeeperDatabase,
) {
    // taskCategoryDao functions
    suspend fun addTaskCategoryAtEnd() {
        db.taskCategoryDao().addTaskCategoryAtEnd()
    }

    suspend fun editTaskCategoryTitle(categoryId: Int, titleChange: String) {
        db.taskCategoryDao().updateTaskCategoryTitle(categoryId, titleChange)
    }

    fun getTaskCategories() = db.taskCategoryDao().getTaskCategories()

    // taskDao functions
    suspend fun addTaskAfter(parentCategoryId: Int, taskId: Int): Int {
        val newTaskId = db.taskDao().addTaskAfter(parentCategoryId, taskId).toInt()

        // TODO pass in remote source into repository that can send firebase data, abstractions to hide and secure
        // TODO firebase here or in dao?
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

    suspend fun moveTaskLayer(
        parentCategoryId: Int,
        thisTask: Task,
        taskList: List<Task>,
        aboveTask: Task?,
        belowTask: Task?,
        requestedLayer: Int,
        autoSort: Boolean,
    ) {
        /* TODO
            val allowedMinimumLayer: Int = if (dragTargetIndex != null && !(targetAboveTask == null && targetBelowTask == null)) {
                DragMode.CHANGE_LAYER -> if (targetAboveTask != null && targetBelowTask != null && targetBelowTask.parentTaskId != null) { targetBelowTask.taskLayer - 1 }
            val allowedMaximumLayer: Int = if (dragTargetIndex != null && !(targetAboveTask == null && targetBelowTask == null)) {
                DragMode.CHANGE_LAYER -> if (targetAboveTask != null) { targetAboveTask.taskLayer + 1 } */

        // TODO three functions: move this task, change parents of all below tasks, and change list order of all below tasks
        // TODO can they all use move function? maybe more efficient to make move all function,

        // TODO change below tasks parent and list order function

        if (aboveTask != null) {
            // TODO should this happen in one transaction???
            /*moveTaskOrder(
                thisTask = thisTask,
                taskAboveDestination = aboveTask,
                requestedLayer = requestedLayer
            )*/
            if (belowTask != null) {
                when (belowTask.parentTaskId) {
                    null -> {
                        // below tasks: no parent or list order change necessary ( if this task was at same layer, the move function will take care of their list order )

                    }
                    thisTask.taskId -> {
                        // below tasks: parent and list order change if this task moving to same layer, after finding extended parent if this task moving to higher layer

                    }
                    thisTask.parentTaskId -> {
                        // below tasks: parent and list order change if this task becoming it's parent, just list order change if this task moving to higher layer (parent stays the same)

                    }
                    else -> {
                        // below tasks: parent and list order change if this task becoming it's parent or moving to same layer, no change if this task moving to higher layer

                    }
                }
            }
        }
}

    suspend fun moveTaskOrder(
        parentCategoryId: Int,
        thisTask: Task,
        taskList: List<Task>,
        taskAboveDestination: Task?,
        requestedLayer: Int,
        autoSort: Boolean,
    ) {
        fun findPreviousTaskAtRequestedLayer(aboveTask: Task, taskList: List<Task>, requestedLayer: Int): Task? {
            var previousTaskAtRequestedLayer: Task? = null
            fun traverse(task: Task) {
                if (requestedLayer == task.taskLayer) {
                    previousTaskAtRequestedLayer = task
                } else {
                    val parentTask = taskList.find { it.taskId == task.parentTaskId }
                    if (parentTask == null) {
                        previousTaskAtRequestedLayer = null
                    } else {
                        traverse(parentTask)
                    }
                }
            }
            traverse(aboveTask)
            return previousTaskAtRequestedLayer
        }
        if (taskAboveDestination == null) {
            when (requestedLayer) {
                0 -> db.taskDao().moveTask(
                    parentCategoryId = parentCategoryId,
                    taskId = thisTask.taskId,
                    parentTaskId = thisTask.parentTaskId,
                    listOrder = thisTask.listOrder,
                    destinationParentTaskId = null,
                    destinationListOrder = 0,
                    autoSort = autoSort
                )
            }
        } else {
            when (requestedLayer) {
                taskAboveDestination.taskLayer -> {
                    db.taskDao().moveTask(
                        parentCategoryId = parentCategoryId,
                        taskId = thisTask.taskId,
                        parentTaskId = thisTask.parentTaskId,
                        listOrder = thisTask.listOrder,
                        destinationParentTaskId = taskAboveDestination.parentTaskId,
                        destinationListOrder = taskAboveDestination.listOrder + 1,
                        autoSort = autoSort
                    )
                }

                taskAboveDestination.taskLayer + 1 -> {
                    db.taskDao().moveTask(
                        parentCategoryId = parentCategoryId,
                        taskId = thisTask.taskId,
                        parentTaskId = thisTask.parentTaskId,
                        listOrder = thisTask.listOrder,
                        destinationParentTaskId = taskAboveDestination.taskId,
                        destinationListOrder = 0,
                        autoSort = autoSort
                    )
                }

                else -> {
                    val previousTaskAtRequestedLayer =
                        findPreviousTaskAtRequestedLayer(
                            aboveTask = taskAboveDestination,
                            taskList = taskList,
                            requestedLayer = requestedLayer
                        )
                    if (previousTaskAtRequestedLayer != null) {
                        db.taskDao().moveTask(
                            parentCategoryId = parentCategoryId,
                            taskId = thisTask.taskId,
                            parentTaskId = thisTask.parentTaskId,
                            listOrder = thisTask.listOrder,
                            destinationParentTaskId = previousTaskAtRequestedLayer.parentTaskId,
                            destinationListOrder = previousTaskAtRequestedLayer.listOrder + 1,
                            autoSort = autoSort
                        )
                    }
                }
            }
        }
    }

    suspend fun removeTask(parentCategoryId: Int, taskId: Int) {
        db.taskDao().removeTask(parentCategoryId, taskId)
    }

}