package com.joshuaschori.taskerkeeper.tasks.tasksDetail.ui

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.joshuaschori.taskerkeeper.Constants.TASK_ROW_ICON_TOP_PADDING
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
    dragLeftPossible: Boolean,
    dragRightPossible: Boolean,
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

    // focus when task is first created
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        // TODO some kind of bug where sometimes new task doesn't appear until recompose
        // TODO keyboard doesn't pop up, which makes sense
        if (focusTaskId == task.taskId) {
            focusRequester.requestFocus()
            actionHandler(TasksDetailAction.ResetFocusTrigger)
        }
    }

    // while text field is being interacted with, update UI immediately and not from database
    val activeTextField = remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    if (!isFocused) {
        activeTextField.value = task.description
    }

    Row(
        modifier = if (draggedLazyListIndex != null && draggedTaskSize != null) Modifier
            .zIndex(if (isDraggedTask) 1f else 0f)
            .graphicsLayer {
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
            )
            .fillMaxWidth()
        else Modifier.fillMaxWidth()
    ) {
        Surface(
            tonalElevation = if (task.taskLayer == 0) {
                10.dp
            } else {
                0.dp
            },
            shadowElevation = 5.dp,
            color = if (isDraggedTask && dragMaxExceeded) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.surface
            },
            modifier = if (isDraggedTask && draggedTaskSize != null) {
                Modifier
                    .padding(
                        start = (layerStepSize.value * task.taskLayer).dp + snappedDp.dp
                    )
                    .height(with(density) { draggedTaskSize.toDp() })
                    .weight(1f)
                    .alpha( if (dragMaxExceeded) 0.25f else 1f )
            } else {
                Modifier
                    .padding(start = (layerStepSize.value * task.taskLayer).dp)
                    .weight(1f)
            }
        ) {
            Row {
                Icon(
                    imageVector = Icons.Filled.DragIndicator,
                    contentDescription = "Rearrange",
                    modifier = Modifier
                        .align(Alignment.Top)
                        .padding(top = TASK_ROW_ICON_TOP_PADDING.dp)
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

                Checkbox(
                    checked = task.isChecked,
                    onCheckedChange = {
                        if (task.isChecked) {
                            actionHandler(TasksDetailAction.MarkTaskIncomplete(task.taskId))
                        } else {
                            actionHandler(TasksDetailAction.MarkTaskComplete(task.taskId))
                        }
                        actionHandler(TasksDetailAction.ClearFocus)
                    },
                )

                BasicTextField(
                    value = if (isFocused) {
                        activeTextField.value
                    } else {
                        task.description
                    },
                    onValueChange = {
                        activeTextField.value = it
                        actionHandler(TasksDetailAction.EditTaskDescription(task.taskId, it))
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .focusRequester(focusRequester)
                        .weight(1f)
                        .alpha( if (isDraggedTask) 0.25f else 1f ),
                    interactionSource = interactionSource,
                )

                if (task.numberOfChildren != 0 && !isDraggedTask) {
                    IconButton(
                        onClick = {
                            if (task.isExpanded) {
                                actionHandler(TasksDetailAction.MinimizeTask(task.taskId))
                            } else {
                                actionHandler(TasksDetailAction.ExpandTask(task.taskId))
                            }
                            actionHandler(TasksDetailAction.ClearFocus)
                        },
                    ) {
                        Icon(
                            if (task.isExpanded) {
                                Icons.Filled.ExpandLess
                            } else {
                                Icons.Filled.ExpandMore
                            },
                            contentDescription = if (task.isExpanded) {
                                "Minimize Subtasks"
                            } else {
                                "Expand Subtasks"
                            },
                        )
                    }
                }

                DragExtensions(
                    isDraggedTask = isDraggedTask,
                    draggedTaskSize = draggedTaskSize,
                    dragMode = dragMode,
                    dragLeftPossible = dragLeftPossible,
                    dragRightPossible = dragRightPossible,
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