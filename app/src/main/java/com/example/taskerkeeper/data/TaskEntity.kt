package com.example.taskerkeeper.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["task_order"], unique = false)],
    /*foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = arrayOf("task_id"),
            childColumns = arrayOf("parent_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]*/
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "task_id") val taskId: Int = 0,
    @ColumnInfo(name = "task_string") val taskString: String,
    @ColumnInfo(name = "is_checked") val isChecked: Boolean,
    @ColumnInfo(name = "is_expanded") val isExpanded: Boolean,
    @ColumnInfo(name = "parent_id") val parentId: Int?,
    @ColumnInfo(name = "task_order") val taskOrder: Int,
)