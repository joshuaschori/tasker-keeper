package com.joshuaschori.taskerkeeper.tasks.tasksDetail

data class Task(
    val taskId: Int,
    val parentTaskId: Int?,
    val description: String,
    val listOrder: Int,
    val isChecked: Boolean,
    val isExpanded: Boolean,
    // The subtaskList is populated in TaskListBuilder to build proper parent / child relationships,
    // and then is set to an empty list as the tree is unpacked for Lazy Column. It will be set to
    // an empty list earlier in the TaskListBuilder if subtasks shouldn't be represented in UI.
    // A null subtaskList represents no child tasks in database.
    // In the UI, a Task with an empty subtaskList will have the option to expand its subtaskList,
    // which will then populate.
    val subtaskList: List<Task>? = null,
    val taskLayer: Int = 0,
    val lazyListIndex: Int = 0,
)