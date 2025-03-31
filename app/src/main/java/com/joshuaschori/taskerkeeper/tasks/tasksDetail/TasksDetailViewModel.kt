package com.joshuaschori.taskerkeeper.tasks.tasksDetail

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            combine(taskRepository.getTasks(parentCategoryId), triggerDatabase) { taskEntityList, _ ->
                taskEntityList
            }.collect { taskEntityList ->
                when (val currentState = _uiState.value) {
                    is TasksDetailState.Content -> {
                        _uiState.value = currentState.copy(
                            taskList = TaskListBuilder().prepareTaskList(
                                taskEntityList = taskEntityList,
                                draggedTaskId = currentState.draggedTaskId,
                                dragMode = currentState.dragMode
                            ),
                            lazyListState = LazyListState()
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
        thisTask: Task,
        taskAboveDestination: Task?,
        taskBelowDestination: Task?,
        requestedLayer: Int,
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                // error handling
                if (thisTask == currentState.taskList[thisTask.lazyListIndex]) {
                    // if thisTask is the destination and no layer change requested, don't move, do nothing
                    // or if thisTask is the only task
                    if (!(taskBelowDestination == null && taskAboveDestination == null) ||
                        !(currentState.dragTargetIndex == thisTask.lazyListIndex && requestedLayer == thisTask.taskLayer)
                    ) {
                        if (taskAboveDestination == null) {
                            // task must be placed on layer 0 / have null parent
                            when (requestedLayer) {
                                0 -> taskRepository.moveTask(
                                    parentCategoryId = currentState.parentCategoryId,
                                    taskId = thisTask.taskId,
                                    parentTaskId = thisTask.parentTaskId,
                                    listOrder = thisTask.listOrder,
                                    destinationParentTaskId = null,
                                    destinationListOrder = 0,
                                    autoSort = currentState.isAutoSortCheckedTasks
                                )
                                else -> {
                                    _uiState.value = TasksDetailState.Error
                                }
                            }
                        } else if (taskBelowDestination == null) {
                            // task can be placed on layers 0 through taskAboveDestination.taskLayer + 1
                            // if not being placed on same layer as taskAboveDestination or its layer + 1, then must find previous task at requested layer
                            when (requestedLayer) {
                                0 -> taskRepository.moveTask(
                                    parentCategoryId = currentState.parentCategoryId,
                                    taskId = thisTask.taskId,
                                    parentTaskId = thisTask.parentTaskId,
                                    listOrder = thisTask.listOrder,
                                    destinationParentTaskId = null,
                                    destinationListOrder = currentState.taskList.size,
                                    autoSort = currentState.isAutoSortCheckedTasks
                                )
                                taskAboveDestination.taskLayer -> {
                                    taskRepository.moveTask(
                                        parentCategoryId = currentState.parentCategoryId,
                                        taskId = thisTask.taskId,
                                        parentTaskId = thisTask.parentTaskId,
                                        listOrder = thisTask.listOrder,
                                        destinationParentTaskId = taskAboveDestination.parentTaskId,
                                        destinationListOrder = taskAboveDestination.listOrder + 1,
                                        autoSort = currentState.isAutoSortCheckedTasks
                                    )
                                }
                                taskAboveDestination.taskLayer + 1 -> {
                                    taskRepository.moveTask(
                                        parentCategoryId = currentState.parentCategoryId,
                                        taskId = thisTask.taskId,
                                        parentTaskId = thisTask.parentTaskId,
                                        listOrder = thisTask.listOrder,
                                        destinationParentTaskId = taskAboveDestination.taskId,
                                        destinationListOrder = 0,
                                        autoSort = currentState.isAutoSortCheckedTasks
                                    )
                                }
                                else -> {
                                    if (requestedLayer > 0 && requestedLayer < taskAboveDestination.taskLayer) {
                                        val previousTaskAtRequestedLayer = findPreviousTaskAtRequestedLayer(
                                            aboveTask = taskAboveDestination,
                                            taskList = currentState.taskList,
                                            requestedLayer = requestedLayer
                                        )
                                        if (previousTaskAtRequestedLayer != null) {
                                            taskRepository.moveTask(
                                                parentCategoryId = currentState.parentCategoryId,
                                                taskId = thisTask.taskId,
                                                parentTaskId = thisTask.parentTaskId,
                                                listOrder = thisTask.listOrder,
                                                destinationParentTaskId = previousTaskAtRequestedLayer.parentTaskId,
                                                destinationListOrder = previousTaskAtRequestedLayer.listOrder + 1,
                                                autoSort = currentState.isAutoSortCheckedTasks
                                            )
                                        } else {
                                            _uiState.value = TasksDetailState.Error
                                        }
                                    } else {
                                        _uiState.value = TasksDetailState.Error
                                    }
                                }
                            }
                        } else if (taskBelowDestination.parentTaskId == taskAboveDestination.taskId) {
                            // task must be placed on layer taskBelowDestination.taskLayer / have same parent
                            when (requestedLayer) {
                                taskAboveDestination.taskLayer + 1 -> taskRepository.moveTask(
                                    parentCategoryId = currentState.parentCategoryId,
                                    taskId = thisTask.taskId,
                                    parentTaskId = thisTask.parentTaskId,
                                    listOrder = thisTask.listOrder,
                                    destinationParentTaskId = taskAboveDestination.taskId,
                                    destinationListOrder = 0,
                                    autoSort = currentState.isAutoSortCheckedTasks
                                )
                                else -> {
                                    _uiState.value = TasksDetailState.Error
                                }
                            }
                        } else if (taskBelowDestination.parentTaskId == taskAboveDestination.parentTaskId) {
                            // task can be placed on layer taskAboveDestination.taskLayer / have same parent
                            // or can be placed on layer taskAboveDestination.taskLayer + 1 / have it as a parent
                            when (requestedLayer) {
                                taskAboveDestination.taskLayer -> {
                                    taskRepository.moveTask(
                                        parentCategoryId = currentState.parentCategoryId,
                                        taskId = thisTask.taskId,
                                        parentTaskId = thisTask.parentTaskId,
                                        listOrder = thisTask.listOrder,
                                        destinationParentTaskId = taskAboveDestination.parentTaskId,
                                        destinationListOrder = taskAboveDestination.listOrder + 1,
                                        autoSort = currentState.isAutoSortCheckedTasks
                                    )
                                }
                                taskAboveDestination.taskLayer + 1 -> {
                                    taskRepository.moveTask(
                                        parentCategoryId = currentState.parentCategoryId,
                                        taskId = thisTask.taskId,
                                        parentTaskId = thisTask.parentTaskId,
                                        listOrder = thisTask.listOrder,
                                        destinationParentTaskId = taskAboveDestination.taskId,
                                        destinationListOrder = 0,
                                        autoSort = currentState.isAutoSortCheckedTasks
                                    )
                                }
                                else -> {
                                    _uiState.value = TasksDetailState.Error
                                }
                            }
                        } else if (taskBelowDestination.parentTaskId == null) {
                            // task can be placed on layers 0 through taskAboveDestination.taskLayer + 1
                            // if not being placed on same layer as taskAboveDestination or its layer + 1, then must find previous task at requested layer
                            when (requestedLayer) {
                                0 -> taskRepository.moveTask(
                                    parentCategoryId = currentState.parentCategoryId,
                                    taskId = thisTask.taskId,
                                    parentTaskId = thisTask.parentTaskId,
                                    listOrder = thisTask.listOrder,
                                    destinationParentTaskId = null,
                                    destinationListOrder = taskBelowDestination.listOrder,
                                    autoSort = currentState.isAutoSortCheckedTasks
                                )
                                taskAboveDestination.taskLayer -> {
                                    taskRepository.moveTask(
                                        parentCategoryId = currentState.parentCategoryId,
                                        taskId = thisTask.taskId,
                                        parentTaskId = thisTask.parentTaskId,
                                        listOrder = thisTask.listOrder,
                                        destinationParentTaskId = taskAboveDestination.parentTaskId,
                                        destinationListOrder = taskAboveDestination.listOrder + 1,
                                        autoSort = currentState.isAutoSortCheckedTasks
                                    )
                                }
                                taskAboveDestination.taskLayer + 1 -> {
                                    taskRepository.moveTask(
                                        parentCategoryId = currentState.parentCategoryId,
                                        taskId = thisTask.taskId,
                                        parentTaskId = thisTask.parentTaskId,
                                        listOrder = thisTask.listOrder,
                                        destinationParentTaskId = taskAboveDestination.taskId,
                                        destinationListOrder = 0,
                                        autoSort = currentState.isAutoSortCheckedTasks
                                    )
                                }
                                else -> {
                                    if (requestedLayer > 0 && requestedLayer < taskAboveDestination.taskLayer) {
                                        val previousTaskAtRequestedLayer = findPreviousTaskAtRequestedLayer(
                                            aboveTask = taskAboveDestination,
                                            taskList = currentState.taskList,
                                            requestedLayer = requestedLayer
                                        )
                                        if (previousTaskAtRequestedLayer != null) {
                                            taskRepository.moveTask(
                                                parentCategoryId = currentState.parentCategoryId,
                                                taskId = thisTask.taskId,
                                                parentTaskId = thisTask.parentTaskId,
                                                listOrder = thisTask.listOrder,
                                                destinationParentTaskId = previousTaskAtRequestedLayer.parentTaskId,
                                                destinationListOrder = previousTaskAtRequestedLayer.listOrder + 1,
                                                autoSort = currentState.isAutoSortCheckedTasks
                                            )
                                        } else {
                                            _uiState.value = TasksDetailState.Error
                                        }
                                    } else {
                                        _uiState.value = TasksDetailState.Error
                                    }
                                }
                            }
                        } else {
                            // above and below task must be related by extension above
                            // task can be placed on layers taskBelowDestination.taskLayer through taskAboveDestination.taskLayer + 1
                            // if not being placed on same layer as taskAboveDestination or its layer + 1, then must find previous task at requested layer
                            when (requestedLayer) {
                                taskBelowDestination.taskLayer -> {
                                    taskRepository.moveTask(
                                        parentCategoryId = currentState.parentCategoryId,
                                        taskId = thisTask.taskId,
                                        parentTaskId = thisTask.parentTaskId,
                                        listOrder = thisTask.listOrder,
                                        destinationParentTaskId = taskBelowDestination.parentTaskId,
                                        destinationListOrder = taskBelowDestination.listOrder,
                                        autoSort = currentState.isAutoSortCheckedTasks
                                    )
                                }
                                taskAboveDestination.taskLayer -> {
                                    taskRepository.moveTask(
                                        parentCategoryId = currentState.parentCategoryId,
                                        taskId = thisTask.taskId,
                                        parentTaskId = thisTask.parentTaskId,
                                        listOrder = thisTask.listOrder,
                                        destinationParentTaskId = taskAboveDestination.parentTaskId,
                                        destinationListOrder = taskAboveDestination.listOrder + 1,
                                        autoSort = currentState.isAutoSortCheckedTasks
                                    )
                                }
                                taskAboveDestination.taskLayer + 1 -> {
                                    taskRepository.moveTask(
                                        parentCategoryId = currentState.parentCategoryId,
                                        taskId = thisTask.taskId,
                                        parentTaskId = thisTask.parentTaskId,
                                        listOrder = thisTask.listOrder,
                                        destinationParentTaskId = taskAboveDestination.taskId,
                                        destinationListOrder = 0,
                                        autoSort = currentState.isAutoSortCheckedTasks
                                    )
                                }
                                else -> {
                                    if (requestedLayer > taskBelowDestination.taskLayer && requestedLayer < taskAboveDestination.taskLayer) {
                                        val previousTaskAtRequestedLayer = findPreviousTaskAtRequestedLayer(
                                            aboveTask = taskAboveDestination,
                                            taskList = currentState.taskList,
                                            requestedLayer = requestedLayer
                                        )
                                        if (previousTaskAtRequestedLayer != null) {
                                            taskRepository.moveTask(
                                                parentCategoryId = currentState.parentCategoryId,
                                                taskId = thisTask.taskId,
                                                parentTaskId = thisTask.parentTaskId,
                                                listOrder = thisTask.listOrder,
                                                destinationParentTaskId = previousTaskAtRequestedLayer.parentTaskId,
                                                destinationListOrder = previousTaskAtRequestedLayer.listOrder + 1,
                                                autoSort = currentState.isAutoSortCheckedTasks
                                            )
                                        } else {
                                            _uiState.value = TasksDetailState.Error
                                        }
                                    } else {
                                        _uiState.value = TasksDetailState.Error
                                    }
                                }
                            }
                        }
                    }
                } else {
                    _uiState.value = TasksDetailState.Error
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
                val dragMode = currentState.dragMode
                if (dragMode == null) {
                    if (abs(dragAmount.y) > abs(dragAmount.x)) {
                        setDragMode(DragMode.REARRANGE)
                        if (dragAmount.y > 0) {
                            setDragYDirection(YDirection.DOWN)
                        } else {
                            setDragYDirection(YDirection.UP)
                        }
                    } else {
                        setDragMode(DragMode.CHANGE_LAYER)
                    }
                } else if (dragMode == DragMode.REARRANGE) {
                    if (abs(dragAmount.y) > abs(dragAmount.x)) {
                        setDragYDirection(if (dragAmount.y < 0) YDirection.UP else YDirection.DOWN)
                    }
                }
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun onDragEnd(
        thisTask: Task,
        requestedLayerChange: Int,
    ) {
        // TODO make extra protection, possible layer change calculated here as well as UI reflection
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                val dragMode = currentState.dragMode
                val dragYDirection = currentState.dragYDirection
                val dragTargetIndex = currentState.dragTargetIndex
                val taskList = currentState.taskList

                val targetAboveTask: Task? = if (dragTargetIndex == null || dragMode == null) {
                    null
                } else {
                    when (dragMode) {
                        DragMode.CHANGE_LAYER -> if (thisTask.lazyListIndex > 0) taskList[thisTask.lazyListIndex - 1] else null
                        DragMode.REARRANGE -> if (thisTask.lazyListIndex == dragTargetIndex) {
                            if (thisTask.lazyListIndex > 0) taskList[thisTask.lazyListIndex - 1] else null
                        } else {
                            when (dragYDirection) {
                                YDirection.UP -> if (dragTargetIndex > 0) taskList[dragTargetIndex - 1] else null
                                YDirection.DOWN -> taskList[dragTargetIndex]
                                null -> null
                            }
                        }
                    }
                }

                val targetBelowTask: Task? = if (dragTargetIndex == null || dragMode == null) {
                    null
                } else {
                    when (dragMode) {
                        DragMode.CHANGE_LAYER -> if (thisTask.lazyListIndex < taskList.size - 1) taskList[thisTask.lazyListIndex + 1] else null
                        DragMode.REARRANGE -> if (thisTask.lazyListIndex == dragTargetIndex) {
                            if (thisTask.lazyListIndex < taskList.size - 1) taskList[thisTask.lazyListIndex + 1] else null
                        } else {
                            when (dragYDirection) {
                                YDirection.UP -> taskList[dragTargetIndex]
                                YDirection.DOWN -> if (dragTargetIndex + 1 < taskList.size) taskList[dragTargetIndex + 1] else null
                                null -> null
                            }
                        }
                    }
                }

                //TODO check these and everything for new logic of destination being same as thisTask, also reflect in TaskWithSubtasks UI logic ?????
                // (I think I was already imagining logic as if it was this)
                // TODO can all this be moved to TaskWithSubtasks or no????? internal variable?
                // TODO should above and below task be state variables?
                val allowedMinimumLayer: Int = if (dragTargetIndex == null || dragMode == null || (targetAboveTask == null && targetBelowTask == null)) {
                    0
                } else {
                    when (dragMode) {
                        DragMode.REARRANGE -> if (targetAboveTask == null || targetBelowTask == null || targetBelowTask.parentTaskId == null) {
                            0
                        } else {
                            targetBelowTask.taskLayer
                        }
                        DragMode.CHANGE_LAYER -> if (targetAboveTask == null || targetBelowTask == null || targetBelowTask.parentTaskId == null) {
                            0
                        } else {
                            targetBelowTask.taskLayer - 1
                        }
                    }
                }

                val allowedMaximumLayer: Int = if (dragTargetIndex == null || dragMode == null) {
                    0
                } else {
                    when (dragMode) {
                        DragMode.REARRANGE -> if (targetAboveTask == null) {
                            0
                        } else {
                            targetAboveTask.taskLayer + 1
                        }
                        DragMode.CHANGE_LAYER -> if (targetAboveTask == null) {
                            0
                        } else {
                            targetAboveTask.taskLayer + 1
                        }
                    }
                }

                val allowedLayerChange: Int = if (thisTask.taskLayer + requestedLayerChange < allowedMinimumLayer) {
                    allowedMinimumLayer - thisTask.taskLayer
                } else if (thisTask.taskLayer + requestedLayerChange > allowedMaximumLayer) {
                    allowedMaximumLayer - thisTask.taskLayer
                } else {
                    requestedLayerChange
                }

                val requestedLayer = thisTask.taskLayer + allowedLayerChange

                // TODO delete println
                /*println("thisTask: $thisTask")
                println("targetAboveTask: $targetAboveTask")
                println("targetBelowTask: $targetBelowTask")
                println("requestedLayer: $requestedLayer")
                println(" ")*/

                if (dragTargetIndex != null && dragMode != null) {
                    when (dragMode) {
                        DragMode.REARRANGE -> if (dragYDirection != null && !(dragTargetIndex == thisTask.lazyListIndex && requestedLayer == thisTask.taskLayer)) {
                            moveTaskOrder(
                                thisTask = thisTask,
                                taskAboveDestination = targetAboveTask,
                                taskBelowDestination = targetBelowTask,
                                requestedLayer = requestedLayer
                            )
                        }
                        DragMode.CHANGE_LAYER -> if (requestedLayer != thisTask.taskLayer) {
                            moveTaskLayer(
                                taskId = thisTask.taskId,
                                aboveTask = targetAboveTask,
                                belowTask = targetBelowTask,
                                requestedLayer = requestedLayer
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
                    draggedTaskId = null,
                    draggedTaskSize = null,
                    dragMode = null,
                    dragTargetIndex = null,
                    dragYDirection = null,
                )
                triggerDatabase.emit(!triggerDatabase.value)
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
                    draggedTaskId = taskId,
                    draggedTaskSize = size,
                )
            } else {
                _uiState.value = TasksDetailState.Error
            }
        }
    }

    fun setDragMode(dragMode: DragMode) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TasksDetailState.Content) {
                _uiState.value = currentState.copy(dragMode = dragMode)
                triggerDatabase.emit(!triggerDatabase.value)
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
                val dragTargetIndex = lazyListState.layoutInfo.visibleItemsInfo.find { item ->
                    dragOffsetTotal in item.offset..item.offset + item.size
                }
                if (dragTargetIndex != null) {
                    _uiState.value = currentState.copy(dragTargetIndex = dragTargetIndex.index)
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
        val draggedTaskId: Int? = null,
        val draggedTaskSize: Int? = null,
        val dragMode: DragMode? = null,
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
        val thisTask: Task,
        val taskAboveDestination: Task?,
        val taskBelowDestination: Task?,
        val requestedLayer: Int,
    ): TasksDetailAction
    data object NavigateToTasksMenu: TasksDetailAction
    data class OnDrag(val dragAmount: Offset): TasksDetailAction
    data class OnDragEnd(
        val thisTask: Task,
        val requestedLayerChange: Int,
    ): TasksDetailAction
    data object ResetClearFocusTrigger: TasksDetailAction
    data object ResetDragHandlers: TasksDetailAction
    data object ResetFocusTrigger: TasksDetailAction
    data class SetDraggedTask(val taskId: Int, val index: Int, val size: Int): TasksDetailAction
    data class SetDragMode(val dragMode: DragMode): TasksDetailAction
    data class SetDragTargetIndex(val dragOffsetTotal: Int): TasksDetailAction
    data class SetDragYDirection(val direction: YDirection): TasksDetailAction
}

typealias TasksDetailActionHandler = (TasksDetailAction) -> Unit

fun findPreviousTaskAtRequestedLayer(aboveTask: Task, taskList: List<Task>, requestedLayer: Int): Task? {
    var previousTaskAtRequestedLayer: Task? = null
    fun traverse(task: Task) {
        if (requestedLayer == task.taskLayer) {
            previousTaskAtRequestedLayer = task
        } else {
            val parentTask = taskList.find { it.taskId == task.parentTaskId }
            if (parentTask == null) {
                previousTaskAtRequestedLayer = null
            } else {
                traverse(parentTask)
            }
        }
    }
    traverse(aboveTask)
    return previousTaskAtRequestedLayer
}