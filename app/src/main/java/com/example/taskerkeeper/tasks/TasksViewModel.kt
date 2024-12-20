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
            val taskList: List<Task> =  if (currentState.isAutoSortCheckedSubtasks && subtaskIndex == null) {
                // add new subtask at the end of the unchecked subtasks, before checked subtasks
                List(
                    currentState.taskList.size
                ) {
                    if (it == taskIndex) {
                        var indexForNewSubtask: Int = 0
                        for ((index, subtask) in currentState.taskList[it].subtaskList.withIndex()) {
                            if (index == 0 && subtask.isChecked) {
                                indexForNewSubtask = 0
                                break
                            } else if (index != 0 && subtask.isChecked && !currentState.taskList[it].subtaskList[index - 1].isChecked) {
                                indexForNewSubtask = index
                                break
                            } else {
                                indexForNewSubtask = currentState.taskList[it].subtaskList.size
                            }
                        }
                        val subtaskList: List<Subtask> = List(
                            currentState.taskList[it].subtaskList.size + 1
                        ) { it2 ->
                            if (it2 < indexForNewSubtask) {
                                currentState.taskList[it].subtaskList[it2]
                            } else if (it2 == indexForNewSubtask) {
                                Subtask()
                            } else {
                                currentState.taskList[it].subtaskList[it2 - 1]
                            }
                        }
                        currentState.taskList[it].copy(subtaskList = subtaskList)
                    } else {
                        currentState.taskList[it]
                    }
                }
            } else if (
                subtaskIndex == null
            ) {
                // add new subtask at bottom of list
                List(
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
            } else {
                // add new subtask below the one selected
                List(
                    currentState.taskList.size
                ) {
                    if (it == taskIndex) {
                        val subtaskList: List<Subtask> = List(
                            currentState.taskList[it].subtaskList.size + 1
                        ) { it2 ->
                            if (it2 == subtaskIndex + 1) {
                                Subtask()
                            } else if (it2 > subtaskIndex + 1) {
                                currentState.taskList[it].subtaskList[it2 - 1]
                            } else {
                                currentState.taskList[it].subtaskList[it2]
                            }
                        }
                        currentState.taskList[it].copy(subtaskList = subtaskList)
                    } else {
                        currentState.taskList[it]
                    }
                }
            }
            currentState.copy(taskList = taskList)
        }
    }

    fun addNewTask(taskIndex: Int?) {
        _uiState.update { currentState ->
            val taskList: List<Task> =  if (currentState.isAutoSortCheckedTasks && taskIndex == null) {
                // add new task at the end of the unchecked tasks, before checked subtasks
                List(
                    currentState.taskList.size + 1
                ) {
                    var indexForNewTask: Int = 0
                    for ((index, task) in currentState.taskList.withIndex()) {
                        if (index == 0 && task.isChecked) {
                            indexForNewTask = 0
                            break
                        } else if (index != 0 && task.isChecked && !currentState.taskList[index - 1].isChecked) {
                            indexForNewTask = index
                            break
                        } else {
                            indexForNewTask = currentState.taskList.size
                        }
                    }
                    println(indexForNewTask)
                    if (it < indexForNewTask) {
                        currentState.taskList[it]
                    } else if (it == indexForNewTask) {
                        Task()
                    } else {
                        currentState.taskList[it - 1]
                    }
                }
            } else if (
                taskIndex == null
            ) {
                // add new task at bottom of list
                List(
                    currentState.taskList.size + 1
                ) {
                    if (it == currentState.taskList.size) {
                        Task()
                    } else {
                        currentState.taskList[it]
                    }
                }
            } else {
                // add new task below the one selected
                List(
                    currentState.taskList.size + 1
                ) {
                    if (it == taskIndex + 1) {
                        Task()
                    } else if (it <= taskIndex) {
                        currentState.taskList[it]
                    } else {
                        currentState.taskList[it - 1]
                    }
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
        val isAutoSortCheckedSubtasks: Boolean = true,
        val isAutoSortCheckedTasks: Boolean = true,
    ) : TaskState
    data object Error : TaskState
    data object Loading : TaskState
}