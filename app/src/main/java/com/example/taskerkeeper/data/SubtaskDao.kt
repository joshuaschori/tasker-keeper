package com.example.taskerkeeper.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface SubtaskDao {
    @Query("SELECT * FROM subtasks")
    fun getAll(): List<SubtaskEntity>

    @Query("SELECT * FROM subtasks WHERE subtaskId IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<SubtaskEntity>

    @Query("SELECT * FROM subtasks WHERE subtask_string LIKE :first LIMIT 1")
    fun findByTask(first: String): SubtaskEntity

    @Insert
    fun insertAll(vararg users: SubtaskEntity)

    @Delete
    fun delete(subtask: SubtaskEntity)

    @Update
    fun update(subtask: SubtaskEntity)
}