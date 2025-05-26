package com.joshuaschori.taskerkeeper.data.diary

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.joshuaschori.taskerkeeper.data.TaskerKeeperDatabase
import com.joshuaschori.taskerkeeper.network.WeatherApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepository @Inject constructor(
    private val db: TaskerKeeperDatabase,
    private val weatherApi: WeatherApi,
    private val ioDispatcher: CoroutineDispatcher,
    private val dataStore: DataStore<Preferences>
) {
    suspend fun addNewDiaryEntry(diaryDate: String): Int {
        return db.diaryDao().addNewDiaryEntry(diaryDate).toInt()
    }

    suspend fun editDiaryText(diaryId: Int, textChange: String) {
        db.diaryDao().editDiaryText(diaryId, textChange)
    }

    fun getDiaryEntries() = db.diaryDao().getDiaryEntries()

    fun getDiaryEntryById(diaryId: Int) = db.diaryDao().getDiaryEntryById(diaryId)

    suspend fun getForecast() {
        withContext(ioDispatcher) {
            // TODO get user's latitude and longitude

            val result = weatherApi.getForecast(
                latitude = "43.074356",
                longitude = "-87.900384"
            )
            val jsonObject = JSONObject(result)
            val currentTemperature = jsonObject.getJSONObject("currently").getString("temperature")

            println(currentTemperature)

            /*dataStore.edit { preferences ->
                preferences[stringPreferencesKey("current_temperature")] = currently
            }*/

        }
    }

}