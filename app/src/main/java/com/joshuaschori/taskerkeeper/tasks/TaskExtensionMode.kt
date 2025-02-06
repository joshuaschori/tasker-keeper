package com.joshuaschori.taskerkeeper.tasks

enum class TaskExtensionMode(val contentDescription: String) {
    NORMAL("Normal Mode"),
    ADD_NEW_TASK("Add Task Mode"),
    ADD_NEW_SUBTASK("Add Subtask Mode"),
    REARRANGE("Rearrange Mode"),
    DELETE("Delete Mode"),
}