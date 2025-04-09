package com.joshuaschori.taskerkeeper.tasks.tasksDetail.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.joshuaschori.taskerkeeper.Constants.MAX_LAYER_FOR_SUBTASKS
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.Task
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailAction
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailActionHandler
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailExtensionMode

@Composable
fun TaskExtensions(
    task: Task,
    selectedTasksDetailExtensionMode: TasksDetailExtensionMode,
    isAutoSortCheckedTasks: Boolean,
    actionHandler: TasksDetailActionHandler,
) {
    Row {
        when (selectedTasksDetailExtensionMode) {
            TasksDetailExtensionMode.NORMAL -> {}
            TasksDetailExtensionMode.ADD_NEW_TASK -> {
                IconButton(
                    onClick = {
                        actionHandler(TasksDetailAction.AddNewTask(task.taskId, null))
                        actionHandler(TasksDetailAction.ClearFocus)
                    },
                    enabled = !(isAutoSortCheckedTasks && task.isChecked),
                    modifier = Modifier.alpha(
                        if (isAutoSortCheckedTasks && task.isChecked) 0f else 1f
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Task After",
                    )
                }
            }
            TasksDetailExtensionMode.ADD_NEW_SUBTASK -> {
                IconButton(
                    onClick = {
                        actionHandler(TasksDetailAction.AddNewTask(null, task.taskId))
                        actionHandler(TasksDetailAction.ClearFocus)
                    },
                    enabled = (
                            !((isAutoSortCheckedTasks && task.isChecked)
                                    || task.taskLayer >= MAX_LAYER_FOR_SUBTASKS)
                            ),
                    modifier = Modifier.alpha(
                        if ((isAutoSortCheckedTasks && task.isChecked)
                            || task.taskLayer >= MAX_LAYER_FOR_SUBTASKS
                        ) 0f else 1f
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.SubdirectoryArrowRight,
                        contentDescription = "Add Subtask",
                    )
                }
            }
            TasksDetailExtensionMode.DELETE -> {
                IconButton(
                    onClick = {
                        actionHandler(TasksDetailAction.DeleteTask(task.taskId))
                        actionHandler(TasksDetailAction.ClearFocus)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteForever,
                        contentDescription = "Delete",
                    )
                }
            }
        }
    }
}