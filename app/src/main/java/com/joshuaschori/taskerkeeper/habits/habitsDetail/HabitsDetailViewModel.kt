package com.joshuaschori.taskerkeeper.habits.habitsDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshuaschori.taskerkeeper.data.habits.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HabitsDetailViewModel @Inject constructor(
    private val habitRepository: HabitRepository
): ViewModel() {
    private val _uiState: MutableStateFlow<HabitsDetailState> = MutableStateFlow(HabitsDetailState.Loading)
    val uiState: StateFlow<HabitsDetailState> = _uiState.asStateFlow()
    // TODO not being used unless we're emitting something
    /*private val _uiAction: MutableSharedFlow<HabitsDetailAction> = MutableSharedFlow()
    val uiAction: SharedFlow<HabitsDetailAction> = _uiAction.asSharedFlow()*/

    fun listenForDatabaseUpdates() {
        viewModelScope.launch {

        }
    }
}

sealed interface HabitsDetailState {
    data class Content(
        val clearFocusTrigger: Boolean = false,
    ) : HabitsDetailState
    data object Error : HabitsDetailState
    data object Loading : HabitsDetailState
}

sealed interface HabitsDetailAction {
}

typealias HabitsDetailActionHandler = (HabitsDetailAction) -> Unit