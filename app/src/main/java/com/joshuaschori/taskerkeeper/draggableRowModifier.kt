package com.joshuaschori.taskerkeeper

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.joshuaschori.taskerkeeper.Constants.TIER_CHANGE_PADDING

@Composable
fun draggableRowModifier(
    itemLazyListIndex: Int,
    tierChangePadding: Dp = TIER_CHANGE_PADDING.dp,
    dragHandler: DragHandler
): Modifier {
    val density = LocalDensity.current
    val dragState by dragHandler.dragState.collectAsState()
    val draggedItem = dragState.draggedItem
    val isDraggedItem = itemLazyListIndex == draggedItem?.lazyListIndex

    return if (draggedItem != null) Modifier
        .zIndex(if (isDraggedItem) 1f else 0f)
        .graphicsLayer {
            translationY = if (dragState.dragMode == DragMode.REARRANGE) {
                if (isDraggedItem && dragState.dragYDirection == YDirection.DOWN && dragState.dragTargetIndex == draggedItem.lazyListIndex - 1) {
                    dragState.yDrag
                } else if (isDraggedItem && dragState.dragTargetIndex < draggedItem.lazyListIndex) {
                    dragState.yDrag - dragState.draggedItemSize
                } else if (isDraggedItem) {
                    dragState.yDrag
                } else {
                    0f
                }
            } else {
                0f
            }
        }
        .padding(
            top = if (dragState.dragYDirection == YDirection.UP && itemLazyListIndex == dragState.dragTargetIndex
                && itemLazyListIndex != draggedItem.lazyListIndex && itemLazyListIndex != draggedItem.lazyListIndex + 1
            ) {
                with(density) { dragState.draggedItemSize.toDp() }
            } else if (isDraggedItem && dragState.dragMode == DragMode.CHANGE_TIER) {
                tierChangePadding
            } else {
                0.dp
            },
            bottom = if (dragState.dragYDirection == YDirection.DOWN && itemLazyListIndex == dragState.dragTargetIndex
                && itemLazyListIndex != draggedItem.lazyListIndex && itemLazyListIndex != draggedItem.lazyListIndex - 1
            ) {
                with(density) { dragState.draggedItemSize.toDp() }
            } else if (isDraggedItem && dragState.dragMode == DragMode.CHANGE_TIER) {
                tierChangePadding
            } else {
                0.dp
            },
        )
    else Modifier
}