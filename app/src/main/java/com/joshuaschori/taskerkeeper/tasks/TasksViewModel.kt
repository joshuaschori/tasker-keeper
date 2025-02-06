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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val tasksRepository: TasksRepository
): ViewModel() {
    private val _uiState: MutableStateFlow<TaskState> = MutableStateFlow(TaskState.Loading)
    val uiState: StateFlow<TaskState> = _uiState.asStateFlow()
    private val _uiAction: MutableSharedFlow<TaskAction> = MutableSharedFlow()
    val uiAction: SharedFlow<TaskAction> = _uiAction.asSharedFlow()

    fun addNewTask(selectedTaskId: Int?, parentId: Int?) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TaskState.Content) {
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
                _uiState.value = TaskState.Error
            }
        }
    }

    fun clearFocusTaskId() {
        _uiState.update { currentState ->
            require(currentState is TaskState.Content)
            currentState.copy(focusTaskId = null)
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

    fun listenForDatabaseUpdates() {
        viewModelScope.launch {
            tasksRepository.getAllTasks().collect { taskEntityList ->
                val treeList = convertTaskEntityListToTaskTreeNodeList(taskEntityList)
                val taskList = convertTaskTreeNodeListToTaskList(treeList)
                val currentState = _uiState.value
                val nextState = if (currentState is TaskState.Content) {
                    currentState.copy(taskList = taskList)
                }
                else {
                    TaskState.Content(taskList = taskList)
                }
                _uiState.value = nextState
            }
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

    // TODO finish
    fun requestFocus(taskId: Int) {
        //TaskAction.PopBackstack
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

sealed interface TaskState {
    data class Content(
        val taskList: List<Task> = emptyList(),
        val isAutoSortCheckedTasks: Boolean = true,
        val focusTaskId: Int? = null,
    ) : TaskState
    data object Error : TaskState
    data object Loading : TaskState
}

sealed interface TaskAction {
    data class AddNewTask(val selectedTaskId: Int?, val parentId: Int?): TaskAction
    data class DeleteTask(val taskId: Int): TaskAction
    data class EditTask(val taskId: Int, val textChange: String): TaskAction
    data class ExpandTask(val taskId: Int): TaskAction
    data class MarkTaskComplete(val taskId: Int): TaskAction
    data class MarkTaskIncomplete(val taskId: Int): TaskAction
    data class MinimizeTask(val taskId: Int): TaskAction
    // TODO
    data class RequestFocus(val taskId: Int): TaskAction
}

typealias TaskActionHandler = (TaskAction) -> Unit