package com.example.structurecounter.counterTree

import com.example.structurecounter.model.CounterEntryType
import javax.swing.tree.DefaultMutableTreeNode

abstract class CounterTreeNode (
    name: String,
    val type: CounterEntryType,
    var classCount: Int,
    var functionCount: Int,
    children: Boolean
) : DefaultMutableTreeNode(name, children) {
    fun updateName(newName: String?) {
        userObject = newName
    }
}
