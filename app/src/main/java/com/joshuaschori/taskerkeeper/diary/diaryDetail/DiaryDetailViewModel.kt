package com.joshuaschori.taskerkeeper.diary.diaryDetail

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshuaschori.taskerkeeper.data.diary.DiaryRepository
import com.joshuaschori.taskerkeeper.network.WeatherApi
import com.joshuaschori.taskerkeeper.network.WeatherApiService
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TaskListBuilder
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class DiaryDetailViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository
): ViewModel() {
    private val _uiState: MutableStateFlow<DiaryDetailState> = MutableStateFlow(DiaryDetailState.Loading)
    val uiState: StateFlow<DiaryDetailState> = _uiState.asStateFlow()
    // TODO not being used unless we're emitting something
    /*private val _uiAction: MutableSharedFlow<DiaryDetailAction> = MutableSharedFlow()
    val uiAction: SharedFlow<DiaryDetailAction> = _uiAction.asSharedFlow()*/

    fun clearFocus() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is DiaryDetailState.Content) {
                _uiState.value = currentState.copy(clearFocusTrigger = true)
            } else {
                _uiState.value = DiaryDetailState.Error("DiaryDetailViewModel clearFocus")
            }
        }
    }

    fun editDiaryText(diaryId: Int, textChange: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is DiaryDetailState.Content) {
                diaryRepository.editDiaryText(diaryId, textChange)
            } else {
                _uiState.value = DiaryDetailState.Error("DiaryDetailViewModel editDiaryText")
            }
        }
    }

    fun getForecast() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is DiaryDetailState.Content || currentState is DiaryDetailState.Loading) {
                try {
                    diaryRepository.getForecast()
                }
                catch (e: IOException) {
                    // TODO
                }
            } else {
                _uiState.value = DiaryDetailState.Error("DiaryDetailViewModel getForecast")
            }
        }
    }

    fun listenForDatabaseUpdates(diaryId: Int) {
        viewModelScope.launch {
            diaryRepository.getDiaryEntryById(diaryId).collect {
                when (val currentState = _uiState.value) {
                    is DiaryDetailState.Content -> {
                        _uiState.value = currentState.copy(
                            diaryDate = it.diaryDate,
                            diaryText = it.diaryText,
                        )
                    }
                    is DiaryDetailState.Loading -> {
                        _uiState.value = DiaryDetailState.Content(
                            diaryId = diaryId,
                            diaryDate = it.diaryDate,
                            diaryText = it.diaryText,
                            // TODO grab / store in database?
                            forecast = null
                        )
                    }
                    else -> {
                        _uiState.value = DiaryDetailState.Error("DiaryDetailViewModel listenForDatabaseUpdates")
                    }
                }
            }
        }
    }

    fun resetClearFocusTrigger() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is DiaryDetailState.Content) {
                _uiState.value = currentState.copy(clearFocusTrigger = false)
            } else {
                _uiState.value = DiaryDetailState.Error("DiaryDetailViewModel resetClearFocusTrigger")
            }
        }
    }
}

sealed interface DiaryDetailState {
    data class Content(
        val diaryId: Int,
        val diaryDate: String,
        val diaryText: String,
        val forecast: String?,
        val clearFocusTrigger: Boolean = false,
    ) : DiaryDetailState
    data class Error(
        val string: String
    ) : DiaryDetailState
    data object Loading : DiaryDetailState
}

sealed interface DiaryDetailAction {
    data object ClearFocus: DiaryDetailAction
    data class EditDiaryText(val diaryId: Int, val textChange: String): DiaryDetailAction
    data object NavigateToDiaryMenu: DiaryDetailAction
    data object ResetClearFocusTrigger: DiaryDetailAction
}

typealias DiaryDetailActionHandler = (DiaryDetailAction) -> Unit