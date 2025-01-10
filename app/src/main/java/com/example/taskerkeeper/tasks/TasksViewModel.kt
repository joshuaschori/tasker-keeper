package com.example.taskerkeeper.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskerkeeper.data.TaskerKeeperDatabase
import com.example.taskerkeeper.data.TasksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    fun addNewSubtask(selectedTaskIndex: Int, selectedSubtaskIndex: Int?) {

    }

    fun addNewTask(selectedTaskIndex: Int?) {
        val currentState = _uiState.value
        require(currentState is TaskState.Content)
        viewModelScope.launch {
            if (selectedTaskIndex == null && currentState.isAutoSortCheckedTasks) {
                tasksRepository.addTaskAfterUnchecked()
            } else if (selectedTaskIndex == null) {
                tasksRepository.addTaskAtEnd()
            } else {
                val taskList = tasksRepository.getAll().first()
                println(taskList)
                tasksRepository.addTaskAtOrder(selectedTaskIndex + 1)
            }
        }
    }

    fun deleteSubtask(taskIndex: Int, subtaskIndex: Int) {

    }

    fun deleteTask(taskIndex: Int) {
        viewModelScope.launch {
            tasksRepository.removeTaskAtOrder(taskIndex)
        }
    }

    fun editSubtask(taskIndex: Int, subtaskIndex: Int, textChange: String) {

    }

    fun editTask(taskIndex: Int, textChange: String) {
        viewModelScope.launch {
            tasksRepository.editTask(taskIndex, textChange)
        }
    }

    fun expandTask(taskIndex: Int) {
        viewModelScope.launch {
            tasksRepository.expandTask(taskIndex)
        }
    }

    fun markSubtaskComplete(taskIndex: Int, subtaskIndex: Int) {

    }

    fun markSubtaskIncomplete(taskIndex: Int, subtaskIndex: Int) {

    }

    fun markTaskComplete(taskIndex: Int) {
        val currentState = _uiState.value
        require(currentState is TaskState.Content)
        viewModelScope.launch {
            tasksRepository.markTaskComplete(taskIndex, currentState.isAutoSortCheckedTasks)
        }
    }

    fun markTaskIncomplete(taskIndex: Int) {
        val currentState = _uiState.value
        require(currentState is TaskState.Content)
        viewModelScope.launch {
            tasksRepository.markTaskIncomplete(taskIndex, currentState.isAutoSortCheckedTasks)
        }
    }

    fun minimizeTask(taskIndex: Int) {
        viewModelScope.launch {
            tasksRepository.minimizeTask(taskIndex)
        }
    }

    fun listenForDatabaseUpdates() {
        viewModelScope.launch {
            tasksRepository.getAll().collect { taskEntityList ->
                _uiState.update {
                    TaskState.Content(
                        taskList = taskEntityList.sortedBy { it.taskOrder } .map { taskEntity ->
                            Task(
                                // TODO subtask list
                                taskString = taskEntity.taskString,
                                isChecked = taskEntity.isChecked,
                                isExpanded = taskEntity.isExpanded
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
        val isAutoSortCheckedSubtasks: Boolean = true,
        val isAutoSortCheckedTasks: Boolean = true,
    ) : TaskState
    data object Error : TaskState
    data object Loading : TaskState
}