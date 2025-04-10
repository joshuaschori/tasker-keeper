package com.joshuaschori.taskerkeeper.tasks.tasksDetail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontVariation.weight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.joshuaschori.taskerkeeper.Constants.LAZY_COLUMN_END_PADDING
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
    dragLeftPossible: Boolean,
    dragRightPossible: Boolean,
) {
    val density = LocalDensity.current

    if (isDraggedTask && draggedTaskSize != null) {
        Row(
            modifier = if (draggedLazyListIndex != null) Modifier
                .zIndex(2f)
                .graphicsLayer {
                    // TODO alpha = (if (isDraggedTask) 0.25f else 1f)
                    translationY = if (dragMode == DragMode.REARRANGE && dragTargetIndex != null) {
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
                    top = if (dragMode == DragMode.CHANGE_LAYER) 24.dp else 0.dp,
                    bottom = if (dragMode == DragMode.CHANGE_LAYER) 24.dp else 0.dp,
                ) else Modifier
        ) {
            Spacer(
                modifier = Modifier.weight(1f)
            )
            Surface(
                tonalElevation = 0.dp,
                shadowElevation = 5.dp,
            ) {
                Row(
                    modifier = Modifier
                        .height(with(density) { draggedTaskSize.toDp() })
                        .padding(top = 12.dp)
                ) {
                    if (dragMode == DragMode.REARRANGE) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "",
                            modifier = Modifier
                                .alpha( if (dragLeftPossible) 1f else 0.25f )
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.KeyboardDoubleArrowLeft,
                            contentDescription = "",
                            modifier = Modifier
                                .alpha( if (dragLeftPossible) 1f else 0.25f )
                        )
                    }

                    // TODO drag up down possible? beginning and end of list

                    Icon(
                        imageVector = Icons.Filled.ArrowUpward,
                        contentDescription = "",
                        modifier = Modifier
                            .alpha( if (dragMode == DragMode.CHANGE_LAYER) 0f else 1f )
                    )

                    Icon(
                        imageVector = Icons.Filled.ArrowDownward,
                        contentDescription = "",
                        modifier = Modifier
                            .alpha( if (dragMode == DragMode.CHANGE_LAYER) 0f else 1f )
                    )

                    if (dragMode == DragMode.REARRANGE) {
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = "",
                            modifier = Modifier
                                .alpha( if (dragRightPossible) 1f else 0.25f )
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.KeyboardDoubleArrowRight,
                            contentDescription = "",
                            modifier = Modifier
                                .alpha( if (dragRightPossible) 1f else 0.25f )
                        )
                    }
                }
            }
            Spacer(
                modifier = Modifier
                    .height(with(density) { draggedTaskSize.toDp() })
                    .width(LAZY_COLUMN_END_PADDING.dp)
                    .background(MaterialTheme.colorScheme.surface)
            )
        }
    }
}
