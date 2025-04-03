package com.joshuaschori.taskerkeeper.tasks.tasksDetail.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.Task
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailAction
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailActionHandler

@Composable
fun TaskRow(
    task: Task,
    focusTaskId: Int?,
    actionHandler: TasksDetailActionHandler,
) {
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
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row {
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
                    .fillMaxWidth(0.75f)
                    .focusRequester(focusRequester),
                interactionSource = interactionSource
            )
        }
        if (task.numberOfChildren != 0) {
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
    }
}