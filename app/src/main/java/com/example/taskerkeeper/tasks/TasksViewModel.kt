package com.example.taskerkeeper.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskerkeeper.TaskTreeBuilder
import com.example.taskerkeeper.TaskTreeNode
import com.example.taskerkeeper.data.TaskEntity
import com.example.taskerkeeper.data.TasksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
                val treeList = convertTaskEntityListToTaskTreeNodeList(taskEntityList)
                val taskList = convertTaskTreeNodeListToTaskList(treeList)
                _uiState.update {
                    TaskState.Content(
                        taskList = taskList
                    )
                }
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

sealed interface TaskState {
    data class Content(
        val taskList: List<Task> = emptyList(),
        val isAutoSortCheckedTasks: Boolean = true,
    ) : TaskState
    data object Error : TaskState
    data object Loading : TaskState
}