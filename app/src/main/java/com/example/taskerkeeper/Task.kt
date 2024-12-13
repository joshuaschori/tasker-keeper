package com.example.taskerkeeper

data class Task(
    val taskString: String = "",
    val subtaskList: List<Subtask> = emptyList(),
    val isChecked: Boolean = false,
    val isExpanded: Boolean = false,
) {

}