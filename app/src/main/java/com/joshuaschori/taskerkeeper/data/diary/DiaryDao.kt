package com.joshuaschori.taskerkeeper.data.diary

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.joshuaschori.taskerkeeper.data.tasks.TaskCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    @Transaction
    suspend fun addNewDiaryEntry(diaryDate: String): Long {
        return insertDiaryEntry(
            DiaryEntity(
                diaryDate = diaryDate,
                diaryText = "",
            )
        )
    }

    @Query("UPDATE diary_entries SET diary_text = :textChange WHERE diary_id = :diaryId")
    suspend fun editDiaryText(diaryId: Int, textChange: String)

    @Query("SELECT * FROM diary_entries")
    fun getDiaryEntries(): Flow<List<DiaryEntity>>

    @Query("SELECT * FROM diary_entries WHERE diary_id = :diaryId")
    fun getDiaryEntryById(diaryId: Int): Flow<DiaryEntity>

    @Insert
    suspend fun insertDiaryEntry(diaryEntity: DiaryEntity): Long

}