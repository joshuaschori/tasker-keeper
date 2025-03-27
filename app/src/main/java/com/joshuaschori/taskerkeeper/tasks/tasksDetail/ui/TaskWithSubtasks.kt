package com.joshuaschori.taskerkeeper.tasks.tasksDetail.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.joshuaschori.taskerkeeper.Constants.MAX_LAYERS_OF_SUBTASKS
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.XYAxis
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.YDirection
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.Task
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailAction
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailActionHandler
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailExtensionMode
import kotlin.math.roundToInt

@Composable
fun TaskWithSubtasks(
    task: Task,
    taskLayer: Int,
    selectedTasksDetailExtensionMode: TasksDetailExtensionMode,
    focusTaskId: Int?,
    isAutoSortCheckedTasks: Boolean,
    lazyListIndex: Int,
    lazyListState: LazyListState,
    draggedIndex: Int?,
    draggedTaskSize: Int?,
    dragOrientation: XYAxis?,
    dragTargetIndex: Int?,
    dragYDirection: YDirection?,
    actionHandler: TasksDetailActionHandler,
) {
    // TODO overscroll / drag and dropping to index 0 / last index?
    // TODO when subtasks above MAX_LAYERS_OF_SUBTASKS, hide on screen or disallow moving task?
    // TODO scrolling while dragging
    // TODO visual indicator of drag up and down attaching to layer of respective above or below task

    val density = LocalDensity.current
    val layerStepSize = 32.dp
    var thisLazyListItem: LazyListItemInfo? by remember { mutableStateOf(null) }
    var yDragClickOffset: Int by remember { mutableIntStateOf(0) }
    var xDrag: Float by remember { mutableFloatStateOf(0f) }
    var yDrag: Float by remember { mutableFloatStateOf(0f) }

    // translate xDrag to the appropriate Px for layered steps
    val xDragDp = with(density) { xDrag.toDp() }
    val requestedLayerChange: Int = (xDragDp / layerStepSize).roundToInt()
    val snappedDp = requestedLayerChange * layerStepSize.value
    val offsetX = with(density) { snappedDp.dp.toPx() }
    val minimumX = with(density) { ((0 - taskLayer) * layerStepSize.value).dp.toPx() }
    val maximumX = with(density) { ((MAX_LAYERS_OF_SUBTASKS - taskLayer) * layerStepSize.value).dp.toPx() }

    Row(
        modifier = if (draggedIndex != null && dragTargetIndex != null && draggedTaskSize != null) Modifier
            .graphicsLayer {
                alpha = (if (draggedIndex == lazyListIndex) 0.5f else 1f)
                translationX =
                    if (dragOrientation == XYAxis.X && lazyListIndex == draggedIndex) {
                        if (offsetX < minimumX) {
                            minimumX
                        } else if (offsetX > maximumX) {
                            maximumX
                        } else {
                            offsetX
                        }
                    } else 0f
                translationY = if (dragOrientation == XYAxis.Y) {
                    if (dragYDirection == YDirection.DOWN && lazyListIndex == draggedIndex && dragTargetIndex == draggedIndex - 1) {
                        yDrag
                    } else if (lazyListIndex == draggedIndex && dragTargetIndex < draggedIndex) {
                        yDrag - draggedTaskSize
                    } else {
                        yDrag
                    }
                } else 0f
            }
            .zIndex(if (draggedIndex == lazyListIndex) 1f else 0f)
            .padding(
                top = if (dragYDirection == YDirection.UP
                    && lazyListIndex == dragTargetIndex && lazyListIndex != draggedIndex && lazyListIndex != draggedIndex + 1
                )
                    with(density) { draggedTaskSize.toDp() } else 0.dp,
                bottom = if (dragYDirection == YDirection.DOWN
                    && lazyListIndex == dragTargetIndex && lazyListIndex != draggedIndex && lazyListIndex != draggedIndex - 1
                )
                    with(density) { draggedTaskSize.toDp() } else 0.dp,
            ) else Modifier
    ) {
        Surface(
            tonalElevation = if (taskLayer == 0) { 10.dp } else { 0.dp },
            shadowElevation = 5.dp,
            modifier = Modifier
                .padding(start = (layerStepSize.value * taskLayer).dp)
                .weight(1f)
        ) {
            Row {
                Icon(
                    imageVector = Icons.Filled.DragIndicator,
                    contentDescription = "Rearrange",
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .pointerInput(task.taskId) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    yDragClickOffset = offset.y.toInt()
                                    thisLazyListItem = lazyListState.layoutInfo.visibleItemsInfo.find {
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
                                    actionHandler(
                                        TasksDetailAction.OnDragEnd(
                                            lazyListIndex = lazyListIndex,
                                            task = task,
                                            taskLayer = taskLayer,
                                            requestedLayerChange = requestedLayerChange
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
            taskLayer = taskLayer,
            actionHandler = actionHandler,
            selectedTasksDetailExtensionMode = selectedTasksDetailExtensionMode,
            isAutoSortCheckedTasks = isAutoSortCheckedTasks,
        )
    }
}