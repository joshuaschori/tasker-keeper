package com.joshuaschori.taskerkeeper

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.joshuaschori.taskerkeeper.Constants.DRAG_MODE_SENSITIVITY
import com.joshuaschori.taskerkeeper.Constants.MAX_TIER_FOR_SUBTASKS
import kotlin.math.abs

data class DragHandler(
    val draggedItem: TieredLazyListDraggableItem? = null,
    val draggedItemSize: Int? = null,
    val dragLeftPossible: Boolean = false,
    val dragMaxExceeded: Boolean = false,
    val dragMode: DragMode? = null,
    val dragRightPossible: Boolean = false,
    val dragTargetIndex: Int? = null,
    val dragYDirection: YDirection? = null,
    val itemAboveTarget: TieredLazyListDraggableItem? = null,
    val itemBelowTarget: TieredLazyListDraggableItem? = null,
    val requestedTierChange: Int? = null,
) {
    @Composable
    fun dragRowModifier(
        taskLazyListIndex: Int,
        isDraggedItem: Boolean,
        yDrag: Float
    ): Modifier {
        val density = LocalDensity.current
        return if (draggedItem != null && draggedItemSize != null) Modifier
            .zIndex(if (isDraggedItem) 1f else 0f)
            .graphicsLayer {
                translationY = if (dragMode == DragMode.REARRANGE && dragTargetIndex != null) {
                    if (isDraggedItem && dragYDirection == YDirection.DOWN && dragTargetIndex == draggedItem.lazyListIndex - 1) {
                        yDrag
                    } else if (isDraggedItem && dragTargetIndex < draggedItem.lazyListIndex) {
                        yDrag - draggedItemSize
                    } else {
                        yDrag
                    }
                } else {
                    0f
                }
            }
            .padding(
                top = if (dragYDirection == YDirection.UP && taskLazyListIndex == dragTargetIndex
                    && taskLazyListIndex != draggedItem.lazyListIndex && taskLazyListIndex != draggedItem.lazyListIndex + 1
                ) {
                    with(density) { draggedItemSize.toDp() }
                } else if (isDraggedItem && dragMode == DragMode.CHANGE_TIER) {
                    24.dp
                } else {
                    0.dp
                },
                bottom = if (dragYDirection == YDirection.DOWN && taskLazyListIndex == dragTargetIndex
                    && taskLazyListIndex != draggedItem.lazyListIndex && taskLazyListIndex != draggedItem.lazyListIndex - 1
                ) {
                    with(density) { draggedItemSize.toDp() }
                } else if (isDraggedItem && dragMode == DragMode.CHANGE_TIER) {
                    24.dp
                } else {
                    0.dp
                },
            )
        else Modifier
    }

    fun updateOnDrag(
        item: TieredLazyListDraggableItem,
        itemList: List<TieredLazyListDraggableItem>,
        dragAmount: Offset,
        dragOffsetTotal: Int,
        lazyListState: LazyListState,
        requestedTierChange: Int
    ): DragHandler? {
        val updatedDragMode = dragMode ?: if (abs(dragAmount.y) > abs(dragAmount.x)) {
            if (abs(dragAmount.y) > DRAG_MODE_SENSITIVITY) DragMode.REARRANGE else null
        } else {
            if (abs(dragAmount.x) > DRAG_MODE_SENSITIVITY) DragMode.CHANGE_TIER else null
        }

        if (updatedDragMode != null) {
            val updatedDragYDirection = if (updatedDragMode == DragMode.REARRANGE) {
                if (abs(dragAmount.y) > abs(dragAmount.x)) {
                    if (dragAmount.y > 0) {
                        YDirection.DOWN
                    } else {
                        YDirection.UP
                    }
                } else {
                    dragYDirection
                }
            } else {
                null
            }

            val updatedDragTargetIndex = lazyListState.layoutInfo.visibleItemsInfo.find { item ->
                dragOffsetTotal in item.offset..item.offset + item.size
            }?.index ?: dragTargetIndex

            val updatedItemAboveTarget: TieredLazyListDraggableItem? = if (updatedDragTargetIndex != null) {
                when (updatedDragMode) {
                    DragMode.CHANGE_TIER -> if (item.lazyListIndex > 0) itemList[item.lazyListIndex - 1] else null
                    DragMode.REARRANGE -> if (item.lazyListIndex == updatedDragTargetIndex) {
                        if (item.lazyListIndex > 0) itemList[item.lazyListIndex - 1] else null
                    } else {
                        when (updatedDragYDirection) {
                            YDirection.UP -> if (updatedDragTargetIndex > 0) itemList[updatedDragTargetIndex - 1] else null
                            YDirection.DOWN -> itemList[updatedDragTargetIndex]
                            null -> null
                        }
                    }
                }
            } else {
                null
            }

            val updatedItemBelowTarget: TieredLazyListDraggableItem? = if (updatedDragTargetIndex != null) {
                when (updatedDragMode) {
                    DragMode.CHANGE_TIER -> if (item.lazyListIndex < itemList.size - 1) itemList[item.lazyListIndex + 1] else null
                    DragMode.REARRANGE -> if (item.lazyListIndex == updatedDragTargetIndex) {
                        if (item.lazyListIndex < itemList.size - 1) itemList[item.lazyListIndex + 1] else null
                    } else {
                        when (updatedDragYDirection) {
                            YDirection.UP -> itemList[updatedDragTargetIndex]
                            YDirection.DOWN -> if (updatedDragTargetIndex + 1 < itemList.size) itemList[updatedDragTargetIndex + 1] else null
                            null -> null
                        }
                    }
                }
            } else {
                null
            }

            val allowedMinimumTier: Int =
                if (updatedDragTargetIndex != null && !(updatedItemAboveTarget == null && updatedItemBelowTarget == null)) {
                    when (updatedDragMode) {
                        DragMode.REARRANGE -> if (updatedItemAboveTarget != null && updatedItemBelowTarget != null && updatedItemBelowTarget.parentItemId != null) {
                            updatedItemBelowTarget.itemTier
                        } else {
                            0
                        }

                        DragMode.CHANGE_TIER -> if (updatedItemAboveTarget != null && updatedItemBelowTarget != null && updatedItemBelowTarget.parentItemId != null) {
                            updatedItemBelowTarget.itemTier - 1
                        } else {
                            0
                        }
                    }
                } else {
                    0
                }

            val allowedMaximumTier: Int =
                if (updatedDragTargetIndex != null && !(updatedItemAboveTarget == null && updatedItemBelowTarget == null)) {
                    when (updatedDragMode) {
                        DragMode.REARRANGE -> if (updatedItemAboveTarget != null) {
                            if (updatedItemAboveTarget.itemTier + 1 + item.highestTierBelow - item.itemTier > MAX_TIER_FOR_SUBTASKS) {
                                MAX_TIER_FOR_SUBTASKS - item.highestTierBelow + item.itemTier
                            } else {
                                updatedItemAboveTarget.itemTier + 1
                            }
                        } else {
                            0
                        }

                        DragMode.CHANGE_TIER -> if (updatedItemAboveTarget != null) {
                            if (updatedItemAboveTarget.itemTier + 1 > MAX_TIER_FOR_SUBTASKS) {
                                MAX_TIER_FOR_SUBTASKS
                            } else {
                                updatedItemAboveTarget.itemTier + 1
                            }
                        } else {
                            0
                        }
                    }
                } else {
                    0
                }

            val updatedAllowedTierChange: Int = if (
                item.itemTier + requestedTierChange < allowedMinimumTier ||
                allowedMaximumTier < allowedMinimumTier
            ) {
                allowedMinimumTier - item.itemTier
            } else if (item.itemTier + requestedTierChange > allowedMaximumTier) {
                allowedMaximumTier - item.itemTier
            } else {
                requestedTierChange
            }

            val updatedDragMaxExceeded =
                (updatedDragMode == DragMode.REARRANGE && item.highestTierBelow + updatedAllowedTierChange > MAX_TIER_FOR_SUBTASKS) ||
                        (updatedDragMode == DragMode.CHANGE_TIER && allowedMinimumTier == item.itemTier && allowedMaximumTier == item.itemTier)

            val updatedDragLeftPossible = item.itemTier + updatedAllowedTierChange > allowedMinimumTier

            val updatedDragRightPossible = item.itemTier + updatedAllowedTierChange < allowedMaximumTier

            return DragHandler(
                draggedItem = draggedItem,
                draggedItemSize = draggedItemSize,
                dragLeftPossible = updatedDragLeftPossible,
                dragMaxExceeded = updatedDragMaxExceeded,
                dragMode = updatedDragMode,
                requestedTierChange = updatedAllowedTierChange,
                dragRightPossible = updatedDragRightPossible,
                dragTargetIndex = updatedDragTargetIndex,
                dragYDirection = updatedDragYDirection,
                itemAboveTarget = updatedItemAboveTarget,
                itemBelowTarget = updatedItemBelowTarget,
            )
        } else {
            return null
        }
    }
}