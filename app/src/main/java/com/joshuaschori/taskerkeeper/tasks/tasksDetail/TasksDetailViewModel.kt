package com.joshuaschori.taskerkeeper.tasks.tasksDetail

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshuaschori.taskerkeeper.Constants.DRAG_MODE_SENSITIVITY
import com.joshuaschori.taskerkeeper.Constants.MAX_LAYER_FOR_SUBTASKS
import com.joshuaschori.taskerkeeper.DragMode
import com.joshuaschori.taskerkeeper.YDirection
import com.joshuaschori.taskerkeeper.data.tasks.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

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
                                draggedTaskId = currentState.draggedTask?.taskId,
                                dragMode = currentState.dragMode
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

    fun onDrag(task: Task, dragAmount: Offset, dragOffsetTotal: Int, lazyListState: LazyListState, requestedLayerChange: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {

                val dragMode = currentState.dragMode ?: if (abs(dragAmount.y) > abs(dragAmount.x)) {

                    if (abs(dragAmount.y) > DRAG_MODE_SENSITIVITY) DragMode.REARRANGE else null
                } else {
                    if (abs(dragAmount.x) > DRAG_MODE_SENSITIVITY) DragMode.CHANGE_LAYER else null
                }

                val onDragModeChangeTriggerDatabase: Boolean = currentState.dragMode != dragMode

                if (dragMode != null) {
                    val dragYDirection = if (dragMode == DragMode.REARRANGE) {
                        if (abs(dragAmount.y) > abs(dragAmount.x)) {
                            if (dragAmount.y > 0) {
                                YDirection.DOWN
                            } else {
                                YDirection.UP
                            }
                        } else { currentState.dragYDirection }
                    } else { null }

                    val dragTargetIndex = lazyListState.layoutInfo.visibleItemsInfo.find { item ->
                        dragOffsetTotal in item.offset..item.offset + item.size
                    }?.index ?: currentState.dragTargetIndex

                    val taskList = currentState.taskList

                    val targetAboveTask: Task? = if (dragTargetIndex != null) {
                        when (dragMode) {
                            DragMode.CHANGE_LAYER -> if (task.lazyListIndex > 0) taskList[task.lazyListIndex - 1] else null
                            DragMode.REARRANGE -> if (task.lazyListIndex == dragTargetIndex) {
                                if (task.lazyListIndex > 0) taskList[task.lazyListIndex - 1] else null
                            } else {
                                when (dragYDirection) {
                                    YDirection.UP -> if (dragTargetIndex > 0) taskList[dragTargetIndex - 1] else null
                                    YDirection.DOWN -> taskList[dragTargetIndex]
                                    null -> null
                                }
                            }
                        }
                    } else { null }

                    val targetBelowTask: Task? = if (dragTargetIndex != null) {
                        when (dragMode) {
                            DragMode.CHANGE_LAYER -> if (task.lazyListIndex < taskList.size - 1) taskList[task.lazyListIndex + 1] else null
                            DragMode.REARRANGE -> if (task.lazyListIndex == dragTargetIndex) {
                                if (task.lazyListIndex < taskList.size - 1) taskList[task.lazyListIndex + 1] else null
                            } else {
                                when (dragYDirection) {
                                    YDirection.UP -> taskList[dragTargetIndex]
                                    YDirection.DOWN -> if (dragTargetIndex + 1 < taskList.size) taskList[dragTargetIndex + 1] else null
                                    null -> null
                                }
                            }
                        }
                    } else { null }

                    val allowedMinimumLayer: Int = if (dragTargetIndex != null && !(targetAboveTask == null && targetBelowTask == null)) {
                        when (dragMode) {
                            DragMode.REARRANGE -> if (targetAboveTask != null && targetBelowTask != null && targetBelowTask.parentTaskId != null) {targetBelowTask.taskLayer } else { 0 }
                            DragMode.CHANGE_LAYER -> if (targetAboveTask != null && targetBelowTask != null && targetBelowTask.parentTaskId != null) { targetBelowTask.taskLayer - 1 } else { 0 }
                        }
                    } else { 0 }

                    val allowedMaximumLayer: Int = if (dragTargetIndex != null && !(targetAboveTask == null && targetBelowTask == null)) {
                        when (dragMode) {
                            DragMode.REARRANGE -> if (targetAboveTask != null) {
                                if (targetAboveTask.taskLayer + 1 + task.highestLayerBelow - task.taskLayer > MAX_LAYER_FOR_SUBTASKS) {
                                    MAX_LAYER_FOR_SUBTASKS - task.highestLayerBelow + task.taskLayer
                                } else {
                                    targetAboveTask.taskLayer + 1
                                }
                            } else { 0 }
                            DragMode.CHANGE_LAYER -> if (targetAboveTask != null) {
                                if (targetAboveTask.taskLayer + 1 > MAX_LAYER_FOR_SUBTASKS) {
                                    MAX_LAYER_FOR_SUBTASKS
                                } else {
                                    targetAboveTask.taskLayer + 1
                                }
                            } else { 0 }
                        }
                    } else { 0 }

                    val allowedLayerChange: Int = if (
                        task.taskLayer + requestedLayerChange < allowedMinimumLayer ||
                        allowedMaximumLayer < allowedMinimumLayer
                    ) {
                        allowedMinimumLayer - task.taskLayer
                    } else if (task.taskLayer + requestedLayerChange > allowedMaximumLayer) {
                        allowedMaximumLayer - task.taskLayer
                    } else {
                        requestedLayerChange
                    }

                    val dragMaxExceeded = (dragMode == DragMode.REARRANGE && task.highestLayerBelow + allowedLayerChange > MAX_LAYER_FOR_SUBTASKS) ||
                            (dragMode == DragMode.CHANGE_LAYER && allowedMinimumLayer == task.taskLayer && allowedMaximumLayer == task.taskLayer)

                    val dragLeftPossible = task.taskLayer + allowedLayerChange > allowedMinimumLayer

                    val dragRightPossible = task.taskLayer + allowedLayerChange < allowedMaximumLayer

                    _uiState.value = currentState.copy(
                        dragMode = dragMode,
                        dragYDirection = dragYDirection,
                        dragTargetIndex = dragTargetIndex,
                        dragTaskAbove = targetAboveTask,
                        dragTaskBelow = targetBelowTask,
                        dragRequestedLayerChange = allowedLayerChange,
                        dragMaxExceeded = dragMaxExceeded,
                        dragLeftPossible = dragLeftPossible,
                        dragRightPossible = dragRightPossible,
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
                if (currentState.draggedTask != null && currentState.dragRequestedLayerChange != null && currentState.dragTargetIndex != null && currentState.dragMode != null) {
                    val parentCategoryId = currentState.parentCategoryId
                    val thisTask = currentState.draggedTask
                    val taskList = currentState.taskList
                    val dragMode = currentState.dragMode
                    val dragYDirection = currentState.dragYDirection
                    val dragTargetIndex = currentState.dragTargetIndex
                    val dragTaskAbove = currentState.dragTaskAbove
                    val dragTaskBelow = currentState.dragTaskBelow
                    val dragMaxExceeded = currentState.dragMaxExceeded
                    val autoSort = currentState.isAutoSortCheckedTasks

                    val requestedLayer = thisTask.taskLayer + currentState.dragRequestedLayerChange

                    if (!(dragTaskAbove == null && dragTaskBelow == null)) {
                        when (dragMode) {
                            DragMode.REARRANGE -> if (
                                dragYDirection != null && !dragMaxExceeded &&
                                !(dragTargetIndex == thisTask.lazyListIndex && requestedLayer == thisTask.taskLayer)
                            ) {
                                taskRepository.moveTaskOrder(
                                    parentCategoryId = parentCategoryId,
                                    thisTask = thisTask,
                                    taskList = taskList,
                                    taskAboveDestination = dragTaskAbove,
                                    requestedLayer = requestedLayer,
                                    autoSort = autoSort
                                )
                            }
                            DragMode.CHANGE_LAYER -> if (requestedLayer != thisTask.taskLayer) {
                                taskRepository.moveTaskLayer(
                                    parentCategoryId = parentCategoryId,
                                    thisTask = thisTask,
                                    taskList = taskList,
                                    aboveTask = dragTaskAbove,
                                    belowTask = dragTaskBelow,
                                    requestedLayer = requestedLayer,
                                    autoSort = autoSort
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
                    draggedTask = task,
                    draggedTaskSize = size,
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
                    draggedTask = null,
                    draggedTaskSize = null,
                    dragMode = null,
                    dragTargetIndex = null,
                    dragYDirection = null,
                    dragRequestedLayerChange = null,
                    dragTaskAbove = null,
                    dragTaskBelow = null,
                    dragMaxExceeded = false,
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
        val draggedTask: Task? = null,
        val draggedTaskSize: Int? = null,
        val dragMode: DragMode? = null,
        val dragTargetIndex: Int? = null,
        val dragYDirection: YDirection? = null,
        val dragTaskAbove: Task? = null,
        val dragTaskBelow: Task? = null,
        val dragRequestedLayerChange: Int? = null,
        val dragMaxExceeded: Boolean = false,
        val dragLeftPossible: Boolean = false,
        val dragRightPossible: Boolean = false,
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
        val requestedLayerChange: Int
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