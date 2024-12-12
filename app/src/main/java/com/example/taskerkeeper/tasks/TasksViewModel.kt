package com.example.taskerkeeper.tasks

import androidx.lifecycle.ViewModel
import com.example.taskerkeeper.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TasksViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(TaskState.Content())
    val uiState: StateFlow<TaskState> = _uiState.asStateFlow()

    fun addNewTask() {
        _uiState.update { currentState ->
            val taskList: List<Task> = List(
                currentState.taskList.size + 1
            ) {
                if (it < currentState.taskList.size) {
                    currentState.taskList[it]
                } else {
                    Task(
                        taskString = "Item $it",
                        checkedState = false,
                    )
                }
            }
            currentState.copy(taskList = taskList)
        }
    }

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

    fun markTaskComplete(taskIndex: Int) {
        _uiState.update {currentState ->
            val taskList: List<Task> = List(
                currentState.taskList.size
            ) {
                if (it == taskIndex) {
                    currentState.taskList[it].copy(
                        checkedState = !currentState.taskList[it].checkedState
                    )
                } else {
                    currentState.taskList[it]
                }
            }
            currentState.copy(taskList = taskList)
        }
    }

    fun markTaskIncomplete(taskIndex: Int) {}

    fun removeTask() {}
    fun addSubtask() {}
    fun editSubtask() {}
    fun removeSubtask() {}
    fun markSubtaskComplete() {}
    fun markSubtaskIncomplete() {}
}

sealed interface TaskState {
    data class Content(
        val taskList: List<Task> = List(4) {
            Task(
                taskString = "",
                checkedState = false,
            )
        },
    ) : TaskState
    data object Error : TaskState
    data object Loading : TaskState
}