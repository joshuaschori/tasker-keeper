package com.joshuaschori.taskerkeeper.tasks.tasksDetail.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joshuaschori.taskerkeeper.Constants.MAX_LAYERS_OF_SUBTASKS
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.Task
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailActionHandler
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailExtensionMode

@Composable
fun TaskAndSubtasks(
    task: Task,
    taskLayer: Int,
    selectedTasksDetailExtensionMode: TasksDetailExtensionMode,
    focusTaskId: Int?,
    isAutoSortCheckedTasks: Boolean,
    actionHandler: TasksDetailActionHandler,
) {
    Row {
        Surface(
            tonalElevation = if (taskLayer == 0) { 10.dp } else { 0.dp },
            shadowElevation = 5.dp,
            modifier = Modifier
                .padding(start = (32 * taskLayer).dp, top = 1.dp)
                .weight(1f)
        ) {
            TaskRow(
                task = task,
                focusTaskId = focusTaskId,
                actionHandler = actionHandler,
            )
        }
        TaskExtensions(
            task = task,
            taskLayer = taskLayer,
            actionHandler = actionHandler,
            selectedTasksDetailExtensionMode = selectedTasksDetailExtensionMode,
            isAutoSortCheckedTasks = isAutoSortCheckedTasks,
        )
    }
    if (task.subtaskList.isNotEmpty() && task.isExpanded && taskLayer < MAX_LAYERS_OF_SUBTASKS) {
        for (subtask in task.subtaskList) {
            TaskAndSubtasks(
                task = subtask,
                taskLayer = taskLayer + 1,
                selectedTasksDetailExtensionMode = selectedTasksDetailExtensionMode,
                focusTaskId = focusTaskId,
                isAutoSortCheckedTasks = isAutoSortCheckedTasks,
                actionHandler = actionHandler,
            )
        }
    }
}