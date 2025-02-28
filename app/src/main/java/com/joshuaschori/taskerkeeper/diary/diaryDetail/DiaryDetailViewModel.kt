package com.joshuaschori.taskerkeeper.diary.diaryDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshuaschori.taskerkeeper.data.diary.DiaryDetailRepository
import com.joshuaschori.taskerkeeper.tasks.tasksMenu.TasksMenuState
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
class DiaryDetailViewModel @Inject constructor(
    private val diaryDetailRepository: DiaryDetailRepository
): ViewModel() {
    private val _uiState: MutableStateFlow<DiaryDetailState> = MutableStateFlow(DiaryDetailState.Loading)
    val uiState: StateFlow<DiaryDetailState> = _uiState.asStateFlow()
    // TODO not being used unless we're emitting something
    /*private val _uiAction: MutableSharedFlow<DiaryDetailAction> = MutableSharedFlow()
    val uiAction: SharedFlow<DiaryDetailAction> = _uiAction.asSharedFlow()*/

    fun listenForDatabaseUpdates() {
        viewModelScope.launch {

        }
    }
}

sealed interface DiaryDetailState {
    data class Content(
        val clearFocusTrigger: Boolean = false,
    ) : DiaryDetailState
    data object Error : DiaryDetailState
    data object Loading : DiaryDetailState
}

sealed interface DiaryDetailAction {
}

typealias DiaryDetailActionHandler = (DiaryDetailAction) -> Unit