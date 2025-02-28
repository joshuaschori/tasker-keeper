package com.joshuaschori.taskerkeeper.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.joshuaschori.taskerkeeper.data.diary.DiaryDao
import com.joshuaschori.taskerkeeper.data.habits.HabitCategoryDao
import com.joshuaschori.taskerkeeper.data.habits.HabitDao
import com.joshuaschori.taskerkeeper.data.tasks.TaskDao
import com.joshuaschori.taskerkeeper.data.tasks.TaskEntity
import com.joshuaschori.taskerkeeper.data.tasks.TaskCategoryDao
import com.joshuaschori.taskerkeeper.data.tasks.TaskCategoryEntity

@Database(
    entities = [TaskEntity::class, TaskCategoryEntity::class],
    version = 3
)
abstract class TaskerKeeperDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
    abstract fun habitDao(): HabitDao
    abstract fun habitCategoryDao(): HabitCategoryDao
    abstract fun taskDao(): TaskDao
    abstract fun taskCategoryDao(): TaskCategoryDao
}