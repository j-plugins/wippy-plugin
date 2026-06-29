package com.github.xepozz.wippy.lsp

import com.github.xepozz.wippy.index.WippyEntryIndex
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLSequence

/**
 * Maps between `.lua` files and the `wippy://namespace:entry_name` document URIs used by the Wippy LSP server.
 * The mapping is resolved from the sibling `_index.yaml` (file -> URI) and the entry index (URI -> file).
 */
object WippyLspUriMapper {

    const val SCHEME = "wippy://"

    fun fileToUri(project: Project, file: VirtualFile): String? = ReadAction.compute<String?, RuntimeException> {
        val dir = file.parent ?: return@compute null
        val indexFile = dir.findChild("_index.yaml") ?: return@compute null
        val root = (PsiManager.getInstance(project).findFile(indexFile) as? YAMLFile)
            ?.documents?.firstOrNull()?.topLevelValue as? YAMLMapping ?: return@compute null
        val namespace = root.getKeyValueByKey("namespace")?.valueText ?: return@compute null
        val entries = root.getKeyValueByKey("entries")?.value as? YAMLSequence ?: return@compute null

        for (item in entries.items) {
            val mapping = item.value as? YAMLMapping ?: continue
            val name = mapping.getKeyValueByKey("name")?.valueText ?: continue
            val source = mapping.getKeyValueByKey("source")?.valueText ?: continue
            if (dir.findFileByRelativePath(source.removePrefix("file://")) == file) {
                return@compute "$SCHEME$namespace:$name"
            }
        }
        null
    }

    fun uriToFile(project: Project, uri: String): VirtualFile? {
        if (!uri.startsWith(SCHEME)) return null
        val id = uri.removePrefix(SCHEME)
        val separator = id.indexOf(':')
        if (separator < 0) return null
        val namespace = id.substring(0, separator)
        val name = id.substring(separator + 1)

        return ReadAction.compute<VirtualFile?, RuntimeException> {
            val index = FileBasedIndex.getInstance()
            val scope = GlobalSearchScope.projectScope(project)
            for (indexFile in index.getContainingFiles(WippyEntryIndex.NAME, namespace, scope)) {
                val entry = index.getFileData(WippyEntryIndex.NAME, indexFile, project)[namespace]
                    ?.firstOrNull { it.name == name } ?: continue
                val source = entry.sourceFile?.removePrefix("file://") ?: continue
                indexFile.parent?.findFileByRelativePath(source)?.let { return@compute it }
            }
            null
        }
    }
}
