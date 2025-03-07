package com.joshuaschori.taskerkeeper.diary.diaryDetail.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.joshuaschori.taskerkeeper.diary.diaryDetail.DiaryDetailAction
import com.joshuaschori.taskerkeeper.diary.diaryDetail.DiaryDetailActionHandler
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailAction
import com.joshuaschori.taskerkeeper.tasks.tasksMenu.TasksMenuAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryDetailTopBar(
    diaryDate: String,
    actionHandler: DiaryDetailActionHandler,
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                text = diaryDate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    actionHandler(DiaryDetailAction.NavigateToDiaryMenu)
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            // TODO empty button
            IconButton(
                onClick = {
                    actionHandler(DiaryDetailAction.ClearFocus)
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