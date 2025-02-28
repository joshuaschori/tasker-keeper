package com.joshuaschori.taskerkeeper.data.tasks

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    // TODO indices in entities?
    /*indices = [
        Index(value = ["list_order"], unique = false),
    ],*/
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = arrayOf("task_id"),
            childColumns = arrayOf("parent_task_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TaskCategoryEntity::class,
            parentColumns = arrayOf("category_id"),
            childColumns = arrayOf("parent_category_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "task_id") val taskId: Int = 0,
    @ColumnInfo(name = "parent_category_id") val parentCategoryId: Int,
    @ColumnInfo(name = "parent_task_id") val parentTaskId: Int?,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "list_order") val listOrder: Int,
    @ColumnInfo(name = "is_checked") val isChecked: Boolean,
    @ColumnInfo(name = "is_expanded") val isExpanded: Boolean,
)