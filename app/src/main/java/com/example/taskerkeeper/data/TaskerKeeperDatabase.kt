package com.example.taskerkeeper.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.taskerkeeper.tasks.Task

@Database(
    entities = [TaskEntity::class, SubtaskEntity::class],
    version = 2
)
abstract class TaskerKeeperDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun subtaskDao(): SubtaskDao
}