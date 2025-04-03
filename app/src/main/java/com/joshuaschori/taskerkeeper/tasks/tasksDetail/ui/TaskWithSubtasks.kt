package com.joshuaschori.taskerkeeper.tasks.tasksDetail.ui

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
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
    task: Task,
    selectedTasksDetailExtensionMode: TasksDetailExtensionMode,
    focusTaskId: Int?,
    isAutoSortCheckedTasks: Boolean,
    lazyListState: LazyListState,
    draggedTask: Task?,
    draggedTaskSize: Int?,
    dragMode: DragMode?,
    dragTargetIndex: Int?,
    dragYDirection: YDirection?,
    dragRequestedLayerChange: Int?,
    onScroll: (Float) -> Unit,
    actionHandler: TasksDetailActionHandler,
) {
    // TODO overscroll / drag and dropping to index 0 / last index?
    // TODO when subtasks above MAX_LAYERS_OF_SUBTASKS, hide on screen or disallow moving task?
    // TODO scrolling while dragging
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val layerStepSize = 32.dp
    var thisLazyListItem: LazyListItemInfo? by remember { mutableStateOf(null) }
    var yDragClickOffset: Int by remember { mutableIntStateOf(0) }
    var xDrag: Float by remember { mutableFloatStateOf(0f) }
    var yDrag: Float by remember { mutableFloatStateOf(0f) }

    // translate xDrag to the appropriate Px for layered steps
    val xDragDp = with(density) { xDrag.toDp() }
    val requestedLayerChange = rememberUpdatedState ( (xDragDp / layerStepSize).roundToInt() )
    val snappedDp = ( dragRequestedLayerChange ?: 0 ) * layerStepSize.value
    val offsetX = with(density) { snappedDp.dp.toPx() }

    // TODO not sure yet how to handle maximum with moving lists
    val minimumX = with(density) { ((0 - task.taskLayer) * layerStepSize.value).dp.toPx() }
    val maximumX = with(density) { ((MAX_LAYERS_OF_SUBTASKS - task.taskLayer) * layerStepSize.value).dp.toPx() }

    Row(
        modifier = if (draggedTask != null && dragTargetIndex != null && draggedTaskSize != null) Modifier
            .graphicsLayer {
                alpha = (if (task == draggedTask) 0.5f else 1f)
                translationX =
                    if (task == draggedTask && dragMode == DragMode.CHANGE_LAYER) {
                        if (offsetX < minimumX) { minimumX }
                        else if (offsetX > maximumX) { maximumX }
                        else { offsetX }
                    } else if (task == draggedTask && dragMode == DragMode.REARRANGE) {
                        if (offsetX < minimumX) { minimumX }
                        else if (offsetX > maximumX) { maximumX }
                        else { offsetX }
                    } else { 0f }
                translationY = if (dragMode == DragMode.REARRANGE) {
                    if (task == draggedTask && dragYDirection == YDirection.DOWN && dragTargetIndex == draggedTask.lazyListIndex - 1) {
                        yDrag
                    } else if (task == draggedTask && dragTargetIndex < draggedTask.lazyListIndex) {
                        yDrag - draggedTaskSize
                    } else { yDrag }
                } else { 0f }
            }
            .zIndex(if (task == draggedTask) 1f else 0f)
            .padding(
                top = if (dragYDirection == YDirection.UP && task.lazyListIndex == dragTargetIndex
                    && task.lazyListIndex != draggedTask.lazyListIndex && task.lazyListIndex != draggedTask.lazyListIndex + 1
                ) {
                    with(density) { draggedTaskSize.toDp() }
                } else if (task == draggedTask && dragMode == DragMode.CHANGE_LAYER) {
                    24.dp
                } else { 0.dp },
                bottom = if (dragYDirection == YDirection.DOWN && task.lazyListIndex == dragTargetIndex
                    && task.lazyListIndex != draggedTask.lazyListIndex && task.lazyListIndex != draggedTask.lazyListIndex - 1
                ) {
                    with(density) { draggedTaskSize.toDp() }
                } else if (task == draggedTask && dragMode == DragMode.CHANGE_LAYER) {
                    24.dp
                } else { 0.dp },
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