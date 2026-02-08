package com.github.xepozz.wippy.index

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import org.jetbrains.yaml.YAMLUtil
import org.jetbrains.yaml.psi.YAMLFile

object WippyConfigUtil {
    fun isWippyConfigFile(file: VirtualFile, project: Project): Boolean {
        val index = FileBasedIndex.getInstance()
        val keys = index.getAllKeys(WippyConfigIndex.NAME, project)
        if (WippyConfigIndex.CONFIG_KEY !in keys) return false
        
        val scope = GlobalSearchScope.fileScope(project, file)
        return index.getValues(WippyConfigIndex.NAME, WippyConfigIndex.CONFIG_KEY, scope).isNotEmpty()
    }

    fun isWippyConfigFile(psiFile: YAMLFile): Boolean {
        val version = YAMLUtil.getQualifiedKeyInFile(psiFile, "version")?.valueText
        val namespace = YAMLUtil.getQualifiedKeyInFile(psiFile, "namespace")?.valueText

        val re = version == "1.0" && namespace == "app"
        return re
    }

}
