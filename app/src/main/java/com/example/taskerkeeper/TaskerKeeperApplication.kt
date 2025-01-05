package com.example.taskerkeeper

import android.app.Application
import androidx.room.Room
import com.example.taskerkeeper.data.TaskerKeeperDatabase
import dagger.Module
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TaskerKeeperApplication: Application() {}