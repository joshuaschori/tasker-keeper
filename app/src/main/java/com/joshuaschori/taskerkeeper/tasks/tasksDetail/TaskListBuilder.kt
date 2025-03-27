package com.joshuaschori.taskerkeeper.tasks.tasksDetail

import com.joshuaschori.taskerkeeper.data.tasks.TaskEntity

class TaskListBuilder {

    fun prepareTaskList(taskEntityList: List<TaskEntity>, draggedTaskId: Int?): List<Task> {
        val taskTreeNodeList = convertTaskEntityListToTaskTreeNodeList(
            taskEntityList = taskEntityList
        )
        val completeTaskList = convertTaskTreeNodeListToTaskList(taskTreeNodeList)
        val visibleTaskList = determineVisibleTasks(
            taskList = completeTaskList,
            lazyListTaskIdBeingDragged = draggedTaskId
        )
        return unpackTaskAndSubtasks(visibleTaskList)
    }

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

    class TaskTreeBuilder {
        private val nodesById = mutableMapOf<Int, TaskTreeNode>()
        private val orphans = mutableMapOf<Int, MutableList<TaskTreeNode>>()

        private fun attachOrphansRecursively(node: TaskTreeNode) {
            orphans[node.data.taskId]?.let { directOrphans ->
                directOrphans.forEach { orphan ->
                    node.children.add(orphan)
                    attachOrphansRecursively(orphan)
                }
                orphans.remove(node.data.taskId)
            }
        }

        fun addNode(node: TaskTreeNode): TaskTreeNode {
            nodesById[node.data.taskId] = node
            if (node.data.parentTaskId == null) {
                attachOrphansRecursively(node)
                return node
            }
            nodesById[node.data.parentTaskId]?.let { parent ->
                parent.children.add(node)
                attachOrphansRecursively(node)
            } ?: run {
                orphans.getOrPut(node.data.parentTaskId) { mutableListOf() }.add(node)
            }
            return node
        }

        fun buildTree(): List<TaskTreeNode> {
            val roots = mutableListOf<TaskTreeNode>()
            nodesById.forEach { (key, value) ->
                if (value.data.parentTaskId == null) {
                    roots.add(value)
                }
            }
            return roots
        }
    }

    private fun convertTaskEntityListToTaskTreeNodeList(taskEntityList: List<TaskEntity>): List<TaskTreeNode> {
        val treeBuilder = TaskTreeBuilder()
        taskEntityList.forEach { taskEntity ->
            val taskTreeNode = TaskTreeNode(
                Task(
                    taskId = taskEntity.taskId,
                    parentTaskId = taskEntity.parentTaskId,
                    description = taskEntity.description,
                    listOrder = taskEntity.listOrder,
                    isChecked = taskEntity.isChecked,
                    isExpanded = taskEntity.isExpanded,
                )
            )
            treeBuilder.addNode(taskTreeNode)
        }
        val tree = treeBuilder.buildTree()
        return tree
    }

    private fun convertTaskTreeNodeListToTaskList(taskTreeNodeList: List<TaskTreeNode>): List<Task> {
        val taskList: MutableList<Task> = mutableListOf()
        for (node in taskTreeNodeList) {
            taskList.add(node.preOrderTraversal())
        }
        return taskList
    }

    private fun determineVisibleTasks (taskList: List<Task>, lazyListTaskIdBeingDragged: Int?): List<Task> {
        val updatedTasks = mutableListOf<Task>()
        fun traverse(task: Task): Task {
            val taskWithSubtasks = task.copy(
                subtaskList = if (task.subtaskList != null && task.isExpanded && task.taskId != lazyListTaskIdBeingDragged) {
                    task.subtaskList.map { traverse(task = it) }
                } else if (task.subtaskList == null) {
                    null
                } else {
                    listOf()
                },
            )
            return taskWithSubtasks
        }
        taskList.forEach{ updatedTasks.add(traverse(it)) }
        return updatedTasks
    }

    // returns list of Tasks, with the root Task and its subtasks in order, prepped for lazyList
    private fun unpackTaskAndSubtasks(taskList: List<Task>): List<Task> {
        val unpackedTaskList = mutableListOf<Task>()
        fun traverse(task: Task) {
            unpackedTaskList.add(task)
            task.subtaskList?.forEach {
                traverse(it)
            }
        }
        taskList.forEach{ traverse(it) }
        return unpackedTaskList
    }

}