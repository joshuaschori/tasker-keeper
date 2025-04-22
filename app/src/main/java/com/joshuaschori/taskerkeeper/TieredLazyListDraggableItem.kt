package com.joshuaschori.taskerkeeper

interface TieredLazyListDraggableItem {
    val itemId: Int
    val parentItemId: Int?
    val numberOfChildren: Int?
    val highestTierBelow: Int
    val itemTier: Int
    val lazyListIndex: Int
}