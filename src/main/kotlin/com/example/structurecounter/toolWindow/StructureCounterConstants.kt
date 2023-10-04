package com.example.structurecounter.toolWindow

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.fileTypes.FileType

object StructureCounterConstants {

    private val supportedFileType: FileType = JavaFileType.INSTANCE
    val supportedExtension = supportedFileType.defaultExtension

}