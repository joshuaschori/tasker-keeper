package com.example.taskerkeeper

import com.example.taskerkeeper.tasks.Task

class TaskTreeNode(val data: Task) {
    var children: MutableList<TaskTreeNode> = mutableListOf()

    fun addChild(node: TaskTreeNode) {
        children.add(node)
    }

    fun search(value: Task): TaskTreeNode? {
        if (data == value) {
            return this
        }
        for (child in children) {
            val foundNode = child.search(value)
            if (foundNode != null) {
                return foundNode
            }
        }
        return null
    }

    fun delete(value: Task): Boolean {
        for (child in children) {
            if (child.data == value) {
                children.remove(child)
                return true
            }
            if (child.delete(value)) {
                return true
            }
        }
        return false
    }

    fun preOrderTraversal(): List<Task> {
        val listOfTasks = mutableListOf<Task>()
        listOfTasks.add(data.copy(
            subtaskList = children.map{it.data}
        ))

        for (child in children) {
            listOfTasks.addAll(child.preOrderTraversal())
        }

        return listOfTasks
    }

    fun postOrderTraversal() {
        // TODO

        for (child in children) {
            child.postOrderTraversal()
        }
    }
}