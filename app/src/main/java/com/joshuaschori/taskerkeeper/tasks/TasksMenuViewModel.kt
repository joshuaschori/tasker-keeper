package com.joshuaschori.taskerkeeper.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshuaschori.taskerkeeper.data.TasksRepository
import com.joshuaschori.taskerkeeper.diary.DiaryAction
import com.joshuaschori.taskerkeeper.diary.DiaryState
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
class TasksMenuViewModel @Inject constructor(
    // TODO tasksMenuRepository
    private val tasksRepository: TasksRepository
): ViewModel() {
    private val _uiState: MutableStateFlow<TasksMenuState> = MutableStateFlow(TasksMenuState.Content())
    val uiState: StateFlow<TasksMenuState> = _uiState.asStateFlow()
    private val _uiAction: MutableSharedFlow<TasksMenuAction> = MutableSharedFlow()
    val uiAction: SharedFlow<TasksMenuAction> = _uiAction.asSharedFlow()

    fun listenForDatabaseUpdates() {
        viewModelScope.launch {

        }
    }
}

sealed interface TasksMenuState {
    data class Content(
        val text: String = ""
    ) : TasksMenuState
    data object Error : TasksMenuState
    data object Loading : TasksMenuState
}

sealed interface TasksMenuAction {
}

typealias TasksMenuActionHandler = (TasksMenuAction) -> Unit