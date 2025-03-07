package com.joshuaschori.taskerkeeper.tasks.tasksMenu.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
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
import com.joshuaschori.taskerkeeper.data.tasks.TaskCategoryEntity
import com.joshuaschori.taskerkeeper.tasks.tasksMenu.TasksMenuAction
import com.joshuaschori.taskerkeeper.tasks.tasksMenu.TasksMenuActionHandler

@Composable
fun CategoryRow(
    category: TaskCategoryEntity,
    focusCategoryId: Int?,
    actionHandler: TasksMenuActionHandler,
) {
    // focus when task is first created
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        // TODO some kind of bug where sometimes new task doesn't appear until recompose
        // TODO keyboard doesn't pop up, which makes sense
        if (focusCategoryId == category.taskCategoryId) {
            focusRequester.requestFocus()
            actionHandler(TasksMenuAction.ResetFocusTrigger)
        }
    }

    // while text field is being interacted with, update UI immediately and not from database
    val activeTextField = remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    if (!isFocused) {
        activeTextField.value = category.title
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row {
            IconButton(
                onClick = {
                    actionHandler(TasksMenuAction.NavigateToTasksDetail(category.taskCategoryId))
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.NavigateNext,
                    contentDescription = "Navigate"
                )
            }
            BasicTextField(
                value = if (isFocused) {
                    activeTextField.value
                } else {
                    category.title
                },
                onValueChange = {
                    activeTextField.value = it
                    actionHandler(TasksMenuAction.EditCategoryTitle(category.taskCategoryId, it))
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
    }
}