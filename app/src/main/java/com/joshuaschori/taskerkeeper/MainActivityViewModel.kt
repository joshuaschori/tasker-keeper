package com.joshuaschori.taskerkeeper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivityViewModel: ViewModel() {
    private val _uiState: MutableStateFlow<MainActivityState> = MutableStateFlow(MainActivityState.Content())
    val uiState: StateFlow<MainActivityState> = _uiState.asStateFlow()
    private val _uiAction: MutableSharedFlow<MainActivityAction> = MutableSharedFlow()
    val uiAction: SharedFlow<MainActivityAction> = _uiAction.asSharedFlow()

    fun changeBottomNavState(bottomNavState: BottomNavState) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MainActivityState.Content) {
                when (bottomNavState) {
                    BottomNavState.HABITS -> _uiAction.emit(MainActivityAction.ShowDiaryTab)
                    BottomNavState.TASKS -> _uiAction.emit(MainActivityAction.ShowTasksTab(currentState.tasksTabState))
                    BottomNavState.DIARY -> _uiAction.emit(MainActivityAction.ShowDiaryTab)
                    BottomNavState.CALENDAR -> _uiAction.emit(MainActivityAction.ShowDiaryTab)
                }
                _uiState.value = currentState.copy(bottomNavState = bottomNavState)
            } else {
                _uiState.value = MainActivityState.Error
            }
        }
    }

    fun navigateToTasksMenu() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MainActivityState.Content) {
                _uiState.value = currentState.copy(tasksTabState = TasksTabState.Menu)
                _uiAction.emit(MainActivityAction.ShowTasksTab(TasksTabState.Menu))
            } else {
                _uiState.value = MainActivityState.Error
            }
        }
    }

    fun navigateToTasksDetail(
        parentCategoryId: Int
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MainActivityState.Content) {
                _uiState.value = currentState.copy(tasksTabState = TasksTabState.Detail(parentCategoryId))
                _uiAction.emit(MainActivityAction.ShowTasksTab(TasksTabState.Detail(parentCategoryId)))
            } else {
                _uiState.value = MainActivityState.Error
            }
        }
    }
}

sealed interface MainActivityState {
    data class Content(
        val bottomNavState: BottomNavState = BottomNavState.TASKS,
        val tasksTabState: TasksTabState = TasksTabState.Menu,
    ) : MainActivityState
    data object Error : MainActivityState
    data object Loading : MainActivityState
}

sealed interface MainActivityAction {
    data class ChangeBottomNavState(val bottomNavState: BottomNavState): MainActivityAction
    data object ShowDiaryTab: MainActivityAction
    data class NavigateToTasksDetail(val categoryId: Int): MainActivityAction
    data object NavigateToTasksMenu: MainActivityAction
    data class ShowTasksTab(val tasksTabState: TasksTabState): MainActivityAction
}

typealias MainActivityActionHandler = (MainActivityAction) -> Unit