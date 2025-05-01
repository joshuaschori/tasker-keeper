package com.joshuaschori.taskerkeeper

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import com.joshuaschori.taskerkeeper.Constants.DRAG_MODE_SENSITIVITY
import com.joshuaschori.taskerkeeper.Constants.MAX_TIER_FOR_SUBTASKS
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.roundToInt

// TODO instructions for class
class DragHandler {
    private val _dragState = MutableStateFlow(DragState())
    val dragState: StateFlow<DragState> = _dragState.asStateFlow()

    fun onDragStart(
        offset: Offset,
        lazyListState: LazyListState,
        draggedItem: TieredLazyListDraggableItem
    ) {
        val currentDragState = dragState.value

        val draggedLazyListItem = lazyListState.layoutInfo.visibleItemsInfo.find {
            it.index == draggedItem.lazyListIndex
        }

        _dragState.value = currentDragState.copy(
            draggedItem = draggedItem,
            draggedItemSize = draggedLazyListItem?.size ?: 0,
            draggedLazyListItem = draggedLazyListItem,
            yDragClickOffset = offset.y.toInt(),
        )
    }

    fun onDrag(
        item: TieredLazyListDraggableItem,
        itemList: List<TieredLazyListDraggableItem>,
        lazyListState: LazyListState,
        requestedTierChange: Int,
        change: PointerInputChange,
        dragAmount: Offset,
    ) {
        val currentDragState = dragState.value

        change.consume()

        // TODO scroll??
        // onScroll(dragAmount.y)

        val dragOffsetTotal = (currentDragState.draggedLazyListItem?.offset ?: 0) + currentDragState.yDragClickOffset + currentDragState.yDrag.toInt()

        val updatedDragMode = currentDragState.dragMode ?: if (abs(dragAmount.y) > abs(dragAmount.x)) {
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
                    currentDragState.dragYDirection
                }
            } else {
                null
            }

            val updatedDragTargetIndex = lazyListState.layoutInfo.visibleItemsInfo.find {
                dragOffsetTotal in it.offset..it.offset + it.size
            }?.index ?: currentDragState.dragTargetIndex

            val updatedItemAboveTarget: TieredLazyListDraggableItem? = when (updatedDragMode) {
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

            val updatedItemBelowTarget: TieredLazyListDraggableItem? = when (updatedDragMode) {
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

            val allowedMinimumTier: Int =
                if ( !(updatedItemAboveTarget == null && updatedItemBelowTarget == null) ) {
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
                if ( !(updatedItemAboveTarget == null && updatedItemBelowTarget == null) ) {
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

            //TODO this and previous function could be expressed more clearly
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

            val onDragModeChangeTriggerDatabase: Boolean = currentDragState.dragMode != updatedDragMode

            _dragState.value = currentDragState.copy(
                dragLeftPossible = updatedDragLeftPossible,
                dragMaxExceeded = updatedDragMaxExceeded,
                dragMode = updatedDragMode,
                requestedTierChange = updatedAllowedTierChange,
                dragRightPossible = updatedDragRightPossible,
                dragTargetIndex = updatedDragTargetIndex,
                dragYDirection = updatedDragYDirection,
                itemAboveTarget = updatedItemAboveTarget,
                itemBelowTarget = updatedItemBelowTarget,
                onDragModeChangeTriggerDatabase = onDragModeChangeTriggerDatabase,
                xDrag = currentDragState.xDrag + dragAmount.x,
                yDrag = currentDragState.yDrag + dragAmount.y,
            )
        }
    }

    fun onDragEnd() {
        _dragState.value = DragState()
    }

    fun onDragCancel() {
        _dragState.value = DragState()
    }
}