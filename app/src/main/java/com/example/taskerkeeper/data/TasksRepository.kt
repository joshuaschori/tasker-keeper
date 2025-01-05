package com.example.taskerkeeper.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TasksRepository @Inject constructor(
    database: TaskerKeeperDatabase,
    subtasksRepository: SubtasksRepository
) {}