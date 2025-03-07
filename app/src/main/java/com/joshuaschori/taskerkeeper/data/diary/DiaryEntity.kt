package com.joshuaschori.taskerkeeper.data.diary

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.joshuaschori.taskerkeeper.data.tasks.TaskCategoryEntity
import com.joshuaschori.taskerkeeper.data.tasks.TaskEntity

@Entity(
    tableName = "diary_entries",
)
data class DiaryEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "diary_id") val diaryId: Int = 0,
    @ColumnInfo(name = "diary_date") val diaryDate: String,
    @ColumnInfo(name = "diary_text") val diaryText: String,
)