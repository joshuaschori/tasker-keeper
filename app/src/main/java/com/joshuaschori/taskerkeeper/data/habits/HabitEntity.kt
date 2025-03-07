package com.joshuaschori.taskerkeeper.data.habits

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "habits",
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "habit_id") val habitId: Int = 0,
)