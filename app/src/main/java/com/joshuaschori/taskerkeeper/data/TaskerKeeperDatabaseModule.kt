package com.joshuaschori.taskerkeeper.data

import android.content.Context
import androidx.room.Room
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