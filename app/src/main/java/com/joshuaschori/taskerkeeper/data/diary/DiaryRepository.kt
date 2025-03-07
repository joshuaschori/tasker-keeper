package com.joshuaschori.taskerkeeper.data.diary

import com.joshuaschori.taskerkeeper.data.TaskerKeeperDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepository @Inject constructor(
    private val db: TaskerKeeperDatabase,
) {
    suspend fun addNewDiaryEntry(diaryDate: String): Int {
        return db.diaryDao().addNewDiaryEntry(diaryDate).toInt()
    }

    suspend fun editDiaryText(diaryId: Int, textChange: String) {
        db.diaryDao().editDiaryText(diaryId, textChange)
    }

    fun getDiaryEntries() = db.diaryDao().getDiaryEntries()

    fun getDiaryEntryById(diaryId: Int) = db.diaryDao().getDiaryEntryById(diaryId)
}