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

    private fun findPreviousTaskAtRequestedLayer(aboveTask: Task, taskList: List<Task>, requestedLayer: Int): Task? {
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
        if (aboveTask != null) {
            // if belowTask's layer starts and ends below thisTask's layer, belowTask's parent and listOrder don't need to change
            if (belowTask?.parentTaskId == null || (belowTask.taskLayer < thisTask.taskLayer && requestedLayer > thisTask.taskLayer)) {
                moveTaskOrder(
                    parentCategoryId = parentCategoryId,
                    thisTask = thisTask,
                    taskList = taskList,
                    taskAboveDestination = aboveTask,
                    requestedLayer = requestedLayer,
                    autoSort = autoSort
                )
            } else {
                // when below task parentTaskId == thisTask.taskId:
                //  parent and list order change if this task moving to same layer, after finding extended parent if this task moving to higher layer
                // when below task parentTaskId == thisTask.parentTaskId:
                //  parent and list order change if this task becoming it's parent, just list order change if this task moving to higher layer (parent stays the same)
                // above cases both work with same parameters

                // TODO account for above task being minimized
                //  if destination list order calculated number of tasks with that parent first, that would work

                val previousTaskAtRequestedLayer =
                    findPreviousTaskAtRequestedLayer(
                        aboveTask = aboveTask,
                        taskList = taskList,
                        requestedLayer = requestedLayer
                    )
                val previousTaskAtRequestedLayerForBelowTasks =
                    findPreviousTaskAtRequestedLayer(
                        aboveTask = aboveTask,
                        taskList = taskList,
                        requestedLayer = belowTask.taskLayer
                    )

                val destinationParentTaskId = when (requestedLayer) {
                    aboveTask.taskLayer -> aboveTask.parentTaskId
                    aboveTask.taskLayer + 1 -> aboveTask.taskId
                    else -> previousTaskAtRequestedLayer?.parentTaskId
                }
                val destinationListOrder = when (requestedLayer) {
                    aboveTask.taskLayer -> aboveTask.listOrder + 1
                    aboveTask.taskLayer + 1 -> aboveTask.numberOfChildren ?: 0 // aboveTask.numberOfChildren accounting for the possibility of aboveTask being minimized
                    else -> previousTaskAtRequestedLayer?.listOrder?.plus(1) ?: 0
                }
                val belowTaskDestinationParentTaskId = when (belowTask.taskLayer) {
                    requestedLayer -> destinationParentTaskId
                    requestedLayer + 1 -> thisTask.taskId
                    else -> previousTaskAtRequestedLayerForBelowTasks?.parentTaskId ?: aboveTask.taskId
                }
                val belowTaskDestinationListOrder = when (belowTask.taskLayer) {
                    requestedLayer -> destinationListOrder + 1
                    requestedLayer + 1 -> 0
                    else -> previousTaskAtRequestedLayerForBelowTasks?.listOrder?.plus(1) ?: 0
                }

                db.taskDao().moveTaskLayer(
                    parentCategoryId = parentCategoryId,
                    taskId = thisTask.taskId,
                    currentParentTaskId = thisTask.parentTaskId,
                    currentListOrder = thisTask.listOrder,
                    destinationParentTaskId = destinationParentTaskId,
                    destinationListOrder = destinationListOrder,
                    belowTaskCurrentParentTaskId = belowTask.parentTaskId,
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
        requestedLayer: Int,
        autoSort: Boolean,
    ) {
        if (taskAboveDestination == null) {
            when (requestedLayer) {
                0 -> db.taskDao().moveTaskOrder(
                    parentCategoryId = parentCategoryId,
                    taskId = thisTask.taskId,
                    currentParentTaskId = thisTask.parentTaskId,
                    currentListOrder = thisTask.listOrder,
                    destinationParentTaskId = null,
                    destinationListOrder = 0,
                    autoSort = autoSort
                )
            }
        } else {

            // TODO
            /*val destinationParentTaskId = when (requestedLayer) {
                taskAboveDestination.taskLayer -> taskAboveDestination.parentTaskId
                taskAboveDestination.taskLayer + 1 -> taskAboveDestination.taskId
                else -> findPreviousTaskAtRequestedLayer(
                    aboveTask = taskAboveDestination,
                    taskList = taskList,
                    requestedLayer = requestedLayer
                )?.parentTaskId
            }
            val destinationListOrder = when (requestedLayer) {
                taskAboveDestination.taskLayer -> taskAboveDestination.listOrder + 1
                taskAboveDestination.taskLayer + 1 -> 0
                else -> findPreviousTaskAtRequestedLayer(
                    aboveTask = taskAboveDestination,
                    taskList = taskList,
                    requestedLayer = requestedLayer
                )?.listOrder?.plus(1)
            }*/

            when (requestedLayer) {
                // TODO this would also work in else?
                taskAboveDestination.taskLayer -> {
                    db.taskDao().moveTaskOrder(
                        parentCategoryId = parentCategoryId,
                        taskId = thisTask.taskId,
                        currentParentTaskId = thisTask.parentTaskId,
                        currentListOrder = thisTask.listOrder,
                        destinationParentTaskId = taskAboveDestination.parentTaskId,
                        destinationListOrder = taskAboveDestination.listOrder + 1,
                        autoSort = autoSort
                    )
                }

                // TODO if above task is minimized, this moves task correctly to listOrder 0, but
                //  might be nice to move it to end. numberOfSubtasks Int maybe in TaskListBuilder?
                //  might also be able to help make Task's subtaskList null / emptyList() logic clearer
                taskAboveDestination.taskLayer + 1 -> {
                    db.taskDao().moveTaskOrder(
                        parentCategoryId = parentCategoryId,
                        taskId = thisTask.taskId,
                        currentParentTaskId = thisTask.parentTaskId,
                        currentListOrder = thisTask.listOrder,
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
                        db.taskDao().moveTaskOrder(
                            parentCategoryId = parentCategoryId,
                            taskId = thisTask.taskId,
                            currentParentTaskId = thisTask.parentTaskId,
                            currentListOrder = thisTask.listOrder,
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