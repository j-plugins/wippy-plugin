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

    /**
     * Checks if the file is a Wippy definition file (e.g., src/_index.yaml).
     * These files contain 'version', 'namespace', and 'entries'.
     */
    fun isWippyDefinitionFile(psiFile: YAMLFile): Boolean {
        val version = YAMLUtil.getQualifiedKeyInFile(psiFile, "version")
        val namespace = YAMLUtil.getQualifiedKeyInFile(psiFile, "namespace")
        val entries = YAMLUtil.getQualifiedKeyInFile(psiFile, "entries")

        return version != null && namespace != null && entries != null
    }

    /**
     * Checks if the file is a Wippy LSP configuration file (e.g., .wippy.yaml).
     * These files are either named '.wippy.yaml' or contain 'lua' and 'lsp' keys.
     */
    fun isWippyLspConfigFile(psiFile: YAMLFile): Boolean {
        if (psiFile.name == ".wippy.yaml") return true

        val lua = YAMLUtil.getQualifiedKeyInFile(psiFile, "lua")
        val lsp = YAMLUtil.getQualifiedKeyInFile(psiFile, "lsp")

        return lua != null && lsp != null
    }

    fun isWippyConfigFile(psiFile: YAMLFile): Boolean {
        return isWippyDefinitionFile(psiFile) || isWippyLspConfigFile(psiFile)
    }

}
