package com.example.taskerkeeper.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskerkeeper.data.TaskerKeeperDatabase
import com.example.taskerkeeper.data.TasksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val tasksRepository: TasksRepository
): ViewModel() {
    private val _uiState: MutableStateFlow<TaskState> = MutableStateFlow(TaskState.Loading)
    val uiState: StateFlow<TaskState> = _uiState.asStateFlow()

    fun addNewTask(selectedTaskId: Int?, parentId: Int?) {
        val currentState = _uiState.value
        require(currentState is TaskState.Content)
        viewModelScope.launch {
            if (selectedTaskId == null && currentState.isAutoSortCheckedTasks) {
                tasksRepository.addTaskAfterUnchecked(parentId)
            } else if (selectedTaskId == null) {
                tasksRepository.addTaskAtEnd(parentId)
            } else {
                tasksRepository.addTaskAfter(selectedTaskId)
            }
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            tasksRepository.removeTask(taskId)
        }
    }

    fun editTask(taskId: Int, textChange: String) {
        viewModelScope.launch {
            tasksRepository.editTask(taskId, textChange)
        }
    }

    fun expandTask(taskId: Int) {
        viewModelScope.launch {
            tasksRepository.expandTask(taskId)
        }
    }

    fun markTaskComplete(taskId: Int) {
        val currentState = _uiState.value
        require(currentState is TaskState.Content)
        viewModelScope.launch {
            tasksRepository.markTaskComplete(taskId, currentState.isAutoSortCheckedTasks)
        }
    }

    fun markTaskIncomplete(taskId: Int) {
        val currentState = _uiState.value
        require(currentState is TaskState.Content)
        viewModelScope.launch {
            tasksRepository.markTaskIncomplete(taskId, currentState.isAutoSortCheckedTasks)
        }
    }

    fun minimizeTask(taskId: Int) {
        viewModelScope.launch {
            tasksRepository.minimizeTask(taskId)
        }
    }

    fun listenForDatabaseUpdates() {
        viewModelScope.launch {
            tasksRepository.getAllTasks().collect { taskEntityList ->
                _uiState.update {
                    TaskState.Content(
                        taskList = taskEntityList.filter {
                            it.parentId == null
                        }.map { taskEntity ->
                            Task(
                                taskId = taskEntity.taskId,
                                taskString = taskEntity.taskString,
                                isChecked = taskEntity.isChecked,
                                isExpanded = taskEntity.isExpanded,
                                parentId = taskEntity.parentId,
                                subtaskList = taskEntityList.filter {
                                    it.parentId == taskEntity.taskId
                                }.map {
                                    Task(
                                        taskId = it.taskId,
                                        taskString = it.taskString,
                                        isChecked = it.isChecked,
                                        isExpanded = it.isExpanded,
                                        parentId = it.parentId,
                                        subtaskList = emptyList()
                                    )
                                }
                            )
                        }
                    )
                }
            }
        }
    }

}

sealed interface TaskState {
    data class Content(
        val taskList: List<Task> = emptyList(),
        val isAutoSortCheckedTasks: Boolean = true,
    ) : TaskState
    data object Error : TaskState
    data object Loading : TaskState
}