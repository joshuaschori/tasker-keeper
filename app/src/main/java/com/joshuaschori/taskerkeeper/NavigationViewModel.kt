package com.joshuaschori.taskerkeeper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshuaschori.taskerkeeper.data.diary.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository
): ViewModel() {
    private val _uiState: MutableStateFlow<NavigationState> = MutableStateFlow(NavigationState.Loading)
    val uiState: StateFlow<NavigationState> = _uiState.asStateFlow()
    private val _uiAction: MutableSharedFlow<NavigationAction> = MutableSharedFlow()
    val uiAction: SharedFlow<NavigationAction> = _uiAction.asSharedFlow()

    init {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is NavigationState.Loading) {
                // create diary entry for the day if none exists yet, populate TabState with ID of today's diary entry
                val diaryEntityList = diaryRepository.getDiaryEntries().first()
                val diaryEntityDates = diaryEntityList.map {
                    it.diaryDate
                }
                val dateToday = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val diaryId = if (diaryEntityDates.contains(dateToday)) {
                    diaryEntityList[ diaryEntityDates.indexOfFirst { it.contains(dateToday) } ].diaryId
                } else {
                    diaryRepository.addNewDiaryEntry(dateToday)
                }
                val diaryTabState = TabState.Detail(diaryId)

                // TODO create general tasks category if none exists yet, populate state with id

                // TODO create general habits category if none exists yet, populate state with id


                _uiState.value = NavigationState.Content(
                    diaryTabState = diaryTabState
                )
            } else {
                _uiState.value = NavigationState.Error
            }
        }
    }

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

    fun navigateToDiaryMenu() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is NavigationState.Content) {
                _uiState.value = currentState.copy(diaryTabState = TabState.Menu)
                _uiAction.emit(NavigationAction.ShowDiaryTab(TabState.Menu))
            } else {
                _uiState.value = NavigationState.Error
            }
        }
    }

    fun navigateToDiaryDetail(diaryId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is NavigationState.Content) {
                _uiState.value = currentState.copy(diaryTabState = TabState.Detail(diaryId))
                _uiAction.emit(NavigationAction.ShowDiaryTab(TabState.Detail(diaryId)))
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

    fun navigateToTasksDetail(parentCategoryId: Int) {
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
        val timeForDayToEndSetting: Int? = null,
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