package com.joshuaschori.taskerkeeper.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshuaschori.taskerkeeper.data.tasks.tasksDetail.TasksDetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val tasksDetailRepository: TasksDetailRepository
): ViewModel() {
    private val _uiState: MutableStateFlow<DiaryState> = MutableStateFlow(DiaryState.Loading)
    val uiState: StateFlow<DiaryState> = _uiState.asStateFlow()
    private val _uiAction: MutableSharedFlow<DiaryAction> = MutableSharedFlow()
    val uiAction: SharedFlow<DiaryAction> = _uiAction.asSharedFlow()

    fun listenForDatabaseUpdates() {
        viewModelScope.launch {
            // TODO diaryRepository
            /*diaryRepository.getAllTasks().collect {
                val currentState = _uiState.value
                val nextState = if (currentState is DiaryState.Content) {
                    currentState.copy()
                }
                else {
                    DiaryState.Content()
                }
                _uiState.value = nextState
            }*/
        }
    }
}

sealed interface DiaryState {
    data class Content(
        val text: String = ""
    ) : DiaryState
    data object Error : DiaryState
    data object Loading : DiaryState
}

sealed interface DiaryAction {
}

typealias DiaryActionHandler = (DiaryAction) -> Unit