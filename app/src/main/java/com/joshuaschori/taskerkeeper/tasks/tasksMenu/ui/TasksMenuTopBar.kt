package com.joshuaschori.taskerkeeper.tasks.tasksMenu.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import com.joshuaschori.taskerkeeper.tasks.tasksMenu.TasksMenuAction
import com.joshuaschori.taskerkeeper.tasks.tasksMenu.TasksMenuActionHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksMenuTopBar(
    actionHandler: TasksMenuActionHandler,
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
        // TODO empty button
        navigationIcon = {

        },
        actions = {
            // TODO empty button
            IconButton(
                onClick = {
                    actionHandler(TasksMenuAction.ClearFocus)
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