package com.example.structurecounter.toolWindow

import com.example.structurecounter.model.CounterEntryType
import com.example.structurecounter.model.StructureCounterEntry
import com.example.structurecounter.model.ToolWindowViewModel
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.*
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel


class StructureCounterToolWindowFactory : ToolWindowFactory {
    private val panel = JPanel(BorderLayout())
    private val refreshButton = JButton("Refresh")

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        DumbService.getInstance(project).runWhenSmart {
            refreshButton.addActionListener {
                gatherToolWindowContent(project, toolWindow)
            }
            gatherToolWindowContent(project, toolWindow)
        }
    }

    private fun gatherToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val moduleListInProject = ArrayList<StructureCounterEntry>()
        val moduleManager = ModuleManager.getInstance(project)
        val modules = moduleManager.modules
        for (module in modules) {
            val roots = ModuleRootManager.getInstance(module).sourceRoots

            val moduleEntry = ToolWindowUtil.getEntryFromFilesAndDirs(module.name, roots, project)

            moduleListInProject.add(
                StructureCounterEntry(
                    CounterEntryType.Module,
                    moduleEntry.name,
                    moduleEntry.classCount,
                    moduleEntry.functionCount,
                    moduleEntry.nested
                )
            )
        }

        val (projectClassCount, projectFunctionCount) = ToolWindowUtil.accumulateChildrenCounters(
            moduleListInProject
        )

        val twInterface = ToolWindowViewModel(
            StructureCounterEntry(
                CounterEntryType.Module,
                project.name,
                projectClassCount,
                projectFunctionCount,
                moduleListInProject
            )
        )

        panel.removeAll()
        toolWindow.contentManager.removeAllContents(true)

        panel.add(refreshButton, BorderLayout.NORTH)
        panel.add(twInterface.projectPane, BorderLayout.CENTER)
        toolWindow.contentManager.addContent(toolWindow.contentManager.factory.createContent(panel, null, false))

        project.messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, StructureCounterBulkFileListener(twInterface, project))
    }
}
