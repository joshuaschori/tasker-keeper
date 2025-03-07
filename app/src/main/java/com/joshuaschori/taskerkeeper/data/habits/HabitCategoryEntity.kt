package com.joshuaschori.taskerkeeper.data.habits

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_categories",
)
data class HabitCategoryEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "habit_category_id") val habitCategoryId: Int = 0,
)