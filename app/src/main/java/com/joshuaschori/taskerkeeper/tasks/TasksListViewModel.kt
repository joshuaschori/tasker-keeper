package com.joshuaschori.taskerkeeper.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshuaschori.taskerkeeper.TaskTreeBuilder
import com.joshuaschori.taskerkeeper.TaskTreeNode
import com.joshuaschori.taskerkeeper.data.TaskEntity
import com.joshuaschori.taskerkeeper.data.TasksRepository
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
class TasksListViewModel @Inject constructor(
    private val tasksRepository: TasksRepository
): ViewModel() {
    // TODO
    var tasksTabState: String = "Tasks"

    private val _uiState: MutableStateFlow<TasksListState> = MutableStateFlow(TasksListState.Loading)
    val uiState: StateFlow<TasksListState> = _uiState.asStateFlow()
    private val _uiAction: MutableSharedFlow<TasksListAction> = MutableSharedFlow()
    val uiAction: SharedFlow<TasksListAction> = _uiAction.asSharedFlow()

    fun addNewTask(selectedTaskId: Int?, parentId: Int?) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksListState.Content) {
                val nextState = if (selectedTaskId == null && currentState.isAutoSortCheckedTasks) {
                    currentState.copy(
                        focusTaskId = tasksRepository.addTaskAfterUnchecked(parentId)
                    )
                } else if (selectedTaskId == null) {
                    currentState.copy(
                        focusTaskId = tasksRepository.addTaskAtEnd(parentId)
                    )
                } else {
                    currentState.copy(
                        focusTaskId = tasksRepository.addTaskAfter(selectedTaskId)
                    )
                }
                _uiState.value = nextState
            } else {
                _uiState.value = TasksListState.Error
            }
        }
    }

    fun changeTaskExtensionMode(taskExtensionMode: TaskExtensionMode) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksListState.Content) {
                val nextState = currentState.copy(selectedTaskExtensionMode = taskExtensionMode)
                _uiState.value = nextState
            } else {
                _uiState.value = TasksListState.Error
            }
        }
    }

    fun clearFocus() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksListState.Content) {
                val nextState = currentState.copy(clearFocusTrigger = true)
                _uiState.value = nextState
            } else {
                _uiState.value = TasksListState.Error
            }
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksListState.Content) {
                tasksRepository.removeTask(taskId)
            } else {
                _uiState.value = TasksListState.Error
            }
        }
    }

    fun editTask(taskId: Int, textChange: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksListState.Content) {
                tasksRepository.editTask(taskId, textChange)
            } else {
                _uiState.value = TasksListState.Error
            }
        }
    }

    fun expandTask(taskId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksListState.Content) {
                tasksRepository.expandTask(taskId)
            } else {
                _uiState.value = TasksListState.Error
            }
        }
    }

    fun listenForDatabaseUpdates() {
        viewModelScope.launch {
            tasksRepository.getAllTasks().collect { taskEntityList ->
                val treeList = convertTaskEntityListToTaskTreeNodeList(taskEntityList)
                val taskList = convertTaskTreeNodeListToTaskList(treeList)
                val currentState = _uiState.value
                val nextState = if (currentState is TasksListState.Content) {
                    currentState.copy(taskList = taskList)
                }
                else {
                    TasksListState.Content(taskList = taskList)
                }
                _uiState.value = nextState
            }
        }
    }

    fun markTaskComplete(taskId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksListState.Content) {
                tasksRepository.markTaskComplete(taskId, currentState.isAutoSortCheckedTasks)
            } else {
                _uiState.value = TasksListState.Error
            }
        }
    }

    fun markTaskIncomplete(taskId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksListState.Content) {
                tasksRepository.markTaskIncomplete(taskId, currentState.isAutoSortCheckedTasks)
            } else {
                _uiState.value = TasksListState.Error
            }
        }
    }

    fun minimizeTask(taskId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksListState.Content) {
                tasksRepository.minimizeTask(taskId)
            } else {
                _uiState.value = TasksListState.Error
            }
        }
    }

    fun resetClearFocusTrigger() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksListState.Content) {
                val nextState = currentState.copy(clearFocusTrigger = false)
                _uiState.value = nextState
            } else {
                _uiState.value = TasksListState.Error
            }
        }
    }

    fun resetFocusTrigger() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksListState.Content) {
                val nextState = currentState.copy(focusTaskId = null)
                _uiState.value = nextState
            } else {
                _uiState.value = TasksListState.Error
            }
        }
    }

}

fun convertTaskEntityListToTaskTreeNodeList(taskEntityList: List<TaskEntity>): List<TaskTreeNode> {
    val treeBuilder = TaskTreeBuilder()
    taskEntityList.forEach { taskEntity ->
        val taskTreeNode = TaskTreeNode(
            Task(
                taskId = taskEntity.taskId,
                taskString = taskEntity.taskString,
                isChecked = taskEntity.isChecked,
                isExpanded = taskEntity.isExpanded,
                parentId = taskEntity.parentId,
                subtaskList = emptyList()
            )
        )
        treeBuilder.addNode(taskTreeNode)
    }
    val tree = treeBuilder.buildTree()
    return tree
}

fun convertTaskTreeNodeListToTaskList(taskTreeNodeList: List<TaskTreeNode>): List<Task> {
    val taskList: MutableList<Task> = mutableListOf()
    for (node in taskTreeNodeList) {
        taskList.add(node.preOrderTraversal())
    }
    return taskList
}

sealed interface TasksListState {
    data class Content(
        val taskList: List<Task> = emptyList(),
        val selectedTaskExtensionMode: TaskExtensionMode = TaskExtensionMode.NORMAL,
        val clearFocusTrigger: Boolean = false,
        val focusTaskId: Int? = null,
        val isAutoSortCheckedTasks: Boolean = true,
    ) : TasksListState
    data object Error : TasksListState
    data object Loading : TasksListState
}

sealed interface TasksListAction {
    data class AddNewTask(val selectedTaskId: Int?, val parentId: Int?): TasksListAction
    data class ChangeTaskExtensionMode(val taskExtensionMode: TaskExtensionMode): TasksListAction
    data object ClearFocus: TasksListAction
    data class DeleteTask(val taskId: Int): TasksListAction
    data class EditTask(val taskId: Int, val textChange: String): TasksListAction
    data class ExpandTask(val taskId: Int): TasksListAction
    data class MarkTaskComplete(val taskId: Int): TasksListAction
    data class MarkTaskIncomplete(val taskId: Int): TasksListAction
    data class MinimizeTask(val taskId: Int): TasksListAction
    data object NavigateToTasksMenu: TasksListAction
    data object ResetClearFocusTrigger: TasksListAction
    data object ResetFocusTrigger: TasksListAction
}

typealias TaskActionHandler = (TasksListAction) -> Unit