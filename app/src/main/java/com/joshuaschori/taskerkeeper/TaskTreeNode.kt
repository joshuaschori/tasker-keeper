package com.joshuaschori.taskerkeeper

import com.joshuaschori.taskerkeeper.tasks.Task

class TaskTreeNode(val data: Task) {
    var children: MutableList<TaskTreeNode> = mutableListOf()

    // returns Task with its Subtasks attached in subtaskList
    fun preOrderTraversal(): Task {
        val taskWithSubtasks = data.copy(
            subtaskList = children.map {
                it.preOrderTraversal()
            }
        )
        return taskWithSubtasks
    }

}