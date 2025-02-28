package com.joshuaschori.taskerkeeper.data.diary

import com.joshuaschori.taskerkeeper.data.TaskerKeeperDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryMenuRepository @Inject constructor(
    private val db: TaskerKeeperDatabase,
) {}