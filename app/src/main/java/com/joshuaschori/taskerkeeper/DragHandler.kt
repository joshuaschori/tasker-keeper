package com.joshuaschori.taskerkeeper

data class DragHandler(
    val lazyListIndexBeingDragged: Int? = null,
    val dragTargetLazyListIndex: Int? = null,
) {
    companion object {
        val mapOfLazyListIndexToItemId = mapOf<Int, Int>()
    }
}