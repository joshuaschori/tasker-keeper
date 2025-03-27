package com.joshuaschori.taskerkeeper.tasks.tasksDetail

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshuaschori.taskerkeeper.XYAxis
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
    private val draggedTaskIdStateFlow: MutableStateFlow<Int?> = MutableStateFlow(null)

    // TODO some of these here and in repository and dao may not be used after simplifying extension modes?
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
            combine(taskRepository.getTasks(parentCategoryId), draggedTaskIdStateFlow) { taskEntityList, draggedTaskId ->
                Pair(taskEntityList, draggedTaskId)
            }.collect { (taskEntityList, draggedTaskId) ->
                when (val currentState = _uiState.value) {
                    is TasksDetailState.Content -> {
                        _uiState.value = currentState.copy(
                            taskList = TaskListBuilder().prepareTaskList(
                                taskEntityList = taskEntityList,
                                draggedTaskId = draggedTaskId
                            ),
                            lazyListState = LazyListState()
                        )
                    }
                    is TasksDetailState.Loading -> {
                        _uiState.value = TasksDetailState.Content(
                            parentCategoryId = parentCategoryId,
                            taskList = TaskListBuilder().prepareTaskList(
                                taskEntityList = taskEntityList,
                                draggedTaskId = draggedTaskId
                            ),
                            lazyListState = LazyListState()
                        )
                    }
                    else -> {
                        _uiState.value = TasksDetailState.Error
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

    fun moveTaskLayer(
        taskId: Int,
        aboveTask: Task?,
        belowTask: Task?,
        requestedLayer: Int,
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value
            // TODO have UI reflect this?
            // TODO put at matching layer, depending on direction up or down
            // TODO allow changing task layer further down, rearranging vertically when necessary???? prioritizing horizontal or vertical??
            // TODO go back over everything and think about indexes more specifically, indexes of lazyTaskList? think about expanded or not expanded
            // TODO consider making new empty task for parent when necessary?????
            // TODO consider allowing any requested layer change, and reattaching children etc if necessary to reflect that UI change
            if (currentState is TasksDetailState.Content) {

                /*// if above task is null, this task's parent must be null, and must already be null
                if (aboveTask != null) {}
                else if (belowTask == null) {}
                // if placing your task at requested layer would break up the bottom task from the upper parent
                // must put task at below task's layer, making above task this task's parent
                else if (belowTask.parentTaskId == aboveTask.taskId) {
                    taskRepository.rearrangeTasks(
                        parentCategoryId = currentState.parentCategoryId,
                        taskId = taskId,
                        parentTaskId = aboveTask.taskId,
                        listOrder = 0,
                        autoSort = currentState.isAutoSortCheckedTasks
                    )
                }
                // task can either be put at the same layer, or one layer higher, making it the subtask of the above task
                else if (belowTask.parentTaskId == aboveTask.parentTaskId) {
                    // if requesting higher layer, make this task the first subtask of above task
                    if (requestedLayer > aboveTask.taskLayer) {
                        taskRepository.rearrangeTasks(
                            parentCategoryId = currentState.parentCategoryId,
                            taskId = taskId,
                            parentTaskId = aboveTask.taskId,
                            listOrder = 0,
                            autoSort = currentState.isAutoSortCheckedTasks
                        )
                    }
                    // else if requesting equal or lower layer, put this task in between above task and below task
                    else {
                        taskRepository.rearrangeTasks(
                            parentCategoryId = currentState.parentCategoryId,
                            taskId = taskId,
                            parentTaskId = aboveTask.taskId,
                            listOrder = aboveTask.listOrder + 1,
                            autoSort = currentState.isAutoSortCheckedTasks
                        )
                    }
                }
                // if above cases aren't true, then below task is not necessarily related to above task
                // if bottom task's parent is null, this task can also have null parent (requestedLayer 0)
                else if (belowTask.parentTaskId == null && requestedLayer <= 0) {
                    taskRepository.rearrangeTasks(
                        parentCategoryId = currentState.parentCategoryId,
                        taskId = taskId,
                        parentTaskId = null,
                        listOrder = belowTask.listOrder,
                        autoSort = currentState.isAutoSortCheckedTasks
                    )
                }
                // if above cases aren't true, then below task and above task must branch off at some lower layer
                // this task's parent could be same as bottom task, or anywhere between that layer and one layer higher than above task
                else if (requestedLayer <= belowTask.taskLayer) {
                    taskRepository.rearrangeTasks(
                        parentCategoryId = currentState.parentCategoryId,
                        taskId = taskId,
                        parentTaskId = belowTask.parentTaskId,
                        listOrder = belowTask.listOrder,
                        autoSort = currentState.isAutoSortCheckedTasks
                    )
                }
                else if (requestedLayer > aboveTask.taskLayer) {
                    taskRepository.rearrangeTasks(
                        parentCategoryId = currentState.parentCategoryId,
                        taskId = taskId,
                        parentTaskId = aboveTask.taskId,
                        listOrder = 0,
                        autoSort = currentState.isAutoSortCheckedTasks
                    )
                }
                else if (requestedLayer == aboveTask.taskLayer) {
                    taskRepository.rearrangeTasks(
                        parentCategoryId = currentState.parentCategoryId,
                        taskId = taskId,
                        parentTaskId = aboveTask.parentTaskId,
                        listOrder = aboveTask.listOrder,
                        autoSort = currentState.isAutoSortCheckedTasks
                    )
                }
                // if all above cases aren't true, then we must determine the parent from the requested task layer
                // the requested task layer must be on the level of an extended parent somewhere in between the above and below tasks' layers
                // determine parent of parent of above task until reaching the same layer
                else {}*/

            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun moveTaskOrder(
        taskId: Int,
        parentTaskId: Int?,
        listOrder: Int,
        aboveTask: Task?,
        belowTask: Task?,
        attachUpOrDown: YDirection,
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                if (aboveTask == null) {
                    taskRepository.moveTask(
                        parentCategoryId = currentState.parentCategoryId,
                        taskId = taskId,
                        parentTaskId = parentTaskId,
                        listOrder = listOrder,
                        destinationParentTaskId = null,
                        destinationListOrder = 0,
                        autoSort = currentState.isAutoSortCheckedTasks
                    )
                } else if (belowTask == null) {
                    taskRepository.moveTask(
                        parentCategoryId = currentState.parentCategoryId,
                        taskId = taskId,
                        parentTaskId = parentTaskId,
                        listOrder = listOrder,
                        destinationParentTaskId = null,
                        destinationListOrder = aboveTask.listOrder + 1,
                        autoSort = currentState.isAutoSortCheckedTasks
                    )
                } else if (belowTask.parentTaskId == aboveTask.taskId) {
                    taskRepository.moveTask(
                        parentCategoryId = currentState.parentCategoryId,
                        taskId = taskId,
                        parentTaskId = parentTaskId,
                        listOrder = listOrder,
                        destinationParentTaskId = aboveTask.taskId,
                        destinationListOrder = 0,
                        autoSort = currentState.isAutoSortCheckedTasks
                    )
                } else {
                    when (attachUpOrDown) {
                        YDirection.UP -> taskRepository.moveTask(
                            parentCategoryId = currentState.parentCategoryId,
                            taskId = taskId,
                            parentTaskId = parentTaskId,
                            listOrder = listOrder,
                            destinationParentTaskId = aboveTask.parentTaskId,
                            destinationListOrder = aboveTask.listOrder + 1,
                            autoSort = currentState.isAutoSortCheckedTasks
                        )
                        YDirection.DOWN -> taskRepository.moveTask(
                            parentCategoryId = currentState.parentCategoryId,
                            taskId = taskId,
                            parentTaskId = parentTaskId,
                            listOrder = listOrder,
                            destinationParentTaskId = belowTask.parentTaskId,
                            destinationListOrder = belowTask.listOrder,
                            autoSort = currentState.isAutoSortCheckedTasks
                        )
                    }
                }
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun onDrag(dragAmount: Offset) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                val dragOrientation = currentState.dragOrientation
                if (dragOrientation == null) {
                    if (abs(dragAmount.y) > abs(dragAmount.x)) {
                        setDragOrientation(XYAxis.Y)
                        if (dragAmount.y > 0) {
                            setDragYDirection(YDirection.DOWN)
                        } else {
                            setDragYDirection(YDirection.UP)
                        }
                    } else {
                        setDragOrientation(XYAxis.X)
                    }
                } else if (dragOrientation == XYAxis.Y) {
                    setDragYDirection(if (dragAmount.y < 0) YDirection.UP else YDirection.DOWN)
                }
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun onDragEnd(lazyListIndex: Int, task: Task, taskLayer: Int, requestedLayerChange: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                val dragTargetIndex = currentState.dragTargetIndex
                val dragOrientation = currentState.dragOrientation
                val dragYDirection = currentState.dragYDirection
                val lazyTaskList = currentState.taskList
                if (dragTargetIndex != null && dragOrientation != null) {
                    when (dragOrientation) {
                        XYAxis.Y -> if (dragYDirection != null && dragTargetIndex != lazyListIndex) {
                            moveTaskOrder(
                                taskId = task.taskId,
                                parentTaskId = task.parentTaskId,
                                listOrder = task.listOrder,
                                aboveTask = when (dragYDirection) {
                                    YDirection.UP -> if (dragTargetIndex > 0) lazyTaskList[dragTargetIndex - 1] else null
                                    YDirection.DOWN -> lazyTaskList[dragTargetIndex]
                                },
                                belowTask = when (dragYDirection) {
                                    YDirection.UP -> lazyTaskList[dragTargetIndex]
                                    YDirection.DOWN -> if (dragTargetIndex + 1 < lazyTaskList.size) lazyTaskList[dragTargetIndex + 1] else null
                                },
                                attachUpOrDown = when (dragYDirection) {
                                    YDirection.UP -> YDirection.DOWN
                                    YDirection.DOWN -> YDirection.UP
                                }
                            )
                        }
                        XYAxis.X -> if (requestedLayerChange != 0) {
                            moveTaskLayer(
                                taskId = task.taskId,
                                aboveTask = if (lazyListIndex > 0) lazyTaskList[lazyListIndex - 1] else null,
                                belowTask = if (lazyListIndex < lazyTaskList.size - 1) lazyTaskList[lazyListIndex + 1] else null,
                                requestedLayer = taskLayer + requestedLayerChange
                            )
                        }
                    }
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

    fun resetDragHandlers() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                _uiState.value = currentState.copy(
                    draggedIndex = null,
                    draggedTaskSize = null,
                    dragOrientation = null,
                    dragTargetIndex = null,
                    dragYDirection = null,
                )
                draggedTaskIdStateFlow.value = null
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

    fun setDraggedTask(taskId: Int, index: Int, size: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                _uiState.value = currentState.copy(
                    draggedIndex = index,
                    draggedTaskSize = size,
                )
                draggedTaskIdStateFlow.value = taskId
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun setDragOrientation(axis: XYAxis) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                _uiState.value = currentState.copy(dragOrientation = axis)
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun setDragTargetIndex(dragOffsetTotal: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                val lazyListState = currentState.lazyListState
                val targetLazyListItem = lazyListState.layoutInfo.visibleItemsInfo.find { item ->
                    dragOffsetTotal in item.offset..item.offset + item.size
                }
                if (targetLazyListItem != null) {
                    _uiState.value = currentState.copy(dragTargetIndex = targetLazyListItem.index)
                }
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun setDragYDirection(direction: YDirection) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                _uiState.value = currentState.copy(dragYDirection = direction)
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

}

sealed interface TasksDetailState {
    data class Content(
        val parentCategoryId: Int,
        val taskList: List<Task>,
        val lazyListState: LazyListState,
        val selectedTasksDetailExtensionMode: TasksDetailExtensionMode = TasksDetailExtensionMode.NORMAL,
        val clearFocusTrigger: Boolean = false,
        val focusTaskId: Int? = null,
        val isAutoSortCheckedTasks: Boolean = true,
        val draggedIndex: Int? = null,
        val draggedTaskSize: Int? = null,
        val dragOrientation: XYAxis? = null,
        val dragTargetIndex: Int? = null,
        val dragYDirection: YDirection? = null,
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
    data class MoveTaskLayer(
        val taskId: Int,
        val aboveTask: Task?,
        val belowTask: Task?,
        val requestedLayer: Int,
    ): TasksDetailAction
    data class MoveTaskOrder(
        val taskId: Int,
        val parentTaskId: Int?,
        val listOrder: Int,
        val aboveTask: Task?,
        val belowTask: Task?,
        val attachUpOrDown: YDirection,
    ): TasksDetailAction
    data object NavigateToTasksMenu: TasksDetailAction
    data class OnDrag(val dragAmount: Offset): TasksDetailAction
    data class OnDragEnd(
        val lazyListIndex: Int,
        val task: Task,
        val taskLayer: Int,
        val requestedLayerChange: Int
    ): TasksDetailAction
    data object ResetClearFocusTrigger: TasksDetailAction
    data object ResetDragHandlers: TasksDetailAction
    data object ResetFocusTrigger: TasksDetailAction
    data class SetDraggedTask(val taskId: Int, val index: Int, val size: Int): TasksDetailAction
    data class SetDragOrientation(val axis: XYAxis): TasksDetailAction
    data class SetDragTargetIndex(val dragOffsetTotal: Int): TasksDetailAction
    data class SetDragYDirection(val direction: YDirection): TasksDetailAction
}

typealias TasksDetailActionHandler = (TasksDetailAction) -> Unit