package com.joshuaschori.taskerkeeper.tasks.tasksDetail

data class Task(
    val taskId: Int,
    val parentTaskId: Int?,
    val description: String,
    val listOrder: Int,
    val isChecked: Boolean,
    val isExpanded: Boolean,
    // null subtaskList represents no child tasks in database
    // empty subtaskList given in fragment if child tasks are present in database but not being shown
    val subtaskList: List<Task>? = null,
    val taskLayer: Int = 0,
)