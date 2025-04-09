package com.joshuaschori.taskerkeeper.tasks.tasksDetail.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.joshuaschori.taskerkeeper.DragMode
import com.joshuaschori.taskerkeeper.YDirection
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.Task
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailAction
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailActionHandler
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailExtensionMode
import kotlin.math.roundToInt

@Composable
fun TaskWithSubtasks(
    task: Task,
    selectedTasksDetailExtensionMode: TasksDetailExtensionMode,
    focusTaskId: Int?,
    isAutoSortCheckedTasks: Boolean,
    lazyListState: LazyListState,
    draggedLazyListIndex: Int?,
    isDraggedTask: Boolean,
    draggedTaskSize: Int?,
    dragMode: DragMode?,
    dragTargetIndex: Int?,
    dragYDirection: YDirection?,
    dragRequestedLayerChange: Int?,
    dragMaxExceeded: Boolean,
    onScroll: (Float) -> Unit,
    actionHandler: TasksDetailActionHandler,
) {
    // TODO overscroll / drag and dropping to index 0 / last index?
    // TODO scrolling while dragging
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val layerStepSize = 32.dp
    var thisLazyListItem: LazyListItemInfo? by remember { mutableStateOf(null) }
    var yDragClickOffset: Int by remember { mutableIntStateOf(0) }

    // TODO would changing these to Animatable help at all? hover over .graphicsLayer to see example
    var xDrag: Float by remember { mutableFloatStateOf(0f) }
    var yDrag: Float by remember { mutableFloatStateOf(0f) }

    // translate xDrag to the appropriate Px for layered steps
    val xDragDp = with(density) { xDrag.toDp() }
    val requestedLayerChange = rememberUpdatedState ( (xDragDp / layerStepSize).roundToInt() )
    val snappedDp = ( dragRequestedLayerChange ?: 0 ) * layerStepSize.value
    val offsetX = with(density) { snappedDp.dp.toPx() }

    Box(
        modifier = Modifier.zIndex(if (isDraggedTask) 1f else 0f)
    ) {
        DragExtensions(
            isDraggedTask = isDraggedTask,
            draggedLazyListIndex = draggedLazyListIndex,
            dragTargetIndex = dragTargetIndex,
            draggedTaskSize = draggedTaskSize,
            dragMode = dragMode,
            dragYDirection = dragYDirection,
            yDrag = yDrag,
            dragMaxExceeded = dragMaxExceeded,
            task = task,
            layerStepSize = layerStepSize.value,
            snappedDp = snappedDp,
        )
        Row(
            modifier = if (draggedLazyListIndex != null && draggedTaskSize != null) Modifier
                .graphicsLayer {
                    alpha = (if (isDraggedTask) 0.25f else 1f)
                    translationX =
                        if ((isDraggedTask && dragMode == DragMode.CHANGE_LAYER) || (isDraggedTask && dragMode == DragMode.REARRANGE)) {
                            offsetX
                        } else {
                            0f
                        }
                    translationY = if (dragMode == DragMode.REARRANGE && dragTargetIndex != null) {
                        if (isDraggedTask && dragYDirection == YDirection.DOWN && dragTargetIndex == draggedLazyListIndex - 1) {
                            yDrag
                        } else if (isDraggedTask && dragTargetIndex < draggedLazyListIndex) {
                            yDrag - draggedTaskSize
                        } else {
                            yDrag
                        }
                    } else {
                        0f
                    }
                }
                .padding(
                    top = if (dragYDirection == YDirection.UP && task.lazyListIndex == dragTargetIndex
                        && task.lazyListIndex != draggedLazyListIndex && task.lazyListIndex != draggedLazyListIndex + 1
                    ) {
                        with(density) { draggedTaskSize.toDp() }
                    } else if (isDraggedTask && dragMode == DragMode.CHANGE_LAYER) {
                        24.dp
                    } else {
                        0.dp
                    },
                    bottom = if (dragYDirection == YDirection.DOWN && task.lazyListIndex == dragTargetIndex
                        && task.lazyListIndex != draggedLazyListIndex && task.lazyListIndex != draggedLazyListIndex - 1
                    ) {
                        with(density) { draggedTaskSize.toDp() }
                    } else if (isDraggedTask && dragMode == DragMode.CHANGE_LAYER) {
                        24.dp
                    } else {
                        0.dp
                    },
                ) else Modifier
        ) {
            Surface(
                tonalElevation = if (task.taskLayer == 0) {
                    10.dp
                } else {
                    0.dp
                },
                shadowElevation = 5.dp,
                color = if (isDraggedTask && dragMaxExceeded) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                modifier = Modifier
                    .padding(start = (layerStepSize.value * task.taskLayer).dp)
                    .weight(1f)
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Filled.DragIndicator,
                        contentDescription = "Rearrange",
                        modifier = Modifier
                            .align(Alignment.Top)
                            .padding(top = 12.dp)
                            .pointerInput(task.taskId, task.parentTaskId) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { offset ->
                                        actionHandler(TasksDetailAction.ClearFocus)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        yDragClickOffset = offset.y.toInt()
                                        thisLazyListItem =
                                            lazyListState.layoutInfo.visibleItemsInfo.find {
                                                it.index == task.lazyListIndex
                                            }
                                        actionHandler(
                                            TasksDetailAction.OnDragStart(
                                                task = task,
                                                size = thisLazyListItem!!.size
                                            )
                                        )
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        xDrag += dragAmount.x
                                        yDrag += dragAmount.y

                                        // TODO scroll??
                                        // onScroll(dragAmount.y)

                                        actionHandler(
                                            TasksDetailAction.OnDrag(
                                                task = task,
                                                dragAmount = dragAmount,
                                                dragOffsetTotal = thisLazyListItem!!.offset + yDragClickOffset + yDrag.toInt(),
                                                lazyListState = lazyListState,
                                                requestedLayerChange = requestedLayerChange.value
                                            )
                                        )
                                    },
                                    onDragEnd = {
                                        actionHandler(TasksDetailAction.OnDragEnd)
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
}