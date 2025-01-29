package com.joshuaschori.taskerkeeper.tasks

data class Task(
    val taskId: Int,
    val taskString: String,
    val isChecked: Boolean,
    val isExpanded: Boolean,
    val parentId: Int?,
    val subtaskList: List<Task>
) {

}