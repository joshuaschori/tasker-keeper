package com.joshuaschori.taskerkeeper.tasks.tasksDetail.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.joshuaschori.taskerkeeper.Constants.TASK_ROW_ICON_TOP_PADDING
import com.joshuaschori.taskerkeeper.DragHandler
import com.joshuaschori.taskerkeeper.DragMode

@Composable
fun DragExtensions(
    isDraggedItem: Boolean,
    dragHandler: DragHandler
) {
    val dragState by dragHandler.dragState.collectAsState()

    if (isDraggedItem) {
        Row(
            modifier = Modifier.padding(top = TASK_ROW_ICON_TOP_PADDING.dp)
        ) {
            if (dragState.dragMode == DragMode.REARRANGE) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "",
                    modifier = Modifier.alpha( if (dragState.dragLeftPossible) 1f else 0.25f )
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.KeyboardDoubleArrowLeft,
                    contentDescription = "",
                    modifier = Modifier.alpha( if (dragState.dragLeftPossible) 1f else 0.25f )
                )
            }

            // TODO visual indication of drag up down possible? at beginning and end of list

            Icon(
                imageVector = Icons.Filled.ArrowUpward,
                contentDescription = "",
                modifier = Modifier.alpha( if (dragState.dragMode == DragMode.CHANGE_TIER) 0f else 1f )
            )

            Icon(
                imageVector = Icons.Filled.ArrowDownward,
                contentDescription = "",
                modifier = Modifier.alpha( if (dragState.dragMode == DragMode.CHANGE_TIER) 0f else 1f )
            )

            if (dragState.dragMode == DragMode.REARRANGE) {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = "",
                    modifier = Modifier.alpha( if (dragState.dragRightPossible) 1f else 0.25f )
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.KeyboardDoubleArrowRight,
                    contentDescription = "",
                    modifier = Modifier.alpha( if (dragState.dragRightPossible) 1f else 0.25f )
                )
            }
        }
    }
}