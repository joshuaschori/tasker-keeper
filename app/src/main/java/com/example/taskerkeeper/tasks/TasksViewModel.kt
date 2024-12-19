package com.example.taskerkeeper.tasks

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TasksViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(TaskState.Content())
    val uiState: StateFlow<TaskState> = _uiState.asStateFlow()

    fun addNewSubtask(taskIndex: Int, subtaskIndex: Int?) {
        _uiState.update { currentState ->
            val taskList: List<Task> = List(
                currentState.taskList.size
            ) {
                if (it == taskIndex) {
                    val subtaskList: List<Subtask> = List(
                        currentState.taskList[it].subtaskList.size + 1
                    ) { it2 ->
                        if (it2 < currentState.taskList[it].subtaskList.size) {
                            currentState.taskList[it].subtaskList[it2]
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

    fun addNewTask(taskIndex: Int?) {
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

    fun deleteSubtask(taskIndex: Int, subtaskIndex: Int) {
        _uiState.update { currentState ->
            val taskList: List<Task> = List(
                currentState.taskList.size
            ) {
                if (it == taskIndex) {
                    val subtaskList: List<Subtask> = List(
                        currentState.taskList[it].subtaskList.size - 1
                    ) { it2 ->
                        if (it2 >= subtaskIndex) {
                            currentState.taskList[it].subtaskList[it2 + 1]
                        } else {
                            currentState.taskList[it].subtaskList[it2]
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

    fun deleteTask(taskIndex: Int) {
        _uiState.update {currentState ->
            val taskList: List<Task> = List(
                currentState.taskList.size - 1
            ) {
                if (it >= taskIndex) {
                    currentState.taskList[it + 1]
                } else {
                    currentState.taskList[it]
                }
            }
            currentState.copy(taskList = taskList)
        }
    }

    fun editSubtask(taskIndex: Int, subtaskIndex: Int, textChange: String) {
        _uiState.update {currentState ->
            val taskList: List<Task> = List(
                currentState.taskList.size
            ) {
                if (it == taskIndex) {
                    val subtaskList: List<Subtask> = List(
                        currentState.taskList[it].subtaskList.size
                    ) { it2 ->
                        if (it2 == subtaskIndex) {
                            currentState.taskList[it].subtaskList[it2]
                                .copy(subtaskString = textChange)
                        } else {
                            currentState.taskList[it].subtaskList[it2]
                        }
                    }
                    currentState.taskList[it].copy(
                        subtaskList = subtaskList
                    )
                } else {
                    currentState.taskList[it]
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

    fun expandTask(taskIndex: Int) {
        _uiState.update { currentState ->
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

    fun markSubtaskComplete(taskIndex: Int, subtaskIndex: Int) {
        _uiState.update { currentState ->
            val taskList: List<Task> = List(
                currentState.taskList.size
            ) {
                if (it == taskIndex) {
                    val subtaskList: List<Subtask> = List(
                        currentState.taskList[it].subtaskList.size
                    ) { it2 ->
                        if (it2 == subtaskIndex) {
                            currentState.taskList[it].subtaskList[it2].copy(isChecked = true)
                        } else {
                            currentState.taskList[it].subtaskList[it2]
                        }
                    }
                    currentState.taskList[it].copy(
                        subtaskList = subtaskList
                    )
                } else {
                    currentState.taskList[it]
                }
            }
            currentState.copy(taskList = taskList)
        }
    }

    fun markSubtaskIncomplete(taskIndex: Int, subtaskIndex: Int) {
        _uiState.update { currentState ->
            val taskList: List<Task> = List(
                currentState.taskList.size
            ) {
                if (it == taskIndex) {
                    val subtaskList: List<Subtask> = List(
                        currentState.taskList[it].subtaskList.size
                    ) { it2 ->
                        if (it2 == subtaskIndex) {
                            currentState.taskList[it].subtaskList[it2].copy(isChecked = false)
                        } else {
                            currentState.taskList[it].subtaskList[it2]
                        }
                    }
                    currentState.taskList[it].copy(
                        subtaskList = subtaskList
                    )
                } else {
                    currentState.taskList[it]
                }
            }
            currentState.copy(taskList = taskList)
        }
    }

    fun markTaskComplete(taskIndex: Int) {
        _uiState.update { currentState ->
            val taskList: List<Task> = List(
                currentState.taskList.size
            ) {
                if (it == taskIndex) {
                    currentState.taskList[it].copy(
                        isChecked = true
                    )
                } else {
                    currentState.taskList[it]
                }
            }
            currentState.copy(taskList = taskList)
        }
    }

    fun markTaskIncomplete(taskIndex: Int) {
        _uiState.update { currentState ->
            val taskList: List<Task> = List(
                currentState.taskList.size
            ) {
                if (it == taskIndex) {
                    currentState.taskList[it].copy(
                        isChecked = false
                    )
                } else {
                    currentState.taskList[it]
                }
            }
            currentState.copy(taskList = taskList)
        }
    }

    fun minimizeTask(taskIndex: Int) {
        _uiState.update { currentState ->
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