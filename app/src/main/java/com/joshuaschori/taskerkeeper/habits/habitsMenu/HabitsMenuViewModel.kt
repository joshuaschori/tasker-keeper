package com.joshuaschori.taskerkeeper.habits.habitsMenu

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
class HabitsMenuViewModel @Inject constructor(
    private val habitRepository: HabitRepository
): ViewModel() {
    private val _uiState: MutableStateFlow<HabitsMenuState> = MutableStateFlow(HabitsMenuState.Loading)
    val uiState: StateFlow<HabitsMenuState> = _uiState.asStateFlow()
    // TODO not being used unless we're emitting something
    /*private val _uiAction: MutableSharedFlow<HabitsMenuAction> = MutableSharedFlow()
    val uiAction: SharedFlow<HabitsMenuAction> = _uiAction.asSharedFlow()*/

    fun listenForDatabaseUpdates() {
        viewModelScope.launch {

        }
    }
}

sealed interface HabitsMenuState {
    data class Content(
        val clearFocusTrigger: Boolean = false,
    ) : HabitsMenuState
    data object Error : HabitsMenuState
    data object Loading : HabitsMenuState
}

sealed interface HabitsMenuAction {
}

typealias HabitsMenuActionHandler = (HabitsMenuAction) -> Unit