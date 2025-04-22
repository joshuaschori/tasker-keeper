package com.joshuaschori.taskerkeeper.tasks.tasksDetail

import com.joshuaschori.taskerkeeper.DragMode
import com.joshuaschori.taskerkeeper.data.tasks.TaskEntity

class TaskListBuilder {

    fun prepareTaskList(taskEntityList: List<TaskEntity>, draggedTaskId: Int?, dragMode: DragMode?): List<Task> {
        val taskTreeNodeList = convertTaskEntityListToTaskTreeNodeList(
            taskEntityList = taskEntityList
        )
        val completeTaskList = convertTaskTreeNodeListToTaskList(taskTreeNodeList)
        val visibleTaskList = determineVisibleTasks(
            taskList = completeTaskList,
            draggedTaskId = draggedTaskId,
            dragMode = dragMode
        )
        return unpackTaskAndSubtasks(visibleTaskList)
    }

    class TaskTreeNode(val data: Task) {
        var children: MutableList<TaskTreeNode> = mutableListOf()

        // returns Task with its Subtasks attached in subtaskList
        fun preOrderTraversal(
            taskLayer: Int = 0,
            passHighestLayerBelow: (Int) -> Unit = {}
        ): Task {
            var highestLayerBelow: Int = taskLayer
            val taskWithSubtasks = data.copy(
                subtaskList = if (children.isNotEmpty()) {
                    children.map { childNode ->
                        childNode.preOrderTraversal(
                            taskLayer = taskLayer + 1,
                            passHighestLayerBelow = {
                                if (it > highestLayerBelow) {
                                    highestLayerBelow = it
                                    passHighestLayerBelow(it)
                                }
                            }
                        )
                    }
                } else {
                    passHighestLayerBelow(taskLayer)
                    null
                },
                highestTierBelow = highestLayerBelow,
                itemTier = taskLayer
            )
            return taskWithSubtasks
        }

    }

    class TaskTreeBuilder {
        private val nodesById = mutableMapOf<Int, TaskTreeNode>()
        private val orphans = mutableMapOf<Int, MutableList<TaskTreeNode>>()

        private fun attachOrphansRecursively(node: TaskTreeNode) {
            orphans[node.data.itemId]?.let { directOrphans ->
                directOrphans.forEach { orphan ->
                    node.children.add(orphan)
                    attachOrphansRecursively(orphan)
                }
                orphans.remove(node.data.itemId)
            }
        }

        fun addNode(node: TaskTreeNode): TaskTreeNode {
            nodesById[node.data.itemId] = node
            if (node.data.parentItemId == null) {
                attachOrphansRecursively(node)
                return node
            }
            nodesById[node.data.parentItemId]?.let { parent ->
                parent.children.add(node)
                attachOrphansRecursively(node)
            } ?: run {
                orphans.getOrPut(node.data.parentItemId) { mutableListOf() }.add(node)
            }
            return node
        }

        fun buildTree(): List<TaskTreeNode> {
            val roots = mutableListOf<TaskTreeNode>()
            nodesById.forEach { (key, value) ->
                if (value.data.parentItemId == null) {
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
                    itemId = taskEntity.taskId,
                    parentItemId = taskEntity.parentTaskId,
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

    private fun determineVisibleTasks (taskList: List<Task>, draggedTaskId: Int?, dragMode: DragMode?): List<Task> {
        val updatedTasks = mutableListOf<Task>()
        fun traverse(task: Task): Task {
            val taskWithSubtasks = task.copy(
                subtaskList = if (task.subtaskList != null
                    && (task.isExpanded || (task.itemId == draggedTaskId && dragMode == DragMode.CHANGE_TIER))
                    && !(task.itemId == draggedTaskId && dragMode == DragMode.REARRANGE)
                ) {
                    task.subtaskList.map { traverse(task = it) }
                } else { null },
                numberOfChildren = task.subtaskList?.size ?: 0
            )
            return taskWithSubtasks
        }
        taskList.forEach{ updatedTasks.add(traverse(it)) }
        return updatedTasks
    }

    // returns list of Tasks, with the root Task and its subtasks in order, prepped for lazyList
    private fun unpackTaskAndSubtasks(taskList: List<Task>): List<Task> {
        val unpackedTaskList = mutableListOf<Task>()
        var index: Int = 0
        fun traverse(task: Task) {
            unpackedTaskList.add(task.copy(subtaskList = null, lazyListIndex = index))
            index++
            task.subtaskList?.forEach {
                traverse(it)
            }
        }
        taskList.forEach{ traverse(it) }
        return unpackedTaskList
    }

}