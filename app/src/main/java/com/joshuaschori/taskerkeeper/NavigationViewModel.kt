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

//TODO change names to navigation
class NavigationViewModel: ViewModel() {
    private val _uiState: MutableStateFlow<NavigationState> = MutableStateFlow(NavigationState.Content())
    val uiState: StateFlow<NavigationState> = _uiState.asStateFlow()
    private val _uiAction: MutableSharedFlow<NavigationAction> = MutableSharedFlow()
    val uiAction: SharedFlow<NavigationAction> = _uiAction.asSharedFlow()

    fun changeBottomNavState(bottomNavState: BottomNavState) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is NavigationState.Content) {
                when (bottomNavState) {
                    BottomNavState.HABITS -> _uiAction.emit(NavigationAction.ShowHabitsTab(currentState.habitsTabState))
                    BottomNavState.TASKS -> _uiAction.emit(NavigationAction.ShowTasksTab(currentState.tasksTabState))
                    BottomNavState.DIARY -> _uiAction.emit(NavigationAction.ShowDiaryTab(currentState.diaryTabState))
                    BottomNavState.CALENDAR -> _uiAction.emit(NavigationAction.ShowCalendarTab)
                }
                _uiState.value = currentState.copy(bottomNavState = bottomNavState)
            } else {
                _uiState.value = NavigationState.Error
            }
        }
    }

    fun navigateToTasksMenu() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is NavigationState.Content) {
                _uiState.value = currentState.copy(tasksTabState = TabState.Menu)
                _uiAction.emit(NavigationAction.ShowTasksTab(TabState.Menu))
            } else {
                _uiState.value = NavigationState.Error
            }
        }
    }

    fun navigateToTasksDetail(
        parentCategoryId: Int
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is NavigationState.Content) {
                _uiState.value = currentState.copy(tasksTabState = TabState.Detail(parentCategoryId))
                _uiAction.emit(NavigationAction.ShowTasksTab(TabState.Detail(parentCategoryId)))
            } else {
                _uiState.value = NavigationState.Error
            }
        }
    }
}

sealed interface NavigationState {
    data class Content(
        val bottomNavState: BottomNavState = BottomNavState.TASKS,
        val habitsTabState: TabState = TabState.Menu,
        val tasksTabState: TabState = TabState.Menu,
        val diaryTabState: TabState = TabState.Menu,
        // TODO today's date and time?
    ) : NavigationState
    data object Error : NavigationState
    data object Loading : NavigationState
}

sealed interface NavigationAction {
    data class ChangeBottomNavState(val bottomNavState: BottomNavState): NavigationAction
    data class NavigateToTasksDetail(val categoryId: Int): NavigationAction
    data object NavigateToTasksMenu: NavigationAction
    data object ShowCalendarTab: NavigationAction
    data class ShowDiaryTab(val diaryTabState: TabState): NavigationAction
    data class ShowHabitsTab(val habitsTabState: TabState): NavigationAction
    data class ShowTasksTab(val tasksTabState: TabState): NavigationAction
}

typealias NavigationActionHandler = (NavigationAction) -> Unit