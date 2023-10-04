package com.example.structurecounter.counterTree

import com.example.structurecounter.model.CounterEntryType
import com.example.structurecounter.model.StructureCounterEntry

class CounterTreeBranch(
    name: String,
    classCount: Int,
    functionCount: Int,
    type: CounterEntryType
) : CounterTreeNode(name, type, classCount, functionCount, true) {
    fun updateClassCount(newCount: Int) {
        classCount = newCount
    }

    fun updateFunctionCount(newCount: Int) {
        functionCount = newCount
    }

    constructor(entry: StructureCounterEntry) : this(entry.name, entry.classCount, entry.functionCount, entry.type)
}
