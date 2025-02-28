package com.joshuaschori.taskerkeeper.data.habits

import com.joshuaschori.taskerkeeper.data.TaskerKeeperDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitsMenuRepository @Inject constructor(
    private val db: TaskerKeeperDatabase,
) {}