package com.joshuaschori.taskerkeeper.di

import android.content.Context
import androidx.room.Room
import com.joshuaschori.taskerkeeper.data.TaskerKeeperDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TaskerKeeperDatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext
        applicationContext: Context
    ): TaskerKeeperDatabase {
        return Room.databaseBuilder(
            applicationContext,
            TaskerKeeperDatabase::class.java, "tasker-keeper-database"
        ).fallbackToDestructiveMigration().build()
    }
}