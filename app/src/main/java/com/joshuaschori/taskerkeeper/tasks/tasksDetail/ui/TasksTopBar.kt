package com.joshuaschori.taskerkeeper.tasks.tasksDetail.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailAction
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailActionHandler
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailExtensionMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksTopBar(
    selectedTasksDetailExtensionMode: TasksDetailExtensionMode,
    actionHandler: TasksDetailActionHandler,
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                "Tasks",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    actionHandler(TasksDetailAction.NavigateToTasksMenu)
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                TasksDetailExtensionMode.entries.forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = label == selectedTasksDetailExtensionMode,
                        onClick = {
                            actionHandler(TasksDetailAction.ChangeTasksDetailExtensionMode(label))
                            actionHandler(TasksDetailAction.ClearFocus)
                        },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = TasksDetailExtensionMode.entries.size
                        ),
                        icon = {},
                        label = {
                            when (label) {
                                TasksDetailExtensionMode.NORMAL -> Icon(
                                    imageVector = Icons.Filled.NotInterested,
                                    contentDescription = TasksDetailExtensionMode.NORMAL.contentDescription,
                                )
                                TasksDetailExtensionMode.ADD_NEW_TASK -> Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = TasksDetailExtensionMode.ADD_NEW_TASK.contentDescription,
                                )
                                TasksDetailExtensionMode.ADD_NEW_SUBTASK -> Icon(
                                    imageVector = Icons.Filled.SubdirectoryArrowRight,
                                    contentDescription = TasksDetailExtensionMode.ADD_NEW_SUBTASK.contentDescription,
                                )
                                TasksDetailExtensionMode.REARRANGE -> Icon(
                                    imageVector = Icons.Filled.DragHandle,
                                    contentDescription = TasksDetailExtensionMode.REARRANGE.contentDescription,
                                )
                                TasksDetailExtensionMode.DELETE -> Icon(
                                    imageVector = Icons.Filled.DeleteForever,
                                    contentDescription = TasksDetailExtensionMode.DELETE.contentDescription,
                                )
                            }
                        }
                    )
                }
            }
            // TODO empty button
            IconButton(
                onClick = {
                    actionHandler(TasksDetailAction.ClearFocus)
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More Options"
                )
            }
        },
    )
}