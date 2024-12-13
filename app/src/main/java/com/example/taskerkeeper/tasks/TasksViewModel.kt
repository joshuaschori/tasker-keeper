package com.example.taskerkeeper.tasks

import androidx.lifecycle.ViewModel
import com.example.taskerkeeper.Subtask
import com.example.taskerkeeper.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TasksViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(TaskState.Content())
    val uiState: StateFlow<TaskState> = _uiState.asStateFlow()

    fun addNewSubtask(taskIndex: Int) {
        _uiState.update { currentState ->
            val taskList: List<Task> = List(
                currentState.taskList.size
            ) {
                if (it == taskIndex) {
                    val subtaskList: List<Subtask> = List(
                        currentState.taskList[taskIndex].subtaskList.size + 1
                    ) {subtaskIndex ->
                        if (subtaskIndex < currentState.taskList[taskIndex].subtaskList.size) {
                            currentState.taskList[taskIndex].subtaskList[subtaskIndex]
                        } else {
                            Subtask()
                        }
                    }
                    currentState.taskList[it].copy(subtaskList = subtaskList)
                } else {
                    currentState.taskList[it]
                }
            }
            currentState.copy(taskList = taskList)
        }
    }

    fun addNewTask() {
        _uiState.update { currentState ->
            val taskList: List<Task> = List(
                currentState.taskList.size + 1
            ) {
                if (it < currentState.taskList.size) {
                    currentState.taskList[it]
                } else {
                    Task()
                }
            }
            currentState.copy(taskList = taskList)
        }
    }

    fun editSubtask(taskIndex: Int, subtaskIndex: Int, textChange: String) {}

    fun editTask(taskIndex: Int, textChange: String) {
        _uiState.update {currentState ->
            val taskList: List<Task> = List(
                currentState.taskList.size
            ) {
                if (it == taskIndex) {
                    currentState.taskList[it].copy(
                        taskString = textChange
                    )
                } else {
                    currentState.taskList[it]
                }
            }
            currentState.copy(taskList = taskList)
        }
    }

    fun expandTask(taskIndex: Int) {
        _uiState.update {currentState ->
            val taskList: List<Task> = List(
                currentState.taskList.size
            ) {
                if (it == taskIndex) {
                    currentState.taskList[it].copy(
                        isExpanded = true
                    )
                } else {
                    currentState.taskList[it]
                }
            }
            currentState.copy(taskList = taskList)
        }
    }

    fun markSubtaskComplete(taskIndex: Int, subtaskIndex: Int) {}

    fun markSubtaskIncomplete(taskIndex: Int, subtaskIndex: Int) {}

    fun markTaskComplete(taskIndex: Int) {
        _uiState.update {currentState ->
            val taskList: List<Task> = List(
                currentState.taskList.size
            ) {
                if (it == taskIndex) {
                    currentState.taskList[it].copy(
                        isChecked = !currentState.taskList[it].isChecked
                    )
                } else {
                    currentState.taskList[it]
                }
            }
            currentState.copy(taskList = taskList)
        }
    }

    fun markTaskIncomplete(taskIndex: Int) {}

    fun minimizeTask(taskIndex: Int) {
        _uiState.update {currentState ->
            val taskList: List<Task> = List(
                currentState.taskList.size
            ) {
                if (it == taskIndex) {
                    currentState.taskList[it].copy(
                        isExpanded = false
                    )
                } else {
                    currentState.taskList[it]
                }
            }
            currentState.copy(taskList = taskList)
        }
    }

    fun removeTask(taskIndex: Int) {}

    fun removeSubtask(taskIndex: Int, subtaskIndex: Int) {}

}

sealed interface TaskState {
    data class Content(
        val taskList: List<Task> = List(4) {
            Task()
        },
    ) : TaskState
    data object Error : TaskState
    data object Loading : TaskState
}