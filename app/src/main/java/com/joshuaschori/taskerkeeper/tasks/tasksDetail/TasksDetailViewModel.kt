package com.joshuaschori.taskerkeeper.tasks.tasksDetail

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshuaschori.taskerkeeper.DragHandler
import com.joshuaschori.taskerkeeper.DragMode
import com.joshuaschori.taskerkeeper.data.tasks.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository
): ViewModel() {
    private val _uiState: MutableStateFlow<TasksDetailState> = MutableStateFlow(TasksDetailState.Loading)
    val uiState: StateFlow<TasksDetailState> = _uiState.asStateFlow()
    private val triggerDatabase: MutableStateFlow<Boolean> = MutableStateFlow(false)

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
                _uiState.value = TasksDetailState.Error("TasksDetailViewModel addNewTask")
            }
        }
    }

    fun changeTasksDetailExtensionMode(tasksDetailExtensionMode: TasksDetailExtensionMode) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                _uiState.value = currentState.copy(selectedTasksDetailExtensionMode = tasksDetailExtensionMode)
            } else {
                _uiState.value = TasksDetailState.Error("TasksDetailViewModel changeTasksDetailExtensionMode")
            }
        }
    }

    fun clearFocus() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                _uiState.value = currentState.copy(clearFocusTrigger = true)
            } else {
                _uiState.value = TasksDetailState.Error("TasksDetailViewModel clearFocus")
            }
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                taskRepository.removeTask(currentState.parentCategoryId, taskId)
            } else {
                _uiState.value = TasksDetailState.Error("TasksDetailViewModel deleteTask")
            }
        }
    }

    fun editTaskDescription(taskId: Int, descriptionChange: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                taskRepository.editTaskDescription(taskId, descriptionChange)
            } else {
                _uiState.value = TasksDetailState.Error("TasksDetailViewModel editTaskDescription")
            }
        }
    }

    fun expandTask(taskId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                taskRepository.expandTask(taskId)
            } else {
                _uiState.value = TasksDetailState.Error("TasksDetailViewModel expandTask")
            }
        }
    }

    fun listenForDatabaseUpdates(parentCategoryId: Int) {
        viewModelScope.launch {
            combine(taskRepository.getTasks(parentCategoryId), triggerDatabase) { taskEntityList, _ ->
                taskEntityList
            }.collect { taskEntityList ->
                when (val currentState = _uiState.value) {
                    is TasksDetailState.Content -> {
                        _uiState.value = currentState.copy(
                            taskList = TaskListBuilder().prepareTaskList(
                                taskEntityList = taskEntityList,
                                draggedTaskId = currentState.dragHandler.draggedItem?.itemId,
                                dragMode = currentState.dragHandler.dragMode
                            ),
                        )
                    }
                    is TasksDetailState.Loading -> {
                        _uiState.value = TasksDetailState.Content(
                            parentCategoryId = parentCategoryId,
                            taskList = TaskListBuilder().prepareTaskList(
                                taskEntityList = taskEntityList,
                                draggedTaskId = null,
                                dragMode = null
                            ),
                        )
                    }
                    else -> {
                        _uiState.value = TasksDetailState.Error("TasksDetailViewModel listenForDatabaseUpdates")
                    }
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
                _uiState.value = TasksDetailState.Error("TasksDetailViewModel markTaskComplete")
            }
        }
    }

    fun markTaskIncomplete(taskId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                taskRepository.markTaskIncomplete(currentState.parentCategoryId, taskId, currentState.isAutoSortCheckedTasks)
            } else {
                _uiState.value = TasksDetailState.Error("TasksDetailViewModel markTaskIncomplete")
            }
        }
    }

    fun minimizeTask(taskId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                taskRepository.minimizeTask(taskId)
            } else {
                _uiState.value = TasksDetailState.Error("TasksDetailViewModel minimizeTask")
            }
        }
    }

    fun onDrag(task: Task, dragAmount: Offset, dragOffsetTotal: Int, lazyListState: LazyListState, requestedTierChange: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {

                val updatedDragHandler = currentState.dragHandler.updateOnDrag(
                    item = task,
                    itemList = currentState.taskList,
                    dragAmount = dragAmount,
                    dragOffsetTotal = dragOffsetTotal,
                    lazyListState = lazyListState,
                    requestedTierChange = requestedTierChange
                )

                val onDragModeChangeTriggerDatabase: Boolean = currentState.dragHandler.dragMode != updatedDragHandler?.dragMode

                if (updatedDragHandler != null) {
                    _uiState.value = currentState.copy(
                        dragHandler = updatedDragHandler
                    )
                    if (onDragModeChangeTriggerDatabase) { triggerDatabase.emit(!triggerDatabase.value) }
                }
            } else {
                _uiState.value = TasksDetailState.Error("TasksDetailViewModel onDrag")
            }
        }
    }

    fun onDragEnd() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                if (currentState.dragHandler.draggedItem != null &&
                    currentState.dragHandler.requestedTierChange != null &&
                    currentState.dragHandler.dragTargetIndex != null &&
                    currentState.dragHandler.dragMode != null
                ) {
                    val parentCategoryId = currentState.parentCategoryId
                    val thisTask = currentState.taskList[currentState.dragHandler.draggedItem.lazyListIndex]
                    val dragTaskAbove = if (currentState.dragHandler.itemAboveTarget?.lazyListIndex != null) {
                        currentState.taskList[currentState.dragHandler.itemAboveTarget.lazyListIndex]
                    } else null
                    val dragTaskBelow = if (currentState.dragHandler.itemBelowTarget?.lazyListIndex != null) {
                        currentState.taskList[currentState.dragHandler.itemBelowTarget.lazyListIndex]
                    } else null
                    val requestedTier = thisTask.itemTier + currentState.dragHandler.requestedTierChange

                    if (!(dragTaskAbove == null && dragTaskBelow == null)) {
                        when (currentState.dragHandler.dragMode) {
                            DragMode.REARRANGE -> if (
                                currentState.dragHandler.dragYDirection != null && !currentState.dragHandler.dragMaxExceeded &&
                                !(currentState.dragHandler.dragTargetIndex == thisTask.lazyListIndex && requestedTier == thisTask.itemTier)
                            ) {
                                taskRepository.moveTaskOrder(
                                    parentCategoryId = parentCategoryId,
                                    thisTask = thisTask,
                                    taskList = currentState.taskList,
                                    taskAboveDestination = dragTaskAbove,
                                    requestedTier = requestedTier,
                                    autoSort = currentState.isAutoSortCheckedTasks
                                )
                            }
                            DragMode.CHANGE_TIER -> if (requestedTier != thisTask.itemTier) {
                                taskRepository.moveTaskTier(
                                    parentCategoryId = parentCategoryId,
                                    thisTask = thisTask,
                                    taskList = currentState.taskList,
                                    aboveTask = dragTaskAbove,
                                    belowTask = dragTaskBelow,
                                    requestedTier = requestedTier,
                                    autoSort = currentState.isAutoSortCheckedTasks
                                )
                            }
                        }
                    }
                }
            } else {
                _uiState.value = TasksDetailState.Error("TasksDetailViewModel onDragEnd")
            }
        }
    }

    fun onDragStart(task: Task, size: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                _uiState.value = currentState.copy(
                    dragHandler = currentState.dragHandler.copy(
                        draggedItem = task,
                        draggedItemSize = size
                    )
                )
            } else {
                _uiState.value = TasksDetailState.Error("TasksDetailViewModel onDragStart")
            }
        }
    }

    fun resetClearFocusTrigger() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                _uiState.value = currentState.copy(clearFocusTrigger = false)
            } else {
                _uiState.value = TasksDetailState.Error("TasksDetailViewModel resetClearFocusTrigger")
            }
        }
    }

    fun resetDragHandlers() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                _uiState.value = currentState.copy(
                    dragHandler = DragHandler()
                )
                triggerDatabase.emit(!triggerDatabase.value)
            } else {
                _uiState.value = TasksDetailState.Error("TasksDetailViewModel resetDragHandlers")
            }
        }
    }

    fun resetFocusTrigger() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                _uiState.value = currentState.copy(focusTaskId = null)
            } else {
                _uiState.value = TasksDetailState.Error("TasksDetailViewModel resetFocusTrigger")
            }
        }
    }

}

sealed interface TasksDetailState {
    data class Content(
        val parentCategoryId: Int,
        val taskList: List<Task>,
        val selectedTasksDetailExtensionMode: TasksDetailExtensionMode = TasksDetailExtensionMode.NORMAL,
        val clearFocusTrigger: Boolean = false,
        val focusTaskId: Int? = null,
        val isAutoSortCheckedTasks: Boolean = true,
        val dragHandler: DragHandler = DragHandler()
    ) : TasksDetailState
    data class Error(
        val string: String,
    ) : TasksDetailState
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
    data class OnDrag(
        val task: Task,
        val dragAmount: Offset,
        val dragOffsetTotal: Int,
        val lazyListState: LazyListState,
        val requestedTierChange: Int
    ): TasksDetailAction
    data object OnDragEnd: TasksDetailAction
    data class OnDragStart(
        val task: Task,
        val size: Int,
    ): TasksDetailAction
    data object ResetClearFocusTrigger: TasksDetailAction
    data object ResetDragHandlers: TasksDetailAction
    data object ResetFocusTrigger: TasksDetailAction
}

typealias TasksDetailActionHandler = (TasksDetailAction) -> Unit