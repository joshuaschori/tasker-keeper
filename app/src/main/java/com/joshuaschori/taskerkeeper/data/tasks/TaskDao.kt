package com.joshuaschori.taskerkeeper.data.tasks

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Transaction
    suspend fun addTaskAfter(parentCategoryId: Int, taskId: Int): Long {
        val parentId = getParentTaskId(taskId)
        val taskOrder = getListOrder(taskId)
        incrementTasks(parentCategoryId, parentId, taskOrder + 1)
        return insertTask(
            TaskEntity(
                parentCategoryId = parentCategoryId,
                parentTaskId = parentId,
                description = "",
                listOrder = taskOrder + 1,
                isChecked = false,
                isExpanded = false,
            )
        )
    }

    @Transaction
    suspend fun addTaskAfterUnchecked(parentCategoryId: Int, parentTaskId: Int?): Long {
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
    suspend fun addTaskAtEnd(parentCategoryId: Int, parentId: Int?): Long {
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
    suspend fun markTaskComplete(parentCategoryId: Int, taskId: Int, autoSort: Boolean) {
        val parentId = getParentTaskId(taskId)
        val taskOrder = getListOrder(taskId)
        if (autoSort) {
            val firstCheckedTaskOrder = getFirstCheckedListOrder(parentCategoryId, parentId)
            if (firstCheckedTaskOrder == null) {
                val taskCount = getTaskCount(parentCategoryId, parentId)
                updateTaskAsChecked(taskId)
                moveTask(parentCategoryId, parentId, taskOrder, taskCount)
                decrementTasks(parentCategoryId, parentId, taskOrder + 1)
            } else {
                updateTaskAsChecked(taskId)
                incrementTasks(parentCategoryId, parentId, firstCheckedTaskOrder)
                moveTask(parentCategoryId, parentId, taskOrder, firstCheckedTaskOrder)
                decrementTasks(parentCategoryId, parentId, taskOrder + 1)
            }
        } else {
            updateTaskAsChecked(taskId)
        }
    }

    @Transaction
    suspend fun markTaskIncomplete(parentCategoryId: Int, taskId: Int, autoSort: Boolean) {
        val parentId = getParentTaskId(taskId)
        val taskOrder = getListOrder(taskId)
        if (autoSort) {
            val firstCheckedTaskOrder = getFirstCheckedListOrder(parentCategoryId, parentId)
            if (firstCheckedTaskOrder != null) {
                updateTaskAsUnchecked(taskId)
                incrementTasks(parentCategoryId, parentId, firstCheckedTaskOrder)
                moveTask(parentCategoryId, parentId, taskOrder + 1, firstCheckedTaskOrder)
                decrementTasks(parentCategoryId, parentId, taskOrder + 2)
            }
        } else {
            updateTaskAsUnchecked(taskId)
        }
    }

    @Transaction
    suspend fun removeTask(parentCategoryId: Int, taskId: Int) {
        val parentId = getParentTaskId(taskId)
        val taskOrder = getListOrder(taskId)
        deleteTask(taskId)
        decrementTasks(parentCategoryId, parentId, taskOrder + 1)
    }

    @Query("UPDATE tasks SET list_order = list_order - 1 WHERE parent_category_id is :parentCategoryId AND parent_task_id is :parentTaskId AND list_order >= :listOrder")
    suspend fun decrementTasks(parentCategoryId: Int, parentTaskId: Int?, listOrder: Int)

    @Query("DELETE FROM tasks WHERE task_id = :taskId")
    suspend fun deleteTask(taskId: Int)

    @Query("SELECT list_order FROM tasks WHERE parent_category_id is :parentCategoryId AND parent_task_id is :parentTaskId AND is_checked = 1 ORDER BY list_order ASC LIMIT 1")
    suspend fun getFirstCheckedListOrder(parentCategoryId: Int, parentTaskId: Int?): Int?

    @Query("SELECT parent_task_id FROM tasks WHERE task_id = :taskId")
    suspend fun getParentTaskId(taskId: Int): Int?

    @Query("SELECT * FROM tasks WHERE parent_category_id is :tasksListId ORDER BY list_order ASC")
    fun getTasks(tasksListId: Int): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(list_order) FROM tasks WHERE parent_category_id is :parentCategoryId AND parent_task_id is :parentTaskId")
    suspend fun getTaskCount(parentCategoryId: Int, parentTaskId: Int?): Int

    @Query("SELECT list_order FROM tasks WHERE task_id = :taskId")
    suspend fun getListOrder(taskId: Int): Int

    @Query("UPDATE tasks SET list_order = list_order + 1 WHERE parent_category_id is :parentCategoryId AND parent_task_id is :parentTaskId AND list_order >= :listOrder")
    suspend fun incrementTasks(parentCategoryId: Int, parentTaskId: Int?, listOrder: Int)

    @Query("UPDATE tasks SET list_order = :listOrderTo WHERE parent_category_id is :parentCategoryId AND parent_task_id is :parentTaskId AND list_order = :listOrderFrom")
    suspend fun moveTask(parentCategoryId: Int, parentTaskId: Int?, listOrderFrom: Int, listOrderTo: Int)

    @Query("UPDATE tasks SET is_checked = 1 WHERE task_id = :taskId")
    suspend fun updateTaskAsChecked(taskId: Int)

    @Query("UPDATE tasks SET is_expanded = 1 WHERE task_id = :taskId")
    suspend fun updateTaskAsExpanded(taskId: Int)

    @Query("UPDATE tasks SET is_expanded = 0 WHERE task_id = :taskId")
    suspend fun updateTaskAsMinimized(taskId: Int)

    @Query("UPDATE tasks SET is_checked = 0 WHERE task_id = :taskId")
    suspend fun updateTaskAsUnchecked(taskId: Int)

    @Query("UPDATE tasks SET description = :descriptionChange WHERE task_id is :taskId")
    suspend fun updateDescription(taskId: Int, descriptionChange: String)

    @Query("SELECT is_expanded FROM tasks WHERE task_id = :taskId LIMIT 1")
    suspend fun verifyIsExpanded(taskId: Int): Boolean

    @Query("SELECT is_checked FROM tasks WHERE task_id = :taskId LIMIT 1")
    suspend fun verifyIsChecked(taskId: Int): Boolean

    @Insert
    suspend fun insertTask(taskEntity: TaskEntity): Long

}