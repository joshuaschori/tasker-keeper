package com.joshuaschori.taskerkeeper.tasks.tasksDetail

class TaskTreeNode(val data: Task) {
    var children: MutableList<TaskTreeNode> = mutableListOf()

    // returns Task with its Subtasks attached in subtaskList
    fun preOrderTraversal(taskLayer: Int = 0): Task {
        val taskWithSubtasks = data.copy(
            subtaskList = if (children.isNotEmpty()) {
                children.map {
                    it.preOrderTraversal(taskLayer + 1)
                }
            } else {
                null
            },
            taskLayer = taskLayer
        )
        return taskWithSubtasks
    }

}