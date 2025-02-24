package com.joshuaschori.taskerkeeper.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.joshuaschori.taskerkeeper.data.tasks.tasksDetail.TaskDao
import com.joshuaschori.taskerkeeper.data.tasks.tasksDetail.TaskEntity
import com.joshuaschori.taskerkeeper.data.tasks.tasksMenu.TaskCategoryDao
import com.joshuaschori.taskerkeeper.data.tasks.tasksMenu.TaskCategoryEntity

@Database(
    entities = [TaskEntity::class, TaskCategoryEntity::class],
    version = 3
)
abstract class TaskerKeeperDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun tasksListDao(): TaskCategoryDao
}