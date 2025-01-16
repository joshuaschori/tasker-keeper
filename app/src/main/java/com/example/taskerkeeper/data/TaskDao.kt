package com.example.taskerkeeper.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Transaction
    suspend fun addTaskAfter(taskId: Int) {
        val parentId = getParentId(taskId)
        val taskOrder = getTaskOrder(taskId)
        incrementTasks(parentId, taskOrder + 1)
        insertTask(parentId, taskOrder + 1)
    }

    @Transaction
    suspend fun addTaskAfterUnchecked(parentId: Int?) {
        val firstCheckedTaskOrder = getFirstCheckedTaskOrder(parentId)
        if (firstCheckedTaskOrder == null) {
            addTaskAtEnd(parentId)
        } else {
            incrementTasks(parentId, firstCheckedTaskOrder)
            insertTask(parentId, firstCheckedTaskOrder)
        }
    }

    @Transaction
    suspend fun addTaskAtEnd(parentId: Int?) {
        val taskCount = getTaskCount(parentId)
        insertTask(parentId, taskCount)
    }

    @Transaction
    suspend fun markTaskComplete(taskId: Int, autoSort: Boolean) {
        val parentId = getParentId(taskId)
        val taskOrder = getTaskOrder(taskId)
        if (autoSort) {
            val firstCheckedTaskOrder = getFirstCheckedTaskOrder(parentId)
            if (firstCheckedTaskOrder == null) {
                val taskCount = getTaskCount(parentId)
                updateTaskAsChecked(taskId)
                moveTask(parentId, taskOrder, taskCount)
                decrementTasks(parentId, taskOrder + 1)
            } else {
                updateTaskAsChecked(taskId)
                incrementTasks(parentId, firstCheckedTaskOrder)
                moveTask(parentId, taskOrder, firstCheckedTaskOrder)
                decrementTasks(parentId, taskOrder + 1)
            }
        } else {
            updateTaskAsChecked(taskId)
        }
    }

    @Transaction
    suspend fun markTaskIncomplete(taskId: Int, autoSort: Boolean) {
        val parentId = getParentId(taskId)
        val taskOrder = getTaskOrder(taskId)
        if (autoSort) {
            val firstCheckedTaskOrder = getFirstCheckedTaskOrder(parentId)
            if (firstCheckedTaskOrder != null) {
                updateTaskAsUnchecked(taskId)
                incrementTasks(parentId, firstCheckedTaskOrder)
                moveTask(parentId, taskOrder + 1, firstCheckedTaskOrder)
                decrementTasks(parentId, taskOrder + 2)
            }
        } else {
            updateTaskAsUnchecked(taskId)
        }
    }

    @Transaction
    suspend fun removeTask(taskId: Int) {
        val parentId = getParentId(taskId)
        val taskOrder = getTaskOrder(taskId)
        deleteTask(taskId)
        decrementTasks(parentId, taskOrder + 1)
    }

    @Query("UPDATE tasks SET task_order = task_order - 1 WHERE parent_id is :parentId AND task_order >= :taskOrder")
    suspend fun decrementTasks(parentId: Int?, taskOrder: Int)

    @Query("DELETE FROM tasks WHERE task_id = :taskId")
    suspend fun deleteTask(taskId: Int)

    @Query("SELECT task_order FROM tasks WHERE parent_id is :parentId AND is_checked = 1 ORDER BY task_order ASC LIMIT 1")
    suspend fun getFirstCheckedTaskOrder(parentId: Int?): Int?

    @Query("SELECT parent_id FROM tasks WHERE task_id = :taskId")
    suspend fun getParentId(taskId: Int): Int?

    @Query("SELECT * FROM tasks ORDER BY task_order ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(task_order) FROM tasks WHERE parent_id is :parentId")
    suspend fun getTaskCount(parentId: Int?): Int

    @Query("SELECT task_order FROM tasks WHERE task_id = :taskId")
    suspend fun getTaskOrder(taskId: Int): Int

    @Query("UPDATE tasks SET task_order = task_order + 1 WHERE parent_id is :parentId AND task_order >= :taskOrder")
    suspend fun incrementTasks(parentId: Int?, taskOrder: Int)

    @Query("INSERT INTO tasks (task_string, is_checked, is_expanded, parent_id, task_order) VALUES ('', 0, 0, :parentId, :taskOrder)")
    suspend fun insertTask(parentId: Int?, taskOrder: Int)

    @Query("UPDATE tasks SET task_order = :taskOrderTo WHERE task_order = :taskOrderFrom AND parent_id is :parentId")
    suspend fun moveTask(parentId: Int?, taskOrderFrom: Int, taskOrderTo: Int)

    @Query("UPDATE tasks SET is_checked = 1 WHERE task_id = :taskId")
    suspend fun updateTaskAsChecked(taskId: Int)

    @Query("UPDATE tasks SET is_expanded = 1 WHERE task_id = :taskId")
    suspend fun updateTaskAsExpanded(taskId: Int)

    @Query("UPDATE tasks SET is_expanded = 0 WHERE task_id = :taskId")
    suspend fun updateTaskAsMinimized(taskId: Int)

    @Query("UPDATE tasks SET is_checked = 0 WHERE task_id = :taskId")
    suspend fun updateTaskAsUnchecked(taskId: Int)

    @Query("UPDATE tasks SET task_string = :textChange WHERE task_id is :taskId")
    suspend fun updateTaskString(taskId: Int, textChange: String)

    @Query("SELECT is_checked FROM tasks WHERE task_id = :taskId LIMIT 1")
    suspend fun verifyChecked(taskId: Int): Boolean

}