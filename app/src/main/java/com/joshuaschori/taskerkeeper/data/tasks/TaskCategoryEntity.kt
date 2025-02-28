package com.joshuaschori.taskerkeeper.data.tasks

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "task_categories"
)
data class TaskCategoryEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "category_id") val categoryId: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "list_order") val listOrder: Int,
)