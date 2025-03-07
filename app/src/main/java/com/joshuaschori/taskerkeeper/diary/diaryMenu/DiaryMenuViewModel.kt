package com.joshuaschori.taskerkeeper.diary.diaryMenu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshuaschori.taskerkeeper.data.diary.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiaryMenuViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository
): ViewModel() {
    private val _uiState: MutableStateFlow<DiaryMenuState> = MutableStateFlow(DiaryMenuState.Loading)
    val uiState: StateFlow<DiaryMenuState> = _uiState.asStateFlow()
    // TODO not being used unless we're emitting something
    /*private val _uiAction: MutableSharedFlow<DiaryMenuAction> = MutableSharedFlow()
    val uiAction: SharedFlow<DiaryMenuAction> = _uiAction.asSharedFlow()*/

    fun listenForDatabaseUpdates() {
        viewModelScope.launch {

        }
    }
}

sealed interface DiaryMenuState {
    data class Content(
        val clearFocusTrigger: Boolean = false,
    ) : DiaryMenuState
    data object Error : DiaryMenuState
    data object Loading : DiaryMenuState
}

sealed interface DiaryMenuAction {
}

typealias DiaryMenuActionHandler = (DiaryMenuAction) -> Unit