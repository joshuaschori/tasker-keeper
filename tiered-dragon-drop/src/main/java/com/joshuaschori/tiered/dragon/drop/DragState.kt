package com.joshuaschori.tiered.dragon.drop

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

data class DragState(
    val draggedItem: TieredLazyListDraggableItem? = null,
    val draggedItemSize: Int = 0,
    val draggedLazyListItem: LazyListItemInfo? = null,
    val dragLeftPossible: Boolean = false,
    val dragMaxExceeded: Boolean = false,
    val dragMode: DragMode? = null,
    val dragRightPossible: Boolean = false,
    val dragTargetIndex: Int = 0,
    val dragYDirection: YDirection? = null,
    val itemAboveTarget: TieredLazyListDraggableItem? = null,
    val itemBelowTarget: TieredLazyListDraggableItem? = null,
    val onDragModeChangeTriggerDatabase: Boolean = false,
    val requestedTierChange: Int = 0,
    val xDrag: Float = 0f,
    val yDrag: Float = 0f,
    val yDragClickOffset: Int = 0,
)