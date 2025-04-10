package com.joshuaschori.taskerkeeper

import androidx.compose.ui.unit.dp

object Constants {
    // including layer 0, which are the null parent root tasks
    const val MAX_LAYER_FOR_SUBTASKS = 5

    // higher number is less sensitive to deciding drag mode
    const val DRAG_MODE_SENSITIVITY = 5

    const val LAZY_COLUMN_START_PADDING = 16
    const val LAZY_COLUMN_TOP_PADDING = 32
    const val LAZY_COLUMN_END_PADDING = 16
    const val LAZY_COLUMN_BOTTOM_PADDING = 320
    const val LAZY_COLUMN_VERTICAL_ARRANGEMENT_SPACING = 1
    const val FLOATING_ACTION_BUTTON_PADDING = 16
    const val TASK_ROW_ICON_TOP_PADDING = 12

}