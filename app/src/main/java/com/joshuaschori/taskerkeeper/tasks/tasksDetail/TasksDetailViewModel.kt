package com.joshuaschori.taskerkeeper.tasks.tasksDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshuaschori.taskerkeeper.DragHandler
import com.joshuaschori.taskerkeeper.data.tasks.TaskEntity
import com.joshuaschori.taskerkeeper.data.tasks.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository
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
                        focusTaskId = taskRepository.addTaskAfterUnchecked(currentState.parentCategoryId, parentId)
                    )
                } else if (selectedTaskId == null) {
                    _uiState.value = currentState.copy(
                        focusTaskId = taskRepository.addTaskAtEnd(currentState.parentCategoryId, parentId)
                    )
                } else {
                    _uiState.value = currentState.copy(
                        focusTaskId = taskRepository.addTaskAfter(currentState.parentCategoryId, selectedTaskId)
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
                taskRepository.removeTask(currentState.parentCategoryId, taskId)
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun editTaskDescription(taskId: Int, descriptionChange: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                taskRepository.editTaskDescription(taskId, descriptionChange)
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun expandTask(taskId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                taskRepository.expandTask(taskId)
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun listenForDatabaseUpdates(parentCategoryId: Int) {
        viewModelScope.launch {
            taskRepository.getTasks(parentCategoryId).collect { taskEntityList ->
                val treeList = convertTaskEntityListToTaskTreeNodeList(taskEntityList)
                val taskList = convertTaskTreeNodeListToTaskList(treeList)
                val currentState = _uiState.value
                if (currentState is TasksDetailState.Content) {
                    _uiState.value = currentState.copy(taskList = taskList)
                } else {
                    _uiState.value = TasksDetailState.Content(parentCategoryId, taskList)
                }
            }
        }
    }

    fun markTaskComplete(taskId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                taskRepository.markTaskComplete(currentState.parentCategoryId, taskId, currentState.isAutoSortCheckedTasks)
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun markTaskIncomplete(taskId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                taskRepository.markTaskIncomplete(currentState.parentCategoryId, taskId, currentState.isAutoSortCheckedTasks)
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun minimizeTask(taskId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                taskRepository.minimizeTask(taskId)
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun rearrangeTasks(
        taskId: Int,
        aboveDestinationTask: Task,
        aboveDestinationTaskLayer: Int,
        belowDestinationTask: Task,
        belowDestinationTaskLayer: Int,
        requestedLayer: Int,
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                // if placing your task at requested layer would break up the bottom task from the upper parent
                // must put task at below task's layer, making above task this task's parent
                if (belowDestinationTask.parentTaskId == aboveDestinationTask.taskId) {
                    taskRepository.rearrangeTasks(
                        parentCategoryId = currentState.parentCategoryId,
                        taskId = taskId,
                        parentTaskId = aboveDestinationTask.taskId,
                        listOrder = 0,
                        autoSort = currentState.isAutoSortCheckedTasks
                    )
                }
                // task can either be put at the same layer, or one layer higher, making it the subtask of the above task
                else if (belowDestinationTask.parentTaskId == aboveDestinationTask.parentTaskId) {
                    // if requesting higher layer, make this task the first subtask of above task
                    if (requestedLayer > aboveDestinationTaskLayer) {
                        taskRepository.rearrangeTasks(
                            parentCategoryId = currentState.parentCategoryId,
                            taskId = taskId,
                            parentTaskId = aboveDestinationTask.taskId,
                            listOrder = 0,
                            autoSort = currentState.isAutoSortCheckedTasks
                        )
                    }
                    // else if requesting equal or lower layer, put this task in between above task and below task
                    else {
                        taskRepository.rearrangeTasks(
                            parentCategoryId = currentState.parentCategoryId,
                            taskId = taskId,
                            parentTaskId = aboveDestinationTask.taskId,
                            listOrder = aboveDestinationTask.listOrder + 1,
                            autoSort = currentState.isAutoSortCheckedTasks
                        )
                    }
                }
                // if above cases aren't true, then below task is not necessarily related to above task
                // if bottom task's parent is null, this task can also have null parent (requestedLayer 0)
                else if (belowDestinationTask.parentTaskId == null && requestedLayer <= 0) {
                    taskRepository.rearrangeTasks(
                        parentCategoryId = currentState.parentCategoryId,
                        taskId = taskId,
                        parentTaskId = null,
                        listOrder = belowDestinationTask.listOrder,
                        autoSort = currentState.isAutoSortCheckedTasks
                    )
                }
                // if above cases aren't true, then below task and above task must branch off at some lower layer
                // this task's parent could be same as bottom task, or anywhere between that layer and one layer higher than above task
                else if (requestedLayer <= belowDestinationTaskLayer) {
                    taskRepository.rearrangeTasks(
                        parentCategoryId = currentState.parentCategoryId,
                        taskId = taskId,
                        parentTaskId = belowDestinationTask.parentTaskId,
                        listOrder = belowDestinationTask.listOrder,
                        autoSort = currentState.isAutoSortCheckedTasks
                    )
                }
                else if (requestedLayer > aboveDestinationTaskLayer) {
                    taskRepository.rearrangeTasks(
                        parentCategoryId = currentState.parentCategoryId,
                        taskId = taskId,
                        parentTaskId = aboveDestinationTask.taskId,
                        listOrder = 0,
                        autoSort = currentState.isAutoSortCheckedTasks
                    )
                }
                else if (requestedLayer == aboveDestinationTaskLayer) {
                    taskRepository.rearrangeTasks(
                        parentCategoryId = currentState.parentCategoryId,
                        taskId = taskId,
                        parentTaskId = aboveDestinationTask.parentTaskId,
                        listOrder = aboveDestinationTask.listOrder,
                        autoSort = currentState.isAutoSortCheckedTasks
                    )
                }
                // if all above cases aren't true, then we must determine the parent from the requested task layer
                // the requested task layer must be on the level of an extended parent somewhere in between the above and below tasks' layers
                // determine parent of parent of above task until reaching the same layer
                else {
                    // TODO must keep information about what layer each task is on


                }
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

    fun setDraggedTaskSize(size: Int?) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                _uiState.value = currentState.copy(draggedTaskSize = size)
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun setLazyListIndexBeingDragged(index: Int?) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                _uiState.value = currentState.copy(lazyListIndexBeingDragged = index)
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun setLazyListTargetIndex(index: Int?) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                _uiState.value = currentState.copy(lazyListTargetIndex = index)
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun updateDragHandler(dragHandler: DragHandler) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                _uiState.value = currentState.copy(dragHandler = dragHandler)
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
                listOrder = taskEntity.listOrder,
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
        val lazyListIndexBeingDragged: Int? = null,
        val lazyListTargetIndex: Int? = null,
        val draggedTaskSize: Int? = null,
        val dragHandler: DragHandler = DragHandler(),
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
    data class RearrangeTasks(
        val taskId: Int,
        val aboveDestinationTask: Task,
        val aboveDestinationTaskLayer: Int,
        val belowDestinationTask: Task,
        val belowDestinationTaskLayer: Int,
        val requestedLayer: Int,
    ): TasksDetailAction
    data object ResetClearFocusTrigger: TasksDetailAction
    data object ResetFocusTrigger: TasksDetailAction
    data class SetDraggedTaskSize(val size: Int?): TasksDetailAction
    data class SetLazyListTargetIndex(val index: Int?): TasksDetailAction
    data class SetLazyListIndexBeingDragged(val index: Int?): TasksDetailAction
    data class UpdateDragHandler(val dragHandler: DragHandler): TasksDetailAction
}

typealias TasksDetailActionHandler = (TasksDetailAction) -> Unit