package com.joshuaschori.taskerkeeper.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CalendarViewModel: ViewModel() {
    private val _uiState: MutableStateFlow<CalendarState> = MutableStateFlow(CalendarState.Loading)
    val uiState: StateFlow<CalendarState> = _uiState.asStateFlow()
    // TODO not being used unless we're emitting something
    /*private val _uiAction: MutableSharedFlow<CalendarAction> = MutableSharedFlow()
    val uiAction: SharedFlow<CalendarAction> = _uiAction.asSharedFlow()*/

    fun listenForDatabaseUpdates() {
        viewModelScope.launch {

        }
    }
}

sealed interface CalendarState {
    data class Content(
        val clearFocusTrigger: Boolean = false,
    ) : CalendarState
    data object Error : CalendarState
    data object Loading : CalendarState
}

sealed interface CalendarAction {
}

typealias CalendarActionHandler = (CalendarAction) -> Unit