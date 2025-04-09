package com.joshuaschori.taskerkeeper.tasks.tasksDetail.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.joshuaschori.taskerkeeper.DragMode
import com.joshuaschori.taskerkeeper.YDirection
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.Task

@Composable
fun DragExtensions(
    isDraggedTask: Boolean,
    draggedLazyListIndex: Int?,
    dragTargetIndex: Int?,
    draggedTaskSize: Int?,
    dragMode: DragMode?,
    dragYDirection: YDirection?,
    yDrag: Float,
    dragMaxExceeded: Boolean,
    task: Task,
    layerStepSize: Float,
    snappedDp: Float,
) {
    val density = LocalDensity.current

    if (isDraggedTask && draggedTaskSize != null) {
        Row(
            modifier = Modifier
                .graphicsLayer {
                    alpha = 1f
                    translationY = if (dragMode == DragMode.REARRANGE && draggedLazyListIndex != null && dragTargetIndex != null) {
                        if (dragYDirection == YDirection.DOWN && dragTargetIndex == draggedLazyListIndex - 1) {
                            yDrag
                        } else if (dragTargetIndex < draggedLazyListIndex) {
                            yDrag - draggedTaskSize
                        } else {
                            yDrag
                        }
                    } else {
                        0f
                    }
                }
                .padding(
                    start = if ((layerStepSize * task.taskLayer).dp + snappedDp.dp > 0.dp) (layerStepSize * task.taskLayer).dp + snappedDp.dp else 0.dp,
                    top = if (dragMode == DragMode.CHANGE_LAYER) {
                        24.dp
                    } else {
                        0.dp
                    },
                    bottom = if (dragMode == DragMode.CHANGE_LAYER) {
                        24.dp
                    } else {
                        0.dp
                    },
                )
        ) {
            Surface(
                tonalElevation = 0.dp,
                shadowElevation = 5.dp,
                color = if (dragMaxExceeded) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(with(density) { draggedTaskSize.toDp() })
                        .padding(top = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardDoubleArrowLeft,
                        contentDescription = ""
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowUpward,
                        contentDescription = ""
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowDownward,
                        contentDescription = ""
                    )
                    Icon(
                        imageVector = Icons.Filled.KeyboardDoubleArrowRight,
                        contentDescription = ""
                    )
                }
            }
        }
    }
}