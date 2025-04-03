package com.joshuaschori.taskerkeeper.data.tasks

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // TODO reverse auto sort so it goes on bottom of completed instead of top?? is this in multiple places?
    // TODO user option to auto sort to bottom or top of completed tasks

    @Transaction
    suspend fun addTaskAfter(
        parentCategoryId: Int,
        taskId: Int
    ): Long {
        // TODO just pass in parentId and listOrder???
        // TODO autoSort should already be checked if created after checked?
        val parentId = getParentTaskId(taskId)
        val listOrder = getListOrder(taskId)
        incrementTasks(parentCategoryId, parentId, listOrder + 1)
        return insertTask(
            TaskEntity(
                parentCategoryId = parentCategoryId,
                parentTaskId = parentId,
                description = "",
                listOrder = listOrder + 1,
                isChecked = false,
                isExpanded = false,
            )
        )
    }

    @Transaction
    suspend fun addTaskAfterUnchecked(
        parentCategoryId: Int,
        parentTaskId: Int?
    ): Long {
        val firstCheckedListOrder = getFirstCheckedListOrder(parentCategoryId, parentTaskId)
        if (firstCheckedListOrder == null) {
            return addTaskAtEnd(parentCategoryId, parentTaskId)
        } else {
            incrementTasks(parentCategoryId, parentTaskId, firstCheckedListOrder)
            return insertTask(
                TaskEntity(
                    parentCategoryId = parentCategoryId,
                    parentTaskId = parentTaskId,
                    description = "",
                    listOrder = firstCheckedListOrder,
                    isChecked = false,
                    isExpanded = false,
                )
            )
        }
    }

    @Transaction
    suspend fun addTaskAtEnd(
        parentCategoryId: Int,
        parentId: Int?
    ): Long {
        // TODO just pass count in
        val taskCount = getTaskCount(parentCategoryId, parentId)
        return insertTask(
            TaskEntity(
                parentCategoryId = parentCategoryId,
                parentTaskId = parentId,
                description = "",
                listOrder = taskCount,
                isChecked = false,
                isExpanded = false,
            )
        )
    }

    @Transaction
    suspend fun markTaskComplete(
        parentCategoryId: Int,
        taskId: Int,
        autoSort: Boolean
    ) {
        // TODO just pass in parentId and listOrder???
        val parentId = getParentTaskId(taskId)
        val listOrder = getListOrder(taskId)
        if (autoSort) {
            val firstCheckedListOrder = getFirstCheckedListOrder(parentCategoryId, parentId)
            if (firstCheckedListOrder == null) {
                val taskCount = getTaskCount(parentCategoryId, parentId)
                updateTaskAsChecked(taskId)
                moveTaskByListOrder(parentCategoryId, parentId, listOrder, taskCount)
                decrementTasks(parentCategoryId, parentId, listOrder + 1)
            } else {
                updateTaskAsChecked(taskId)
                incrementTasks(parentCategoryId, parentId, firstCheckedListOrder)
                moveTaskByListOrder(parentCategoryId, parentId, listOrder, firstCheckedListOrder)
                decrementTasks(parentCategoryId, parentId, listOrder + 1)
            }
        } else {
            updateTaskAsChecked(taskId)
        }
    }

    @Transaction
    suspend fun markTaskIncomplete(
        parentCategoryId: Int,
        taskId: Int,
        autoSort: Boolean
    ) {
        // TODO just pass in parentId and listOrder???
        val parentId = getParentTaskId(taskId)
        val listOrder = getListOrder(taskId)
        if (autoSort) {
            val firstCheckedListOrder = getFirstCheckedListOrder(parentCategoryId, parentId)
            if (firstCheckedListOrder != null) {
                updateTaskAsUnchecked(taskId)
                incrementTasks(parentCategoryId, parentId, firstCheckedListOrder)
                moveTaskByListOrder(parentCategoryId, parentId, listOrder + 1, firstCheckedListOrder)
                decrementTasks(parentCategoryId, parentId, listOrder + 2)
            }
        } else {
            updateTaskAsUnchecked(taskId)
        }
    }

    @Transaction
    suspend fun moveTaskLayer(
        parentCategoryId: Int,
        taskId: Int,
        currentParentTaskId: Int?,
        currentListOrder: Int,
        destinationParentTaskId: Int?,
        destinationListOrder: Int,
        belowTaskCurrentParentTaskId: Int?,
        belowTaskCurrentListOrder: Int,
        belowTaskDestinationParentTaskId: Int?,
        belowTaskDestinationListOrder: Int,
        autoSort: Boolean
    ) {
        if (destinationParentTaskId == taskId) {
            println("TaskRepository Error, trying to make a task its own parent")
            return
        }
        val getBelowTasksCount: Int = getTaskCountStartingFromListOrder(
            parentCategoryId = parentCategoryId,
            parentTaskId = belowTaskCurrentParentTaskId,
            startingListOrder = belowTaskCurrentListOrder
        )

        // if this task and below task start with same parent, or if moveTaskOrder moves this task
        // to same parent as below task, then moveTaskOrder will change below tasks' list_orders
        val listOrderDifferential = if (currentParentTaskId == belowTaskCurrentParentTaskId) {
            -1
        } else if (destinationParentTaskId == belowTaskCurrentParentTaskId) {
            1
        } else { 0 }

        var incrementingBelowTaskListOrder: Int = belowTaskCurrentListOrder + listOrderDifferential
        var incrementingBelowTaskListOrderDestination: Int = belowTaskDestinationListOrder


        moveTaskOrder(
            parentCategoryId = parentCategoryId,
            taskId = taskId,
            currentParentTaskId = currentParentTaskId,
            currentListOrder = currentListOrder,
            destinationParentTaskId = destinationParentTaskId,
            destinationListOrder = destinationListOrder,
            autoSort = autoSort
        )

        // TODO if parent is the same, then list orders will conflict while this is processing?

        repeat (getBelowTasksCount) {
            updateListOrderByListOrder(
                currentParentTaskId = belowTaskCurrentParentTaskId,
                currentListOrder = incrementingBelowTaskListOrder,
                destinationListOrder = -1
            )
            if (belowTaskCurrentParentTaskId != belowTaskDestinationParentTaskId) {
                updateParentByListOrder(
                    currentParentTaskId = belowTaskCurrentParentTaskId,
                    currentListOrder = -1,
                    destinationParentTaskId = belowTaskDestinationParentTaskId
                )
            }
            updateListOrderByListOrder(
                currentParentTaskId = belowTaskDestinationParentTaskId,
                currentListOrder = -1,
                destinationListOrder = incrementingBelowTaskListOrderDestination
            )
            incrementingBelowTaskListOrder++
            incrementingBelowTaskListOrderDestination++
        }

        // if destination parent is not expanded, expand
        if (belowTaskDestinationParentTaskId != null) {
            if (!verifyIsExpanded(belowTaskDestinationParentTaskId)) {
                updateTaskAsExpanded(belowTaskDestinationParentTaskId)
            }
        }

        // TODO if autosort, and moved task is completed, move it to top of completed task under parent if it isn't already in the completed tasks
        // TODO if autosort, delay?
    }

    @Transaction
    suspend fun moveTaskOrder(
        parentCategoryId: Int,
        taskId: Int,
        currentParentTaskId: Int?,
        currentListOrder: Int,
        destinationParentTaskId: Int?,
        destinationListOrder: Int,
        autoSort: Boolean
    ) {
        if (destinationParentTaskId == taskId) {
            println("TaskRepository Error, trying to make a task its own parent")
            return
        }
        if (currentParentTaskId == destinationParentTaskId) {
            if (destinationListOrder < currentListOrder) {
                incrementTasks(
                    parentCategoryId = parentCategoryId,
                    parentTaskId = currentParentTaskId,
                    listOrder = destinationListOrder
                )
                updateListOrder(
                    taskId = taskId,
                    listOrder = -1
                )
                updateParent(
                    taskId = taskId,
                    parentTaskId = destinationParentTaskId
                )
                updateListOrder(
                    taskId = taskId,
                    listOrder = destinationListOrder
                )
                decrementTasks(
                    parentCategoryId = parentCategoryId,
                    parentTaskId = currentParentTaskId,
                    listOrder = currentListOrder + 1
                )
            }
            else if (destinationListOrder > currentListOrder) {
                incrementTasks(
                    parentCategoryId = parentCategoryId,
                    parentTaskId = currentParentTaskId,
                    listOrder = destinationListOrder
                )
                updateListOrder(
                    taskId = taskId,
                    listOrder = -1
                )
                updateParent(
                    taskId = taskId,
                    parentTaskId = destinationParentTaskId
                )
                updateListOrder(
                    taskId = taskId,
                    listOrder = destinationListOrder
                )
                decrementTasks(
                    parentCategoryId = parentCategoryId,
                    parentTaskId = currentParentTaskId,
                    listOrder = currentListOrder
                )
            }
        } else {
            incrementTasks(
                parentCategoryId = parentCategoryId,
                parentTaskId = destinationParentTaskId,
                listOrder = destinationListOrder
            )
            updateListOrder(
                taskId = taskId,
                listOrder = -1
            )
            updateParent(
                taskId = taskId,
                parentTaskId = destinationParentTaskId
            )
            updateListOrder(
                taskId = taskId,
                listOrder = destinationListOrder
            )
            decrementTasks(
                parentCategoryId = parentCategoryId,
                parentTaskId = currentParentTaskId,
                listOrder = currentListOrder
            )
        }
        // if destination parent is not expanded, expand
        if (destinationParentTaskId != null) {
            if (!verifyIsExpanded(destinationParentTaskId)) {
                updateTaskAsExpanded(destinationParentTaskId)
            }
        }
        // TODO if autosort, and moved task is completed, move it to top of completed task under parent if it isn't already in the completed tasks
        // TODO if autosort, delay?
    }

    @Transaction
    suspend fun removeTask(parentCategoryId: Int, taskId: Int) {
        val parentId = getParentTaskId(taskId)
        val listOrder = getListOrder(taskId)
        deleteTask(taskId)
        decrementTasks(parentCategoryId, parentId, listOrder + 1)
    }

    // TODO figure out how to make this work with indexes, do one by one instead of setting all at once?
    @Query("UPDATE tasks SET list_order = list_order - 1 WHERE parent_category_id is :parentCategoryId AND parent_task_id is :parentTaskId AND list_order >= :listOrder")
    suspend fun decrementTasks(parentCategoryId: Int, parentTaskId: Int?, listOrder: Int)

    @Query("DELETE FROM tasks WHERE task_id = :taskId")
    suspend fun deleteTask(taskId: Int)

    @Query("SELECT list_order FROM tasks WHERE parent_category_id is :parentCategoryId AND parent_task_id is :parentTaskId AND is_checked = 1 ORDER BY list_order ASC LIMIT 1")
    suspend fun getFirstCheckedListOrder(parentCategoryId: Int, parentTaskId: Int?): Int?

    @Query("SELECT parent_task_id FROM tasks WHERE task_id = :taskId")
    suspend fun getParentTaskId(taskId: Int): Int?

    @Query("SELECT * FROM tasks WHERE parent_category_id is :parentCategoryId ORDER BY list_order ASC")
    fun getTasks(parentCategoryId: Int): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(list_order) FROM tasks WHERE parent_category_id is :parentCategoryId AND parent_task_id is :parentTaskId")
    suspend fun getTaskCount(parentCategoryId: Int, parentTaskId: Int?): Int

    @Query("SELECT COUNT(list_order) FROM tasks WHERE parent_category_id is :parentCategoryId AND parent_task_id is :parentTaskId AND list_order >= :startingListOrder")
    suspend fun getTaskCountStartingFromListOrder(parentCategoryId: Int, parentTaskId: Int?, startingListOrder: Int): Int

    @Query("SELECT COUNT(list_order) FROM tasks WHERE parent_category_id is :parentCategoryId AND parent_task_id is :parentTaskId")
    suspend fun getTaskCountWithParentTaskId(parentCategoryId: Int, parentTaskId: Int?): Int

    @Query("SELECT list_order FROM tasks WHERE task_id = :taskId")
    suspend fun getListOrder(taskId: Int): Int

    @Query("UPDATE tasks SET list_order = list_order + 1 WHERE parent_category_id is :parentCategoryId AND parent_task_id is :parentTaskId AND list_order >= :listOrder")
    suspend fun incrementTasks(parentCategoryId: Int, parentTaskId: Int?, listOrder: Int)

    @Query("UPDATE tasks SET list_order = :listOrderTo WHERE parent_category_id is :parentCategoryId AND parent_task_id is :parentTaskId AND list_order = :listOrderFrom")
    suspend fun moveTaskByListOrder(parentCategoryId: Int, parentTaskId: Int?, listOrderFrom: Int, listOrderTo: Int)

    @Query("UPDATE tasks SET description = :descriptionChange WHERE task_id is :taskId")
    suspend fun updateDescription(taskId: Int, descriptionChange: String)

    @Query("UPDATE tasks SET list_order = :listOrder WHERE task_id = :taskId")
    suspend fun updateListOrder(taskId: Int, listOrder: Int)

    @Query("UPDATE tasks SET list_order = :destinationListOrder WHERE parent_task_id = :currentParentTaskId AND list_order = :currentListOrder")
    suspend fun updateListOrderByListOrder(currentParentTaskId: Int?, currentListOrder: Int, destinationListOrder: Int)

    @Query("UPDATE tasks SET parent_task_id = :parentTaskId WHERE task_id = :taskId")
    suspend fun updateParent(taskId: Int, parentTaskId: Int?)

    @Query("UPDATE tasks SET parent_task_id = :destinationParentTaskId WHERE parent_task_id = :currentParentTaskId AND list_order = :currentListOrder")
    suspend fun updateParentByListOrder(currentParentTaskId: Int?, currentListOrder: Int, destinationParentTaskId: Int?)

    @Query("UPDATE tasks SET is_checked = 1 WHERE task_id = :taskId")
    suspend fun updateTaskAsChecked(taskId: Int)

    @Query("UPDATE tasks SET is_expanded = 1 WHERE task_id = :taskId")
    suspend fun updateTaskAsExpanded(taskId: Int)

    @Query("UPDATE tasks SET is_expanded = 0 WHERE task_id = :taskId")
    suspend fun updateTaskAsMinimized(taskId: Int)

    @Query("UPDATE tasks SET is_checked = 0 WHERE task_id = :taskId")
    suspend fun updateTaskAsUnchecked(taskId: Int)

    @Query("SELECT is_expanded FROM tasks WHERE task_id = :taskId LIMIT 1")
    suspend fun verifyIsExpanded(taskId: Int): Boolean

    @Query("SELECT is_checked FROM tasks WHERE task_id = :taskId LIMIT 1")
    suspend fun verifyIsChecked(taskId: Int): Boolean

    @Insert
    suspend fun insertTask(taskEntity: TaskEntity): Long

}