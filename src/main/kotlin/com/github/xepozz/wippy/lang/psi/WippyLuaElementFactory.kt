package com.github.xepozz.wippy.lang.psi

import com.github.xepozz.wippy.WippyFileType
import com.github.xepozz.wippy.lang.WippyLuaFile
import com.intellij.openapi.project.Project

object WippyLuaElementFactory {
    fun createFile(project: Project, text: String): WippyLuaFile {
        val name = "PHPOp"
        return com.intellij.psi.PsiFileFactory.getInstance(project)
            .createFileFromText(name, WippyFileType.INSTANCE, text) as WippyLuaFile
    }
}