package com.joshuaschori.taskerkeeper.tasks.tasksDetail.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.Task
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailAction
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailActionHandler
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailExtensionMode

@Composable
fun TaskWithSubtasks(
    task: Task,
    taskLayer: Int,
    selectedTasksDetailExtensionMode: TasksDetailExtensionMode,
    focusTaskId: Int?,
    isAutoSortCheckedTasks: Boolean,
    taskIdBeingDragged: Int?,
    actionHandler: TasksDetailActionHandler,
) {
    var shouldCreateExtraRoomForDraggable by remember { mutableStateOf(false) }
    var xDrag: Float by remember { mutableFloatStateOf(0f) }
    var yDrag: Float by remember { mutableFloatStateOf(0f) }

    Row(
        modifier = Modifier
            .graphicsLayer {
                alpha = (if (taskIdBeingDragged == task.taskId) 0.5f else 1f)
                translationY = yDrag
            }
            .zIndex(if (taskIdBeingDragged == task.taskId) 1f else 0f)
            .padding(top = if (shouldCreateExtraRoomForDraggable) 32.dp else 0.dp)
    ) {
        Surface(
            tonalElevation = if (taskLayer == 0) { 10.dp } else { 0.dp },
            shadowElevation = 5.dp,
            modifier = Modifier
                .padding(start = (32 * taskLayer).dp)
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
                                    actionHandler(TasksDetailAction.SetTaskIdBeingDragged(task.taskId))
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    yDrag += dragAmount.y
                                },
                                onDragEnd = {
                                    actionHandler(TasksDetailAction.SetTaskIdBeingDragged(null))
                                    yDrag = 0f
                                },
                                onDragCancel = {
                                    actionHandler(TasksDetailAction.SetTaskIdBeingDragged(null))
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