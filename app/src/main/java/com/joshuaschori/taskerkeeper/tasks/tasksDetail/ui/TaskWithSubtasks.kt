package com.joshuaschori.taskerkeeper.tasks.tasksDetail.ui

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.joshuaschori.taskerkeeper.Constants.DRAG_ALPHA
import com.joshuaschori.taskerkeeper.Constants.ROOT_TIER_TONAL_ELEVATION
import com.joshuaschori.taskerkeeper.Constants.SURFACE_SHADOW_ELEVATION
import com.joshuaschori.taskerkeeper.Constants.TASK_ROW_ICON_TOP_PADDING
import com.joshuaschori.taskerkeeper.Constants.TIER_STEP_SIZE
import com.joshuaschori.taskerkeeper.DragHandler
import com.joshuaschori.taskerkeeper.dragIconModifier
import com.joshuaschori.taskerkeeper.draggableRowModifier
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.Task
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailAction
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailActionHandler
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailExtensionMode

@Composable
fun TaskWithSubtasks(
    task: Task,
    taskList: List<Task>,
    selectedTasksDetailExtensionMode: TasksDetailExtensionMode,
    focusTaskId: Int?,
    isAutoSortCheckedTasks: Boolean,
    lazyListState: LazyListState,
    onScroll: (Float) -> Unit,
    dragHandler: DragHandler,
    actionHandler: TasksDetailActionHandler,
) {
    val dragState by dragHandler.dragState.collectAsState()

    // TODO overscroll / drag and dropping to index 0 / last index?
    // TODO scrolling while dragging
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val isDraggedTask = task.lazyListIndex == dragState.draggedItem?.lazyListIndex

    // focus when task is first created
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        // TODO some kind of bug where sometimes new task doesn't appear until recompose
        // TODO keyboard doesn't pop up, which makes sense
        if (focusTaskId == task.itemId) {
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

    // TODO
    val draggedItemSize = dragState.draggedItemSize
    val snappedDp = dragState.requestedTierChange * TIER_STEP_SIZE.dp.value

    Row(
        modifier = draggableRowModifier(
            itemLazyListIndex = task.lazyListIndex,
            dragHandler = dragHandler
        ).fillMaxWidth()
    ) {
        Surface(
            tonalElevation = if (task.itemTier == 0) {
                ROOT_TIER_TONAL_ELEVATION.dp
            } else {
                0.dp
            },
            shadowElevation = SURFACE_SHADOW_ELEVATION.dp,
            color = if (isDraggedTask && dragState.dragMaxExceeded) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.surface
            },
            modifier = if (isDraggedTask) {
                Modifier
                    .padding(
                        start = (TIER_STEP_SIZE * task.itemTier).dp + snappedDp.dp
                    )
                    .height(with(density) { draggedItemSize.toDp() })
                    .weight(1f)
                    .alpha( if (dragState.dragMaxExceeded) DRAG_ALPHA else 1f )
            } else {
                Modifier
                    .padding(start = (TIER_STEP_SIZE * task.itemTier).dp)
                    .weight(1f)
            }
        ) {
            Row {
                Icon(
                    imageVector = Icons.Filled.DragIndicator,
                    contentDescription = "Rearrange",
                    modifier = dragIconModifier(
                        item = task,
                        itemList = taskList,
                        lazyListState = lazyListState,
                        dragHandler = dragHandler,
                        onDragStart = { _, _, ->
                            actionHandler(TasksDetailAction.ClearFocus)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onDrag = { onDragDragState, _, _, ->
                            actionHandler(TasksDetailAction.OnDrag(onDragDragState.onDragModeChangeTriggerDatabase, onDragDragState))
                        },
                        onDragEnd = {
                            actionHandler(TasksDetailAction.OnDragEnd(it))
                        },
                        onDragCancel = {
                            actionHandler(TasksDetailAction.OnDragCancel)
                        }
                    ).align(Alignment.Top).padding(top = TASK_ROW_ICON_TOP_PADDING.dp)
                )

                Checkbox(
                    checked = task.isChecked,
                    onCheckedChange = {
                        if (task.isChecked) {
                            actionHandler(TasksDetailAction.MarkTaskIncomplete(task.itemId))
                        } else {
                            actionHandler(TasksDetailAction.MarkTaskComplete(task.itemId))
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
                        actionHandler(TasksDetailAction.EditTaskDescription(task.itemId, it))
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .focusRequester(focusRequester)
                        .weight(1f)
                        .alpha( if (isDraggedTask && !dragState.dragMaxExceeded) DRAG_ALPHA else 1f ),
                    interactionSource = interactionSource,
                )

                if (task.numberOfChildren != 0 && task.lazyListIndex != dragState.draggedItem?.lazyListIndex) {
                    IconButton(
                        onClick = {
                            if (task.isExpanded) {
                                actionHandler(TasksDetailAction.MinimizeTask(task.itemId))
                            } else {
                                actionHandler(TasksDetailAction.ExpandTask(task.itemId))
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
                    isDraggedItem = isDraggedTask,
                    dragHandler = dragHandler
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