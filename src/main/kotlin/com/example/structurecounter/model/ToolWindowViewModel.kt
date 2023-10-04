package com.example.structurecounter.model

import com.example.structurecounter.counterTree.CounterTreeBranch
import com.example.structurecounter.counterTree.CounterTreeLeaf
import com.example.structurecounter.counterTree.CounterTreeNode
import com.example.structurecounter.counterTree.CounterTreeRenderer

import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import javax.swing.JScrollPane
import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath

class ToolWindowViewModel internal constructor(baseEntry: StructureCounterEntry) {
    private val model: DefaultTreeModel
    val projectPane: JScrollPane

    init {
        val root = CounterTreeBranch(
            baseEntry.name, baseEntry.classCount,
            baseEntry.functionCount, CounterEntryType.Module
        )
        model = DefaultTreeModel(root)
        val projectTree = Tree(model)
        projectTree.cellRenderer = CounterTreeRenderer()
        projectPane = JBScrollPane(projectTree)
        createChildNodes(root, baseEntry)
    }

    /**
     * Locates given parent node and creates new child node based on [nodeEntry]. Does nothing if no node with [parentName] is found.
     * Changed amounts of classes and functions are propagated upwards.
     *
     * @param parentName exact name of parent node
     * @param nodeEntry new entry to create child node
     */
    fun createChildNode(parentName: String, nodeEntry: StructureCounterEntry) {
        val pathToParent = locateNode(TreePath(model.root), parentName)?.lastPathComponent ?: return
        val parentNode = pathToParent as CounterTreeBranch
        createChildNode(parentNode, nodeEntry)

        // intended: siftDeltasUp initially had wrong sign, the delta value is subtracted from the current
        siftDeltasUp(parentNode, nodeEntry.classCount, nodeEntry.functionCount)
    }

    /**
     * Locates node that is given by [modifiedEntry]'s name property at [parentName] branch node, deletes it and creates new node based on [modifiedEntry].
     * If nodes are not found, does nothing. Changed amounts of classes and functions are propagated upwards.
     *
     * @param parentName exact name of parent node
     * @param modifiedEntry new entry to create child node. Name will be used to delete updated node
     */
    fun updateExistingFileNodeByContents(parentName: String, modifiedEntry: StructureCounterEntry) {
        val pathToParent = locateNode(TreePath(model.root), parentName) ?: return
        val pathToNode = locateNode(pathToParent, modifiedEntry.name) ?: return
        val node = pathToNode.lastPathComponent as CounterTreeNode

        val classDelta = node.classCount - modifiedEntry.classCount
        val functionDelta = node.functionCount - modifiedEntry.functionCount

        val parentNode = node.parent
        createChildNode(parentNode as CounterTreeBranch, modifiedEntry)
        siftDeltasUp(parentNode, -classDelta, -functionDelta) // TODO check
        deleteChildNode(node)
    }

    /**
     * Locates node that is given by [previousName] at [parentName] branch node. Assigns [newName] to its label.
     *
     * @param parentName exact name of parent node
     * @param previousName exact name of existing node
     * @param newName name to be set to node
     */
    fun renameNode(parentName: String, previousName: String, newName: String) {
        val parentNode = locateNode(TreePath(model.root), parentName) ?: return
        val node = locateNode(TreePath(parentNode), previousName)?.lastPathComponent ?: return
        (node as CounterTreeNode).updateName(newName)
        model.nodeChanged(node)
    }

    /**
     * Locates [nodeName] node at [parentName] branch node and deletes it.
     *
     * @param parentName exact name of parent node
     * @param nodeName exact name of child node
     */
    fun deleteChildNode(parentName: String, nodeName: String) {
        val parentNode = locateNode(TreePath(model.root), parentName) ?: return
        val node = locateNode(parentNode, nodeName)?.lastPathComponent ?: return
        siftDeltasUp((node as CounterTreeNode).parent, -node.classCount, -node.functionCount)
        deleteChildNode(node)
    }

    /**
     * Creates new node from [nodeEntry] and places it as child to [parentNode]. Nested nodes are inserted recursively.
     */
    private fun createChildNode(parentNode: CounterTreeBranch, nodeEntry: StructureCounterEntry) {
        if (nodeEntry.type == CounterEntryType.Function) {
            model.insertNodeInto(CounterTreeLeaf(nodeEntry.name), parentNode, parentNode.childCount)
        } else {
            model.insertNodeInto(CounterTreeBranch(nodeEntry), parentNode, parentNode.childCount)
            val currentNode = parentNode.getChildAt(parentNode.childCount - 1) as CounterTreeBranch
            for (nested in nodeEntry.nested) {
                createChildNode(currentNode, nested)
            }
        }
    }

    /**
     * Same as [createChildNode], but instead of creating a new node and its children inserts children directly
     */
    private fun createChildNodes(
        parentNode: CounterTreeBranch,
        parentEntry: StructureCounterEntry
    ) {
        for (entry in parentEntry.nested) {
            createChildNode(parentNode, entry)
        }
    }

    /**
     * Propagates changes from [fromNode] to root.
     */
    private fun siftDeltasUp(fromNode: TreeNode?, classDelta: Int, functionDelta: Int) {
        var mutNode = fromNode
        if (classDelta != 0 || functionDelta != 0) {
            while (mutNode != null) {
                updateBranchByDeltas(mutNode as CounterTreeBranch, classDelta, functionDelta)
                mutNode = mutNode.parent
            }
        }
    }

    private fun deleteChildNode(node: CounterTreeNode) {
        model.removeNodeFromParent(node)
    }

    private fun updateBranchByDeltas(
        branch: CounterTreeBranch,
        classCounter: Int,
        functionCounter: Int
    ) {
        branch.updateFunctionCount(branch.functionCount + functionCounter)
        branch.updateClassCount(branch.classCount + classCounter)
        model.nodeChanged(branch)
    }

    /**
     * DFS algorithm to locate node by its name.
     * As many nodes with the same name can exist, all calls should start from locating parent node (which is usually module file)
     * Would be more time-effective if child nodes could be accessed by their user content as in map
     */
    private fun locateNode(path: TreePath, name: String): TreePath? {
        val node = path.lastPathComponent as CounterTreeNode
        if (node.userObject == name) {
            return path
        }
        for (i in 0 until node.childCount) {
            val childPath = path.pathByAddingChild(node.getChildAt(i))
            val foundNode = locateNode(childPath, name)
            if (foundNode != null) {
                return foundNode
            }
        }
        return null
    }
}
