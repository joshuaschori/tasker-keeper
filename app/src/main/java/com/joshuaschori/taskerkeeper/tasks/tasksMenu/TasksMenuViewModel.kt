package com.joshuaschori.taskerkeeper.tasks.tasksMenu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshuaschori.taskerkeeper.data.tasks.TaskCategoryEntity
import com.joshuaschori.taskerkeeper.data.tasks.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksMenuViewModel @Inject constructor(
    private val taskRepository: TaskRepository
): ViewModel() {
    private val _uiState: MutableStateFlow<TasksMenuState> = MutableStateFlow(TasksMenuState.Loading)
    val uiState: StateFlow<TasksMenuState> = _uiState.asStateFlow()
    // TODO not being used unless we're emitting something
    /*private val _uiAction: MutableSharedFlow<TasksMenuAction> = MutableSharedFlow()
    val uiAction: SharedFlow<TasksMenuAction> = _uiAction.asSharedFlow()*/

    fun addNewCategory() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksMenuState.Content) {
                taskRepository.addTaskCategoryAtEnd()
            } else {
                _uiState.value = TasksMenuState.Error
            }
        }
    }

    fun clearFocus() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksMenuState.Content) {
                _uiState.value = currentState.copy(clearFocusTrigger = true)
            } else {
                _uiState.value = TasksMenuState.Error
            }
        }
    }

    fun editCategoryTitle(categoryId: Int, titleChange: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksMenuState.Content) {
                taskRepository.editTaskCategoryTitle(categoryId, titleChange)
            } else {
                _uiState.value = TasksMenuState.Error
            }
        }
    }

    fun listenForDatabaseUpdates() {
        viewModelScope.launch {
            when (val currentState = _uiState.value) {
                is TasksMenuState.Content -> {
                    taskRepository.getTaskCategories().collect {
                        _uiState.value = currentState.copy(taskCategories = it)
                    }
                }
                is TasksMenuState.Loading -> {
                    taskRepository.getTaskCategories().collect {
                        _uiState.value = TasksMenuState.Content(taskCategories = it)
                    }
                }
                else -> {
                    _uiState.value = TasksMenuState.Error
                }
            }
        }
    }

    fun resetClearFocusTrigger() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksMenuState.Content) {
                _uiState.value = currentState.copy(clearFocusTrigger = false)
            } else {
                _uiState.value = TasksMenuState.Error
            }
        }
    }

    fun resetFocusTrigger() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksMenuState.Content) {
                _uiState.value = currentState.copy(focusCategoryId = null)
            } else {
                _uiState.value = TasksMenuState.Error
            }
        }
    }
}

sealed interface TasksMenuState {
    data class Content(
        val taskCategories: List<TaskCategoryEntity>,
        val clearFocusTrigger: Boolean = false,
        val focusCategoryId: Int? = null,
    ) : TasksMenuState
    data object Error : TasksMenuState
    data object Loading : TasksMenuState
}

sealed interface TasksMenuAction {
    data object AddNewCategory: TasksMenuAction
    data class EditCategoryTitle(val categoryId: Int, val titleChange: String): TasksMenuAction
    data object ClearFocus: TasksMenuAction
    data class NavigateToTasksDetail(val categoryId: Int): TasksMenuAction
    data object ResetClearFocusTrigger: TasksMenuAction
    data object ResetFocusTrigger: TasksMenuAction
}

typealias TasksMenuActionHandler = (TasksMenuAction) -> Unit