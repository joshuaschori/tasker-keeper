package com.joshuaschori.taskerkeeper.tasks.tasksDetail

import com.joshuaschori.taskerkeeper.TieredLazyListDraggableItem

data class Task(
    override val itemId: Int,
    override val parentItemId: Int?,
    val description: String,
    val listOrder: Int,
    val isChecked: Boolean,
    val isExpanded: Boolean,
    // The subtaskList is populated in TaskListBuilder to build proper parent / child relationships,
    // and then set to null as the tree is unpacked for Lazy Column.
    // In the UI, a Task with a non-zero numberOfChildren will have the option to expand its subtaskList,
    // which will then populate those tasks in the TaskListBuilder.
    val subtaskList: List<Task>? = null,
    override val numberOfChildren: Int? = null,
    override val highestTierBelow: Int = 0,
    override val itemTier: Int = 0,
    override val lazyListIndex: Int = 0,
): TieredLazyListDraggableItem