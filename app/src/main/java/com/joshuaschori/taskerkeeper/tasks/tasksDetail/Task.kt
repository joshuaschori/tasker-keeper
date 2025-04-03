package com.joshuaschori.taskerkeeper.tasks.tasksDetail

data class Task(
    val taskId: Int,
    val parentTaskId: Int?,
    val description: String,
    val listOrder: Int,
    val isChecked: Boolean,
    val isExpanded: Boolean,
    // The subtaskList is populated in TaskListBuilder to build proper parent / child relationships,
    // and then set to null as the tree is unpacked for Lazy Column.
    // In the UI, a Task with a non-zero numberOfChildren will have the option to expand its subtaskList,
    // which will then populate those tasks in the TaskListBuilder.
    val subtaskList: List<Task>? = null,
    val numberOfChildren: Int? = null,
    val taskLayer: Int = 0,
    val lazyListIndex: Int = 0,
)