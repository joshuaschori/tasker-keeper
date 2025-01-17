package com.example.taskerkeeper

class TaskTreeBuilder {
    private val nodesById = mutableMapOf<Int, TaskTreeNode>()
    private val orphans = mutableMapOf<Int, MutableList<TaskTreeNode>>()

    private fun attachOrphansRecursively(node: TaskTreeNode) {
        // Get direct orphans for this node
        orphans[node.data.taskId]?.let { directOrphans ->
            // For each direct orphan
            directOrphans.forEach { orphan ->
                // Add it as a child
                node.children.add(orphan)
                // Recursively attach any orphans waiting for this orphan
                attachOrphansRecursively(orphan)
            }
            // Clear the orphans list for this ID
            orphans.remove(node.data.taskId)
        }
    }

    fun addNode(node: TaskTreeNode): TaskTreeNode {
        // Create new node
        nodesById[node.data.taskId] = node

        // If this is a root node, attach its orphan tree and return
        if (node.data.parentId == null) {
            attachOrphansRecursively(node)
            return node
        }

        // If parent exists, add this node as its child and attach its orphan tree
        nodesById[node.data.parentId]?.let { parent ->
            parent.children.add(node)
            attachOrphansRecursively(node)
        } ?: run {
            // Parent doesn't exist yet, add to orphans
            orphans.getOrPut(node.data.parentId) { mutableListOf() }.add(node)
        }

        return node
    }

    fun buildTree(): List<TaskTreeNode> {
        val roots = mutableListOf<TaskTreeNode>()

        nodesById.forEach { (key, value) ->
            if (value.data.parentId == null) {
                roots.add(value)
            }
        }

        return roots
    }
}