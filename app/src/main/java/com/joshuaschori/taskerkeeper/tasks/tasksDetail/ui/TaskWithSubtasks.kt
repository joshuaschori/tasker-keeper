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
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.XYAxis
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.YDirection
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.Task
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailAction
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailActionHandler
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailExtensionMode
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun TaskWithSubtasks(
    task: Task,
    taskLayer: Int,
    selectedTasksDetailExtensionMode: TasksDetailExtensionMode,
    focusTaskId: Int?,
    isAutoSortCheckedTasks: Boolean,
    lazyTaskList: List<Task>,
    lazyListIndex: Int,
    lazyListIndexBeingDragged: Int?,
    lazyListTargetIndex: Int?,
    lazyListState: LazyListState,
    draggedTaskSize: Int?,
    dragOrientation: XYAxis?,
    dragYDirection: YDirection?,
    setDraggedTaskSize: (Int?) -> Unit,
    setLazyListIndexBeingDragged: (Int?) -> Unit,
    setLazyListTaskIdBeingDragged: (Int?) -> Unit,
    setLazyListTargetIndex: (Int?) -> Unit,
    setDragOrientation: (XYAxis?) -> Unit,
    setDragYDirection: (YDirection?) -> Unit,
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

    // TODO these variables needed because while in onDrag, the logic in onDrag won't update from the variables passed into composable?
    var dragOrientationInternal by remember { mutableStateOf<XYAxis?>(null) }
    var lazyListTargetIndexInternal by remember { mutableStateOf<Int?>(null) }
    var dragYDirectionInternal by remember { mutableStateOf<YDirection?>(null) }
    var updatedLazyTaskList = rememberUpdatedState(lazyTaskList)

    Row(
        modifier = if (lazyListIndexBeingDragged != null && lazyListTargetIndex != null && draggedTaskSize != null) Modifier
            .graphicsLayer {
                alpha = (if (lazyListIndexBeingDragged == lazyListIndex) 0.5f else 1f)
                translationX =
                    if (dragOrientation == XYAxis.X && lazyListIndex == lazyListIndexBeingDragged) {
                        if (offsetX < minimumX) {
                            minimumX
                        } else if (offsetX > maximumX) {
                            maximumX
                        } else {
                            offsetX
                        }
                    } else 0f
                translationY = if (dragOrientation == XYAxis.Y) {
                    if (dragYDirection == YDirection.DOWN && lazyListIndex == lazyListIndexBeingDragged && lazyListTargetIndex == lazyListIndexBeingDragged - 1) {
                        yDrag
                    } else if (lazyListIndex == lazyListIndexBeingDragged && lazyListTargetIndex < lazyListIndexBeingDragged) {
                        yDrag - draggedTaskSize
                    } else {
                        yDrag
                    }
                } else 0f
            }
            .zIndex(if (lazyListIndexBeingDragged == lazyListIndex) 1f else 0f)
            .padding(
                top = if (dragYDirection == YDirection.UP
                    && lazyListIndex == lazyListTargetIndex && lazyListIndex != lazyListIndexBeingDragged && lazyListIndex != lazyListIndexBeingDragged + 1
                )
                    with(density) { draggedTaskSize.toDp() } else 0.dp,
                bottom = if (dragYDirection == YDirection.DOWN
                    && lazyListIndex == lazyListTargetIndex && lazyListIndex != lazyListIndexBeingDragged && lazyListIndex != lazyListIndexBeingDragged - 1
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
                        .pointerInput(true) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    setLazyListIndexBeingDragged(lazyListIndex)
                                    setLazyListTaskIdBeingDragged(task.taskId)
                                    yDragClickOffset = offset.y.toInt()
                                    thisLazyListItem =
                                        lazyListState.layoutInfo.visibleItemsInfo.find {
                                            it.index == lazyListIndex
                                        }
                                    setDraggedTaskSize(thisLazyListItem?.size)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    if (dragOrientationInternal == null) {
                                        if (abs(dragAmount.y) > abs(dragAmount.x)) {
                                            setDragOrientation(XYAxis.Y)
                                            dragOrientationInternal = XYAxis.Y
                                            if (dragAmount.y > 0) {
                                                setDragYDirection(YDirection.DOWN)
                                            } else {
                                                setDragYDirection(YDirection.UP)
                                            }
                                        } else {
                                            setDragOrientation(XYAxis.X)
                                            dragOrientationInternal = XYAxis.X
                                        }
                                    }
                                    if (dragOrientationInternal == XYAxis.Y) {
                                        setDragYDirection(if (dragAmount.y < 0) YDirection.UP else YDirection.DOWN)
                                        dragYDirectionInternal = if (dragAmount.y < 0) YDirection.UP else YDirection.DOWN
                                    }
                                    xDrag += dragAmount.x
                                    yDrag += dragAmount.y
                                    if (thisLazyListItem != null) {
                                        val targetLazyListItem =
                                            lazyListState.layoutInfo.visibleItemsInfo.find { item ->
                                                thisLazyListItem!!.offset + yDragClickOffset + yDrag.toInt() in item.offset..item.offset + item.size
                                            }
                                        if (targetLazyListItem != null) {
                                            setLazyListTargetIndex(targetLazyListItem.index)
                                            lazyListTargetIndexInternal = targetLazyListItem.index
                                        }
                                    }
                                },
                                onDragEnd = {
                                    val targetIndex = lazyListTargetIndexInternal
                                    val orientation = dragOrientationInternal
                                    val yDirection = dragYDirectionInternal

                                    if (targetIndex != null && orientation != null) {
                                        when (orientation) {
                                            // TODO account for out of bounds, going up y while out of bounds bottom vice versa
                                            XYAxis.Y -> if (yDirection != null && targetIndex != lazyListIndex) actionHandler(
                                                TasksDetailAction.MoveTaskOrder(
                                                    taskId = task.taskId,
                                                    parentTaskId = task.parentTaskId,
                                                    listOrder = task.listOrder,
                                                    aboveTask = when (yDirection) {
                                                        YDirection.UP ->  if (targetIndex > 0) updatedLazyTaskList.value[targetIndex - 1] else null
                                                        YDirection.DOWN -> updatedLazyTaskList.value[targetIndex]
                                                    },
                                                    belowTask = when (yDirection) {
                                                        YDirection.UP -> updatedLazyTaskList.value[targetIndex]
                                                        YDirection.DOWN -> if (targetIndex + 1 < updatedLazyTaskList.value.size) updatedLazyTaskList.value[targetIndex + 1] else null
                                                    },
                                                    attachUpOrDown = yDirection
                                                )
                                            )
                                            XYAxis.X -> if (requestedLayerChange != 0) {
                                                actionHandler(
                                                    TasksDetailAction.MoveTaskLayer(
                                                        taskId = task.taskId,
                                                        aboveTask = if (lazyListIndex > 0) updatedLazyTaskList.value[lazyListIndex - 1] else null,
                                                        belowTask = if (lazyListIndex < lazyTaskList.size - 1) updatedLazyTaskList.value[lazyListIndex + 1] else null,
                                                        requestedLayer = taskLayer + requestedLayerChange
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    setLazyListIndexBeingDragged(null)
                                    setLazyListTaskIdBeingDragged(null)
                                    setLazyListTargetIndex(null)
                                    setDragOrientation(null)
                                    setDragYDirection(null)
                                    setDraggedTaskSize(null)
                                    dragOrientationInternal = null
                                    lazyListTargetIndexInternal = null
                                    dragYDirectionInternal = null
                                    xDrag = 0f
                                    yDrag = 0f
                                },
                                onDragCancel = {
                                    setLazyListIndexBeingDragged(null)
                                    setLazyListTaskIdBeingDragged(null)
                                    setLazyListTargetIndex(null)
                                    setDragOrientation(null)
                                    setDragYDirection(null)
                                    setDraggedTaskSize(null)
                                    dragOrientationInternal = null
                                    lazyListTargetIndexInternal = null
                                    dragYDirectionInternal = null
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