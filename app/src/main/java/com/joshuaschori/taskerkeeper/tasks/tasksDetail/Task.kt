package com.joshuaschori.taskerkeeper.tasks.tasksDetail

data class Task(
    val taskId: Int,
    val description: String,
    val isChecked: Boolean,
    val isExpanded: Boolean,
    val parentTaskId: Int?,
    val subtaskList: List<Task>,
)