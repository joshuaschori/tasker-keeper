package com.joshuaschori.taskerkeeper

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.joshuaschori.taskerkeeper.Constants.TIER_STEP_SIZE
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailAction
import kotlin.math.roundToInt

@Composable
fun dragIconModifier(
    item: TieredLazyListDraggableItem,
    itemList: List<TieredLazyListDraggableItem>,
    lazyListState: LazyListState,
    dragHandler: DragHandler,
    pointerInputKey1: Any? = item.itemId,
    pointerInputKey2: Any? = item.parentItemId,
    onDragStart: (DragState, Offset) -> Unit = { _, _ -> },
    onDrag: (DragState, PointerInputChange, Offset) -> Unit = { _, _, _ -> },
    onDragEnd: (DragState) -> Unit = {},
    onDragCancel: (DragState) -> Unit = {},
): Modifier {
    val density = LocalDensity.current
    val dragState by dragHandler.dragState.collectAsState()
    val updatedLazyListState by rememberUpdatedState(lazyListState)
    val updatedItemList by rememberUpdatedState(itemList)

    // translate xDrag to the appropriate Px for tiered steps
    val xDragDp = with(density) { dragState.xDrag.toDp() }
    val requestedTierChange = rememberUpdatedState((xDragDp / TIER_STEP_SIZE.dp).roundToInt())

    return Modifier
        .pointerInput(pointerInputKey1, pointerInputKey2) {
            detectDragGesturesAfterLongPress(
                onDragStart = { offset ->
                    onDragStart(dragState, offset)
                    dragHandler.onDragStart(
                        offset = offset,
                        lazyListState = lazyListState,
                        draggedItem = item
                    )
                },
                onDrag = { change, dragAmount ->
                    dragHandler.onDrag(
                        item = item,
                        itemList = updatedItemList,
                        dragAmount = dragAmount,
                        lazyListState = updatedLazyListState,
                        requestedTierChange = requestedTierChange.value,
                        change = change
                    )
                    onDrag(dragState, change, dragAmount)
                },
                onDragEnd = {
                    onDragEnd(dragState)
                    dragHandler.onDragEnd()
                },
                onDragCancel = {
                    onDragCancel(dragState)
                    dragHandler.onDragCancel()
                },
            )
        }
}