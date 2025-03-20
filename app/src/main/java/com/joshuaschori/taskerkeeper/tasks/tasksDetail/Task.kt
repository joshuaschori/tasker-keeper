package com.joshuaschori.taskerkeeper.tasks.tasksDetail

data class Task(
    val taskId: Int,
    val parentTaskId: Int?,
    val description: String,
    val listOrder: Int,
    val isChecked: Boolean,
    val isExpanded: Boolean,
    val subtaskList: List<Task>,
)