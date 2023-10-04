package com.example.structurecounter.counterTree

import com.example.structurecounter.model.CounterEntryType

class CounterTreeLeaf(name: String) : CounterTreeNode(name, CounterEntryType.Function, 0, 1,false)
