package com.joshuaschori.taskerkeeper.tasks.tasksDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshuaschori.taskerkeeper.data.tasks.TaskEntity
import com.joshuaschori.taskerkeeper.data.tasks.TasksDetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksDetailViewModel @Inject constructor(
    private val tasksDetailRepository: TasksDetailRepository
): ViewModel() {
    private val _uiState: MutableStateFlow<TasksDetailState> = MutableStateFlow(TasksDetailState.Loading)
    val uiState: StateFlow<TasksDetailState> = _uiState.asStateFlow()
    // TODO not being used unless we're emitting something
    /*private val _uiAction: MutableSharedFlow<TasksDetailAction> = MutableSharedFlow()
    val uiAction: SharedFlow<TasksDetailAction> = _uiAction.asSharedFlow()*/

    fun addNewTask(selectedTaskId: Int?, parentId: Int?) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                if (selectedTaskId == null && currentState.isAutoSortCheckedTasks) {
                    _uiState.value = currentState.copy(
                        focusTaskId = tasksDetailRepository.addTaskAfterUnchecked(currentState.parentCategoryId, parentId)
                    )
                } else if (selectedTaskId == null) {
                    _uiState.value = currentState.copy(
                        focusTaskId = tasksDetailRepository.addTaskAtEnd(currentState.parentCategoryId, parentId)
                    )
                } else {
                    _uiState.value = currentState.copy(
                        focusTaskId = tasksDetailRepository.addTaskAfter(currentState.parentCategoryId, selectedTaskId)
                    )
                }
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun changeTasksDetailExtensionMode(tasksDetailExtensionMode: TasksDetailExtensionMode) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                _uiState.value = currentState.copy(selectedTasksDetailExtensionMode = tasksDetailExtensionMode)
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun clearFocus() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                _uiState.value = currentState.copy(clearFocusTrigger = true)
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                tasksDetailRepository.removeTask(currentState.parentCategoryId, taskId)
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun editTaskDescription(taskId: Int, descriptionChange: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                tasksDetailRepository.editTaskDescription(taskId, descriptionChange)
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun expandTask(taskId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                tasksDetailRepository.expandTask(taskId)
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun listenForDatabaseUpdates(parentCategoryId: Int) {
        viewModelScope.launch {
            tasksDetailRepository.getTasks(parentCategoryId).collect { taskEntityList ->
                val treeList = convertTaskEntityListToTaskTreeNodeList(taskEntityList)
                val taskList = convertTaskTreeNodeListToTaskList(treeList)
                _uiState.value = TasksDetailState.Content(parentCategoryId, taskList)
            }
        }
    }

    fun markTaskComplete(taskId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                tasksDetailRepository.markTaskComplete(currentState.parentCategoryId, taskId, currentState.isAutoSortCheckedTasks)
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun markTaskIncomplete(taskId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                tasksDetailRepository.markTaskIncomplete(currentState.parentCategoryId, taskId, currentState.isAutoSortCheckedTasks)
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun minimizeTask(taskId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                tasksDetailRepository.minimizeTask(taskId)
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun resetClearFocusTrigger() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                _uiState.value = currentState.copy(clearFocusTrigger = false)
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun resetFocusTrigger() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                _uiState.value = currentState.copy(focusTaskId = null)
            } else {
                _uiState.value = TasksDetailState.Error
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
                parentTaskId = taskEntity.parentTaskId,
                description = taskEntity.description,
                isChecked = taskEntity.isChecked,
                isExpanded = taskEntity.isExpanded,
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

sealed interface TasksDetailState {
    data class Content(
        val parentCategoryId: Int,
        val taskList: List<Task> = emptyList(),
        val selectedTasksDetailExtensionMode: TasksDetailExtensionMode = TasksDetailExtensionMode.NORMAL,
        val clearFocusTrigger: Boolean = false,
        val focusTaskId: Int? = null,
        val isAutoSortCheckedTasks: Boolean = true,
    ) : TasksDetailState
    data object Error : TasksDetailState
    data object Loading: TasksDetailState
}

sealed interface TasksDetailAction {
    data class AddNewTask(val selectedTaskId: Int?, val parentId: Int?): TasksDetailAction
    data class ChangeTasksDetailExtensionMode(val tasksDetailExtensionMode: TasksDetailExtensionMode): TasksDetailAction
    data object ClearFocus: TasksDetailAction
    data class DeleteTask(val taskId: Int): TasksDetailAction
    data class EditTaskDescription(val taskId: Int, val descriptionChange: String): TasksDetailAction
    data class ExpandTask(val taskId: Int): TasksDetailAction
    data class MarkTaskComplete(val taskId: Int): TasksDetailAction
    data class MarkTaskIncomplete(val taskId: Int): TasksDetailAction
    data class MinimizeTask(val taskId: Int): TasksDetailAction
    data object NavigateToTasksMenu: TasksDetailAction
    data object ResetClearFocusTrigger: TasksDetailAction
    data object ResetFocusTrigger: TasksDetailAction
}

typealias TasksDetailActionHandler = (TasksDetailAction) -> Unit