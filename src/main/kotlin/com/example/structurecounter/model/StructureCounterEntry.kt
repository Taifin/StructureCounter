package com.example.structurecounter.model

data class StructureCounterEntry(
    val type: CounterEntryType,
    val name: String,
    val classCount: Int,
    val functionCount: Int,
    val nested: List<StructureCounterEntry>
)
