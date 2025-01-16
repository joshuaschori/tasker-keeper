package com.example.taskerkeeper.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.NO_ACTION
import androidx.room.PrimaryKey

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = arrayOf("task_id"),
            childColumns = arrayOf("task_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SubtaskEntity(
    @PrimaryKey val subtaskId: Int,
    @ColumnInfo(name = "subtask_string") val subtaskString: String,
    @ColumnInfo(name = "is_checked") val isChecked: Boolean,
    @ColumnInfo(name = "task_id") val taskId: Int,
    @ColumnInfo(name = "subtask_order") val subtaskOrder: Int,
)