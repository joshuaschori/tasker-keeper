package com.joshuaschori.taskerkeeper.data.tasks

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.joshuaschori.taskerkeeper.data.TaskerKeeperDatabase
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.Task
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val db: TaskerKeeperDatabase,
) {
    // taskCategoryDao functions //
    suspend fun addTaskCategoryAtEnd(): Int {
        val newTaskCategoryId = db.taskCategoryDao().addTaskCategoryAtEnd().toInt()
        return newTaskCategoryId
    }

    suspend fun editTaskCategoryTitle(categoryId: Int, titleChange: String) {
        db.taskCategoryDao().updateTaskCategoryTitle(categoryId, titleChange)
    }

    fun getTaskCategories() = db.taskCategoryDao().getTaskCategories()

    // taskDao functions //
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

    private fun findPreviousTaskAtRequestedTier(aboveTask: Task, taskList: List<Task>, requestedTier: Int): Task? {
        var previousTaskAtRequestedTier: Task? = null
        fun traverse(task: Task) {
            if (requestedTier == task.itemTier) {
                previousTaskAtRequestedTier = task
            } else {
                val parentTask = taskList.find { it.itemId == task.parentItemId }
                if (parentTask == null) {
                    previousTaskAtRequestedTier = null
                } else {
                    traverse(parentTask)
                }
            }
        }
        traverse(aboveTask)
        return previousTaskAtRequestedTier
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

    suspend fun moveTaskTier(
        parentCategoryId: Int,
        thisTask: Task,
        taskList: List<Task>,
        aboveTask: Task?,
        belowTask: Task?,
        requestedTier: Int,
        autoSort: Boolean,
    ) {
        if (aboveTask != null) {
            // if belowTask's tier starts and ends below thisTask's tier, belowTask's parent and listOrder don't need to change
            if (belowTask?.parentItemId == null || (belowTask.itemTier < thisTask.itemTier && requestedTier > thisTask.itemTier)) {
                moveTaskOrder(
                    parentCategoryId = parentCategoryId,
                    thisTask = thisTask,
                    taskList = taskList,
                    taskAboveDestination = aboveTask,
                    requestedTier = requestedTier,
                    autoSort = autoSort
                )
            } else {
                // when below task parentTaskId == thisTask.taskId:
                //  parent and list order change if this task moving to same tier, after finding extended parent if this task moving to higher tier
                // when below task parentTaskId == thisTask.parentTaskId:
                //  parent and list order change if this task becoming it's parent, just list order change if this task moving to higher tier (parent stays the same)
                // above cases both work with same parameters

                val previousTaskAtRequestedTier =
                    findPreviousTaskAtRequestedTier(
                        aboveTask = aboveTask,
                        taskList = taskList,
                        requestedTier = requestedTier
                    )
                val previousTaskAtRequestedTierForBelowTasks =
                    findPreviousTaskAtRequestedTier(
                        aboveTask = aboveTask,
                        taskList = taskList,
                        requestedTier = belowTask.itemTier
                    )

                val destinationParentTaskId = when (requestedTier) {
                    aboveTask.itemTier -> aboveTask.parentItemId
                    aboveTask.itemTier + 1 -> aboveTask.itemId
                    else -> previousTaskAtRequestedTier?.parentItemId
                }
                val destinationListOrder = when (requestedTier) {
                    aboveTask.itemTier -> aboveTask.listOrder + 1
                    aboveTask.itemTier + 1 -> aboveTask.numberOfChildren ?: 0 // aboveTask.numberOfChildren accounting for the possibility of aboveTask being minimized
                    else -> previousTaskAtRequestedTier?.listOrder?.plus(1) ?: 0
                }
                val belowTaskDestinationParentTaskId = when (belowTask.itemTier) {
                    requestedTier -> destinationParentTaskId
                    requestedTier + 1 -> thisTask.itemId
                    else -> previousTaskAtRequestedTierForBelowTasks?.parentItemId ?: aboveTask.itemId
                }
                val belowTaskDestinationListOrder = when (belowTask.itemTier) {
                    requestedTier -> destinationListOrder + 1
                    requestedTier + 1 -> 0
                    else -> previousTaskAtRequestedTierForBelowTasks?.listOrder?.plus(1) ?: 0
                }

                db.taskDao().moveTaskTier(
                    parentCategoryId = parentCategoryId,
                    taskId = thisTask.itemId,
                    currentParentTaskId = thisTask.parentItemId,
                    currentListOrder = thisTask.listOrder,
                    destinationParentTaskId = destinationParentTaskId,
                    destinationListOrder = destinationListOrder,
                    belowTaskCurrentParentTaskId = belowTask.parentItemId,
                    belowTaskCurrentListOrder = belowTask.listOrder,
                    belowTaskDestinationParentTaskId = belowTaskDestinationParentTaskId,
                    belowTaskDestinationListOrder = belowTaskDestinationListOrder,
                    autoSort = autoSort,
                )

            }
        }
    }

    suspend fun moveTaskOrder(
        parentCategoryId: Int,
        thisTask: Task,
        taskList: List<Task>,
        taskAboveDestination: Task?,
        requestedTier: Int,
        autoSort: Boolean,
    ) {
        if (taskAboveDestination == null) {
            when (requestedTier) {
                0 -> db.taskDao().moveTaskOrder(
                    parentCategoryId = parentCategoryId,
                    taskId = thisTask.itemId,
                    currentParentTaskId = thisTask.parentItemId,
                    currentListOrder = thisTask.listOrder,
                    destinationParentTaskId = null,
                    destinationListOrder = 0,
                    autoSort = autoSort
                )
            }
        } else {

            when (requestedTier) {
                taskAboveDestination.itemTier -> {
                    db.taskDao().moveTaskOrder(
                        parentCategoryId = parentCategoryId,
                        taskId = thisTask.itemId,
                        currentParentTaskId = thisTask.parentItemId,
                        currentListOrder = thisTask.listOrder,
                        destinationParentTaskId = taskAboveDestination.parentItemId,
                        destinationListOrder = taskAboveDestination.listOrder + 1,
                        autoSort = autoSort
                    )
                }

                taskAboveDestination.itemTier + 1 -> {
                    db.taskDao().moveTaskOrder(
                        parentCategoryId = parentCategoryId,
                        taskId = thisTask.itemId,
                        currentParentTaskId = thisTask.parentItemId,
                        currentListOrder = thisTask.listOrder,
                        destinationParentTaskId = taskAboveDestination.itemId,
                        destinationListOrder = if (!taskAboveDestination.isExpanded) taskAboveDestination.numberOfChildren ?: 0 else 0,
                        autoSort = autoSort
                    )
                }

                else -> {
                    val previousTaskAtRequestedTier =
                        findPreviousTaskAtRequestedTier(
                            aboveTask = taskAboveDestination,
                            taskList = taskList,
                            requestedTier = requestedTier
                        )
                    if (previousTaskAtRequestedTier != null) {
                        db.taskDao().moveTaskOrder(
                            parentCategoryId = parentCategoryId,
                            taskId = thisTask.itemId,
                            currentParentTaskId = thisTask.parentItemId,
                            currentListOrder = thisTask.listOrder,
                            destinationParentTaskId = previousTaskAtRequestedTier.parentItemId,
                            destinationListOrder = previousTaskAtRequestedTier.listOrder + 1,
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