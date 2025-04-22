package com.joshuaschori.taskerkeeper

import com.joshuaschori.taskerkeeper.tasks.tasksDetail.Task

data class DragHandler(
    val draggedTask: Task? = null,
    val draggedTaskSize: Int? = null,
    val dragLeftPossible: Boolean = false,
    val dragMaxExceeded: Boolean = false,
    val dragMode: DragMode? = null,
    val dragRequestedLayerChange: Int? = null,
    val dragRightPossible: Boolean = false,
    val dragTargetIndex: Int? = null,
    val dragTaskAbove: Task? = null,
    val dragTaskBelow: Task? = null,
    val dragYDirection: YDirection? = null,
)