package com.github.xepozz.wippy.util

import com.github.xepozz.wippy.index.WippyConfigUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.yaml.psi.YAMLFile

fun VirtualFile.isWippyConfig(project: Project): Boolean =
    WippyConfigUtil.isWippyConfigFile(this, project)

fun PsiFile.isWippyDefinition(): Boolean = when(this) {
    is YAMLFile -> WippyConfigUtil.isWippyDefinitionFile(this)
    else -> false
}

fun PsiFile.isWippyLspConfig(): Boolean = when(this) {
    is YAMLFile -> WippyConfigUtil.isWippyLspConfigFile(this)
    else -> false
}

fun PsiFile.isWippyConfig(): Boolean = when(this) {
    is YAMLFile -> WippyConfigUtil.isWippyConfigFile(this)
    else -> originalFile.virtualFile?.isWippyConfig(project) ?: false
}

fun PsiElement.isInWippyDefinitionFile(): Boolean =
    containingFile?.isWippyDefinition() ?: false
