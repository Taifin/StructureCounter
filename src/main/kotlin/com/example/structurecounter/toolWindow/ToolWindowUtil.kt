package com.example.structurecounter.toolWindow

import com.example.structurecounter.model.CounterEntryType
import com.example.structurecounter.model.StructureCounterEntry
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.impl.VirtualDirectoryImpl
import com.intellij.openapi.vfs.newvfs.impl.VirtualFileImpl
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod

data class Count(var classCount: Int, var functionCount: Int)

object ToolWindowUtil {
    fun accumulateChildrenCounters(moduleListInProject: List<StructureCounterEntry>): Count =
        moduleListInProject.map { count ->
            Count(count.classCount, count.functionCount)
        }.reduce { (classAcc, functionAcc), (classCount, functionCount) ->
            Count(classAcc + classCount, functionAcc + functionCount)
        }

    private fun readFile(project: Project, file: VirtualFile): StructureCounterEntry? {
        if (!file.extension.equals(StructureCounterConstants.supportedExtension)) return null

        val psiFile = PsiManager.getInstance(project).findFile(file) ?: return null
        val classListInFile = ArrayList<StructureCounterEntry>()

        for (fileElement in psiFile.children) {
            if (fileElement is PsiClass) {
                val methodListInClass = ArrayList<StructureCounterEntry>()
                for (classElement in fileElement.getChildren()) {
                    if (classElement is PsiMethod) {
                        methodListInClass.add(
                            StructureCounterEntry(
                                CounterEntryType.Function,
                                classElement.name,
                                0,
                                0,
                                ArrayList()
                            )
                        )
                    }
                }
                classListInFile.add(
                    StructureCounterEntry(
                        CounterEntryType.Class,
                        fileElement.name!!,
                        0,
                        methodListInClass.size,
                        methodListInClass
                    )
                )
            }
        }
        val allMethodsInFileCount = classListInFile.map(
            StructureCounterEntry::functionCount
        ).reduce { a: Int, b: Int -> a + b }

        return StructureCounterEntry(
            CounterEntryType.SourceFile,
            file.name,
            classListInFile.size,
            allMethodsInFileCount,
            classListInFile
        )
    }

    /**
     * Run recursive traversal over all elements in the directory
     */
    private fun readDirectory(
        project: Project,
        directory: VirtualDirectoryImpl
    ) = getEntryFromFilesAndDirs(directory.name, directory.children, project)

    /**
     * Dispatch reading to correct method depending on the file type
     */
    fun readEntryFromVirtualFile(project: Project, file: VirtualFile): StructureCounterEntry? {
        return when (file) {
            is VirtualDirectoryImpl -> readDirectory(project, file)
            is VirtualFileImpl -> readFile(project, file)
            else -> null
        }
    }

    /**
     * Given an array of virtual files, traverse them recursively and gather information on functions and classes
     */
    fun getEntryFromFilesAndDirs(
        name: String,
        files: Array<VirtualFile>,
        project: Project
    ): StructureCounterEntry {
        val childEntries = ArrayList<StructureCounterEntry>()
        for (file in files) {
            childEntries.add(
                readEntryFromVirtualFile(project, file) ?: continue
            )
        }

        val (classCount, functionsCount) = accumulateChildrenCounters(childEntries)
        return StructureCounterEntry(
            CounterEntryType.Package,
            name,
            classCount,
            functionsCount,
            childEntries
        )
    }
}
