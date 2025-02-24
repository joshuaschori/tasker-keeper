package com.joshuaschori.taskerkeeper

sealed interface TasksTabState {
    data class Detail(val parentCategoryId: Int): TasksTabState
    data object Menu: TasksTabState
}