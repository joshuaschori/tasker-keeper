package com.example.taskerkeeper.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Dao
interface TaskDao {

    @Transaction
    suspend fun addTaskAfterUnchecked() {
        val firstCheckedTaskOrder = getFirstCheckedTaskOrder()
        incrementTasksAfterOrder(firstCheckedTaskOrder)
        insertTaskAtOrder(firstCheckedTaskOrder)
    }

    @Transaction
    suspend fun addTaskAtEnd() {
        val taskCount = getTaskCount()
        insertTaskAtOrder(taskCount)
    }

    @Transaction
    suspend fun addTaskAtOrder(taskOrder: Int) {
        incrementTasksAfterOrder(taskOrder)
        val taskList = getAll().first()
        println(taskList)
        insertTaskAtOrder(taskOrder)
    }

    @Transaction
    suspend fun markTaskComplete(taskOrder: Int, autoSort: Boolean) {
        if (autoSort) {
            val firstCheckedTaskOrder = getFirstCheckedTaskOrder()
            if (verifyChecked(firstCheckedTaskOrder)) {
                updateTaskAsChecked(taskOrder)
                incrementTasksAfterOrder(firstCheckedTaskOrder)
                moveTaskAtOrder(taskOrder, firstCheckedTaskOrder)
                decrementTasksAfterOrder(taskOrder + 1)
            } else {
                val taskCount = getTaskCount()
                updateTaskAsChecked(taskOrder)
                moveTaskAtOrder(taskOrder, taskCount)
                decrementTasksAfterOrder(taskOrder + 1)
            }
        } else {
            updateTaskAsChecked(taskOrder)
        }
    }

    @Transaction
    suspend fun markTaskIncomplete(taskOrder: Int, autoSort: Boolean) {
        if (autoSort) {
            val firstCheckedTaskOrder = getFirstCheckedTaskOrder()
            updateTaskAsUnchecked(taskOrder)
            incrementTasksAfterOrder(firstCheckedTaskOrder)
            moveTaskAtOrder(taskOrder + 1, firstCheckedTaskOrder)
            decrementTasksAfterOrder(taskOrder + 1)
        } else {
            updateTaskAsUnchecked(taskOrder)
        }
    }

    @Transaction
    suspend fun removeTaskAtOrder(taskOrder: Int) {
        deleteTaskAtOrder(taskOrder)
        decrementTasksAfterOrder(taskOrder)
    }

    @Query("UPDATE tasks SET task_order = task_order - 1 WHERE task_order >= :taskOrder")
    suspend fun decrementTasksAfterOrder(taskOrder: Int)

    @Query("DELETE FROM tasks WHERE task_order = :taskOrder")
    suspend fun deleteTaskAtOrder(taskOrder: Int)

    @Query("SELECT * FROM tasks ORDER BY task_order ASC")
    fun getAll(): Flow<List<TaskEntity>>

    @Query("SELECT task_order FROM tasks WHERE is_checked = 1 ORDER BY task_order ASC LIMIT 1")
    suspend fun getFirstCheckedTaskOrder(): Int

    @Query("SELECT COUNT(task_order) FROM tasks")
    suspend fun getTaskCount(): Int

    @Query("UPDATE tasks SET task_order = task_order + 1 WHERE task_order >= :taskOrder")
    suspend fun incrementTasksAfterOrder(taskOrder: Int)

    @Query("INSERT INTO tasks (task_string, is_checked, is_expanded, task_order) VALUES ('', 0, 0, :taskOrder)")
    suspend fun insertTaskAtOrder(taskOrder: Int)

    @Query("UPDATE tasks SET task_order = :taskOrderTo WHERE task_order = :taskOrderFrom")
    suspend fun moveTaskAtOrder(taskOrderFrom: Int, taskOrderTo: Int)

    @Query("UPDATE tasks SET is_checked = 1 WHERE task_order = :taskOrder")
    suspend fun updateTaskAsChecked(taskOrder: Int)

    @Query("UPDATE tasks SET is_expanded = 1 WHERE task_order = :taskOrder")
    suspend fun updateTaskAsExpanded(taskOrder: Int)

    @Query("UPDATE tasks SET is_expanded = 0 WHERE task_order = :taskOrder")
    suspend fun updateTaskAsMinimized(taskOrder: Int)

    @Query("UPDATE tasks SET is_checked = 0 WHERE task_order = :taskOrder")
    suspend fun updateTaskAsUnchecked(taskOrder: Int)

    @Query("UPDATE tasks SET task_string = :textChange WHERE task_order = :taskOrder")
    suspend fun updateTaskString(taskOrder: Int, textChange: String)

    @Query("SELECT is_checked FROM tasks WHERE task_order = :taskOrder LIMIT 1")
    suspend fun verifyChecked(taskOrder: Int): Boolean
}