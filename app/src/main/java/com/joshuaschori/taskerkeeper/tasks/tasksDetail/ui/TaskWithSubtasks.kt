package com.joshuaschori.taskerkeeper.tasks.tasksDetail.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListScope
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
    lazyListIndexBeingDragged: Int?,
    lazyListTargetIndex: Int?,
    lazyListState: LazyListState,
    draggedTaskSize: Int?,
    dragDirection: String,
    setDraggedTaskSize: (Int?) -> Unit,
    setLazyListIndexBeingDragged: (Int?) -> Unit,
    setLazyListTaskIdBeingDragged: (Int?) -> Unit,
    setLazyListTargetIndex: (Int?) -> Unit,
    setDragDirection: (String) -> Unit,
    actionHandler: TasksDetailActionHandler,
) {
    // TODO overscroll / drag and dropping to index 0 / last index?
    // TODO minimum and maximum steps
    // TODO taskLayer < MAX_LAYERS_OF_SUBTASKS????
    // TODO scrolling while dragging

    val density = LocalDensity.current
    val layerStepSize = 32.dp
    var thisLazyListItem: LazyListItemInfo? by remember { mutableStateOf(null) }
    var yDragOffset: Int by remember { mutableIntStateOf(0) }
    var xDrag: Float by remember { mutableFloatStateOf(0f) }
    var yDrag: Float by remember { mutableFloatStateOf(0f) }
    val xDragDp = with(density) { xDrag.toDp() }
    val requestedLayerChange: Int = (xDragDp / layerStepSize).roundToInt()
    val snappedDp = requestedLayerChange * layerStepSize.value
    val offsetX = with(density) { snappedDp.dp.toPx() }

    Row(
        modifier = if (lazyListIndexBeingDragged != null && lazyListTargetIndex != null && draggedTaskSize != null) Modifier
            .graphicsLayer {
                alpha = (if (lazyListIndexBeingDragged == lazyListIndex) 0.5f else 1f)
                translationX = if (lazyListIndex == lazyListIndexBeingDragged) offsetX else 0f
                translationY =
                    if (dragDirection == "down" && lazyListIndex == lazyListIndexBeingDragged && lazyListTargetIndex == lazyListIndexBeingDragged - 1) {
                        yDrag
                    } else if (lazyListIndex == lazyListIndexBeingDragged && lazyListTargetIndex < lazyListIndexBeingDragged) {
                        yDrag - draggedTaskSize
                    } else {
                        yDrag
                    }
            }
            .zIndex(if (lazyListIndexBeingDragged == lazyListIndex) 1f else 0f)
            .padding(
                top = if (dragDirection == "up" && lazyListIndex == lazyListTargetIndex && lazyListIndex != lazyListIndexBeingDragged && lazyListIndex != lazyListIndexBeingDragged + 1)
                    with(density) { draggedTaskSize.toDp() } else 0.dp,
                bottom =
                if (dragDirection == "down" && lazyListIndex == lazyListTargetIndex && lazyListIndex != lazyListIndexBeingDragged && lazyListIndex != lazyListIndexBeingDragged - 1) {
                    with(density) { draggedTaskSize.toDp() }
                } else 0.dp,
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
                                    yDragOffset = offset.y.toInt()
                                    thisLazyListItem =
                                        lazyListState.layoutInfo.visibleItemsInfo.find {
                                            it.index == lazyListIndex
                                        }
                                    setDraggedTaskSize(thisLazyListItem?.size)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    xDrag += dragAmount.x
                                    setDragDirection(if (dragAmount.y < 0) "up" else "down")
                                    yDrag += dragAmount.y
                                    if (thisLazyListItem != null) {
                                        val targetLazyListItem =
                                            lazyListState.layoutInfo.visibleItemsInfo.find { item ->
                                                thisLazyListItem!!.offset + yDragOffset + yDrag.toInt() in item.offset..item.offset + item.size
                                            }
                                        if (targetLazyListItem != null) {
                                            setLazyListTargetIndex(targetLazyListItem.index)
                                        }
                                    }
                                },
                                onDragEnd = {
                                    // TODO change order of task in database
                                    actionHandler(
                                        TasksDetailAction.RearrangeTasks(
                                            taskId = task.taskId,
                                            aboveDestinationTask = task /*TODO*/,
                                            aboveDestinationTaskLayer = 0 /*TODO*/,
                                            belowDestinationTask = task /*TODO*/,
                                            belowDestinationTaskLayer = 0 /*TODO*/,
                                            requestedLayer = taskLayer + requestedLayerChange,
                                        )
                                    )
                                    setLazyListIndexBeingDragged(null)
                                    setLazyListTaskIdBeingDragged(null)
                                    setLazyListTargetIndex(null)
                                    xDrag = 0f
                                    yDrag = 0f
                                },
                                onDragCancel = {
                                    setLazyListIndexBeingDragged(null)
                                    setLazyListTaskIdBeingDragged(null)
                                    setLazyListTargetIndex(null)
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