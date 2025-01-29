package com.joshuaschori.taskerkeeper.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TaskEntity::class],
    version = 3
)
abstract class TaskerKeeperDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}