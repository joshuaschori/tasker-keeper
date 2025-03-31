package com.joshuaschori.taskerkeeper.tasks.tasksDetail.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.joshuaschori.taskerkeeper.Constants.MAX_LAYERS_OF_SUBTASKS
import com.joshuaschori.taskerkeeper.DragMode
import com.joshuaschori.taskerkeeper.YDirection
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.Task
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailAction
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailActionHandler
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailExtensionMode
import kotlin.math.roundToInt

@Composable
fun TaskWithSubtasks(
    taskList: List<Task>,
    selectedTasksDetailExtensionMode: TasksDetailExtensionMode,
    focusTaskId: Int?,
    isAutoSortCheckedTasks: Boolean,
    lazyListIndex: Int,
    lazyListState: LazyListState,
    draggedIndex: Int?,
    draggedTaskSize: Int?,
    dragMode: DragMode?,
    dragTargetIndex: Int?,
    dragYDirection: YDirection?,
    actionHandler: TasksDetailActionHandler,
) {
    // TODO overscroll / drag and dropping to index 0 / last index?
    // TODO when subtasks above MAX_LAYERS_OF_SUBTASKS, hide on screen or disallow moving task?
    // TODO scrolling while dragging

    val task = taskList[lazyListIndex]
    val density = LocalDensity.current
    val layerStepSize = 32.dp

    // TODO not sure yet how to handle maximum with moving lists
    val minimumX = with(density) { ((0 - task.taskLayer) * layerStepSize.value).dp.toPx() }
    val maximumX = with(density) { ((MAX_LAYERS_OF_SUBTASKS - task.taskLayer) * layerStepSize.value).dp.toPx() }

    var thisLazyListItem: LazyListItemInfo? by remember { mutableStateOf(null) }
    var yDragClickOffset: Int by remember { mutableIntStateOf(0) }
    var xDrag: Float by remember { mutableFloatStateOf(0f) }
    var yDrag: Float by remember { mutableFloatStateOf(0f) }

    // translate xDrag to the appropriate Px for layered steps
    val xDragDp = with(density) { xDrag.toDp() }
    val requestedLayerChange = rememberUpdatedState ( (xDragDp / layerStepSize).roundToInt() )

    /* TODO experiment, this works in drag */
    val internalDragYDirection = rememberUpdatedState( dragYDirection )

    val targetAboveTask: Task? = if (dragTargetIndex == null || dragMode == null) {
        null
    } else {
        when (dragMode) {
            DragMode.CHANGE_LAYER -> if (lazyListIndex > 0) taskList[lazyListIndex - 1] else null
            DragMode.REARRANGE -> when (dragYDirection) {
                YDirection.UP -> if (dragTargetIndex > 0) taskList[dragTargetIndex - 1] else null
                YDirection.DOWN -> taskList[dragTargetIndex]
                null -> null
            }
        }
    }

    val targetBelowTask: Task? = if (dragTargetIndex == null || dragMode == null) {
        null
    } else {
        when (dragMode) {
            DragMode.CHANGE_LAYER -> if (lazyListIndex < taskList.size - 1) taskList[lazyListIndex + 1] else null
            DragMode.REARRANGE -> when (dragYDirection) {
                YDirection.UP -> taskList[dragTargetIndex]
                YDirection.DOWN -> if (dragTargetIndex + 1 < taskList.size) taskList[dragTargetIndex + 1] else null
                null -> null
            }
        }
    }

    val allowedMinimumLayer: Int = if (dragTargetIndex == null || dragMode == null || (targetAboveTask == null && targetBelowTask == null)) {
        0
    } else {
        when (dragMode) {
            // TODO simplify if statements but include comment with full logic? will need this also for viewModel function
            DragMode.REARRANGE -> if (targetAboveTask == null) {
                0
            } else if (targetBelowTask == null) {
                0
            } else if (targetBelowTask.parentTaskId == targetAboveTask.taskId) {
                targetBelowTask.taskLayer
            } else if (targetBelowTask.parentTaskId == targetAboveTask.parentTaskId) {
                targetBelowTask.taskLayer
            } else if (targetBelowTask.parentTaskId == null) {
                0
            } else {
                // above and below task must be related by extension above
                targetBelowTask.taskLayer
            }
            DragMode.CHANGE_LAYER -> if (targetAboveTask == null) {
                0
            } else if (targetBelowTask == null) {
                0
            } else if (targetBelowTask.parentTaskId == targetAboveTask.taskId) {
                targetBelowTask.taskLayer - 1
            } else if (targetBelowTask.parentTaskId == targetAboveTask.parentTaskId) {
                targetBelowTask.taskLayer - 1
            } else if (targetBelowTask.parentTaskId == null) {
                0
            } else {
                // above and below task must be related by extension above
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
            } else if (targetBelowTask == null) {
                targetAboveTask.taskLayer + 1
            } else if (targetBelowTask.parentTaskId == targetAboveTask.taskId) {
                targetAboveTask.taskLayer + 1
            } else if (targetBelowTask.parentTaskId == targetAboveTask.parentTaskId) {
                targetAboveTask.taskLayer + 1
            } else if (targetBelowTask.parentTaskId == null) {
                targetAboveTask.taskLayer + 1
            } else {
                // above and below task must be related by extension above
                targetAboveTask.taskLayer + 1
            }
            DragMode.CHANGE_LAYER -> if (targetAboveTask == null) {
                0
            } else if (targetBelowTask == null) {
                targetAboveTask.taskLayer + 1
            } else if (targetBelowTask.parentTaskId == targetAboveTask.taskId) {
                targetAboveTask.taskLayer + 1
            } else if (targetBelowTask.parentTaskId == targetAboveTask.parentTaskId) {
                targetAboveTask.taskLayer + 1
            } else if (targetBelowTask.parentTaskId == null) {
                targetAboveTask.taskLayer + 1
            } else {
                // above and below task must be related by extension above
                targetAboveTask.taskLayer + 1
            }
        }
    }

    val allowedLayerChange: Int = if (task.taskLayer + requestedLayerChange.value < allowedMinimumLayer) {
        allowedMinimumLayer - task.taskLayer
    } else if (task.taskLayer + requestedLayerChange.value > allowedMaximumLayer) {
        allowedMaximumLayer - task.taskLayer
    } else {
        requestedLayerChange.value
    }

    val snappedDp = allowedLayerChange * layerStepSize.value
    val offsetX = with(density) { snappedDp.dp.toPx() }

    Row(
        modifier = if (draggedIndex != null && dragTargetIndex != null && draggedTaskSize != null) Modifier
            .graphicsLayer {
                alpha = (if (draggedIndex == lazyListIndex) 0.5f else 1f)
                translationX =
                    if (dragMode == DragMode.CHANGE_LAYER && lazyListIndex == draggedIndex) {
                        if (offsetX < minimumX) {
                            minimumX
                        } else if (offsetX > maximumX) {
                            maximumX
                        } else {
                            offsetX
                        }
                    } else if (dragMode == DragMode.REARRANGE && lazyListIndex == draggedIndex) {
                        if (offsetX < minimumX) {
                            minimumX
                        } else if (offsetX > maximumX) {
                            maximumX
                        } else {
                            offsetX
                        }
                    } else {
                        0f
                    }
                translationY = if (dragMode == DragMode.REARRANGE) {
                    if (dragYDirection == YDirection.DOWN && lazyListIndex == draggedIndex && dragTargetIndex == draggedIndex - 1) {
                        yDrag
                    } else if (lazyListIndex == draggedIndex && dragTargetIndex < draggedIndex) {
                        yDrag - draggedTaskSize
                    } else {
                        yDrag
                    }
                } else {
                    0f
                }
            }
            .zIndex(if (draggedIndex == lazyListIndex) 1f else 0f)
            .padding(
                top = if (dragYDirection == YDirection.UP && lazyListIndex == dragTargetIndex && lazyListIndex != draggedIndex && lazyListIndex != draggedIndex + 1) {
                    with(density) { draggedTaskSize.toDp() }
                } else if (dragMode == DragMode.CHANGE_LAYER && lazyListIndex == draggedIndex) {
                    24.dp
                } else {
                    0.dp
                },
                bottom = if (dragYDirection == YDirection.DOWN && lazyListIndex == dragTargetIndex && lazyListIndex != draggedIndex && lazyListIndex != draggedIndex - 1) {
                    with(density) { draggedTaskSize.toDp() }
                } else if (dragMode == DragMode.CHANGE_LAYER && lazyListIndex == draggedIndex) {
                    24.dp
                } else {
                    0.dp
                },
            ) else Modifier
    ) {
        Surface(
            tonalElevation = if (task.taskLayer == 0) { 10.dp } else { 0.dp },
            shadowElevation = 5.dp,
            modifier = Modifier
                .padding(start = (layerStepSize.value * task.taskLayer).dp)
                .weight(1f)
        ) {
            Row {
                Icon(
                    imageVector = Icons.Filled.DragIndicator,
                    contentDescription = "Rearrange",
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .pointerInput(task.taskId, task.parentTaskId) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    yDragClickOffset = offset.y.toInt()
                                    thisLazyListItem =
                                        lazyListState.layoutInfo.visibleItemsInfo.find {
                                            it.index == lazyListIndex
                                        }
                                    actionHandler(
                                        TasksDetailAction.SetDraggedTask(
                                            taskId = task.taskId,
                                            index = lazyListIndex,
                                            size = thisLazyListItem!!.size
                                        )
                                    )
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    actionHandler(TasksDetailAction.OnDrag(dragAmount = dragAmount))
                                    xDrag += dragAmount.x
                                    yDrag += dragAmount.y
                                    actionHandler(
                                        TasksDetailAction.SetDragTargetIndex(
                                            dragOffsetTotal = thisLazyListItem!!.offset + yDragClickOffset + yDrag.toInt()
                                        )
                                    )
                                },
                                onDragEnd = {

                                    /* TODO try getting correct above and below tasks from composable here !!!
                                    *   avoid repeat logic and put safechecks in view model if necessary
                                    *   or, do I want to just put those in state completely and pull it from state in UI? */

                                    actionHandler(
                                        TasksDetailAction.OnDragEnd(
                                            thisTask = task,
                                            requestedLayerChange = requestedLayerChange.value
                                        )
                                    )
                                    actionHandler(TasksDetailAction.ResetDragHandlers)
                                    xDrag = 0f
                                    yDrag = 0f
                                },
                                onDragCancel = {
                                    actionHandler(TasksDetailAction.ResetDragHandlers)
                                    xDrag = 0f
                                    yDrag = 0f
                                },
                            )
                        },
                )
                TaskRow(
                    task = task,
                    focusTaskId = focusTaskId,
                    actionHandler = actionHandler,
                )
            }
        }
        TaskExtensions(
            task = task,
            actionHandler = actionHandler,
            selectedTasksDetailExtensionMode = selectedTasksDetailExtensionMode,
            isAutoSortCheckedTasks = isAutoSortCheckedTasks,
        )
    }
}