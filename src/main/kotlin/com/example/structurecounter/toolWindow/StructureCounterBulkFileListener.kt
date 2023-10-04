package com.example.structurecounter.toolWindow

import com.example.structurecounter.model.ToolWindowViewModel
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.*

class StructureCounterBulkFileListener(
    private val twInterface: ToolWindowViewModel,
    private val project: Project
) : BulkFileListener {
    override fun after(events: MutableList<out VFileEvent>) {
        super.after(events)
        for (event in events) {
            when (event) {
                is VFileCreateEvent -> fileCreated(event)
                is VFileCopyEvent -> fileCopied(event)
                is VFileDeleteEvent -> fileDeleted(event)
                is VFileMoveEvent -> fileMoved(event)
                is VFileContentChangeEvent -> contentsChanged(event)
                is VFilePropertyChangeEvent -> propertyChanged(event)
            }
        }
    }

    private fun fileCreated(event: VFileCreateEvent) {
        val newContentEntry = ToolWindowUtil.readEntryFromVirtualFile(project, event.file ?: return) ?: return
        twInterface.createChildNode(
            event.parent.name,
            newContentEntry
        )
    }

    private fun fileCopied(event: VFileCopyEvent) {
        if (event.file.extension != StructureCounterConstants.supportedExtension) return
        val newContentEntry = ToolWindowUtil.readEntryFromVirtualFile(project, event.file) ?: return
        twInterface.createChildNode(event.newParent.name, newContentEntry)
    }

    private fun fileDeleted(event: VFileDeleteEvent) {
        twInterface.deleteChildNode(event.file.parent.name, event.file.name)
    }

    /*
        BUG: if directory is moved, its content may not be updated, as the following actions are performed
        1. New dir created
        2. Nested files are copied
        3. Old dir removed
        The current algorithm finds one of the dirs, which may be incorrect.
        Possible fix: change usage of first parent nodes to module parent nodes, as they must be unique?
    */
    private fun fileMoved(event: VFileMoveEvent) {
        twInterface.deleteChildNode(event.oldParent.name, event.file.name)

        val fileEntry = ToolWindowUtil.readEntryFromVirtualFile(project, event.file) ?: return
        twInterface.createChildNode(event.newParent.name, fileEntry)
    }

    private fun propertyChanged(event: VFilePropertyChangeEvent) {
        // only name changes are interesting for structure
        if (event.propertyName == VirtualFile.PROP_NAME) twInterface.renameNode(
            event.file.parent.name,
            event.oldValue.toString(),
            event.newValue.toString()
        )
    }

    private fun contentsChanged(event: VFileContentChangeEvent) {
        if (event.file.extension != StructureCounterConstants.supportedExtension) return
        val newContentEntry = ToolWindowUtil.readEntryFromVirtualFile(project, event.file) ?: return
        twInterface.updateExistingFileNodeByContents(event.file.parent.name, newContentEntry)
    }
}