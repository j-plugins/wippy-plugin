package com.github.xepozz.wippy.reference

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar

/**
 * Shows a gutter icon (→) next to `source: file://...` lines in _index.yaml.
 * Clicking the icon navigates to the referenced Lua file.
 */
class WippySourceLineMarkerProvider : LineMarkerProvider {

    companion object {
        private const val FILE_PROTOCOL = "file://"
        private const val INDEX_FILENAME = "_index.yaml"
    }

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        // Line markers must be registered on leaf elements to avoid duplicates
        val yamlScalar = element as? YAMLScalar ?: return null

        if (element.containingFile?.name != INDEX_FILENAME) return null

        val keyValue = PsiTreeUtil.getParentOfType(yamlScalar, YAMLKeyValue::class.java)
        if (keyValue?.keyText != "source") return null

        val text = yamlScalar.textValue
        if (!text.startsWith(FILE_PROTOCOL)) return null

        val filePath = text.removePrefix(FILE_PROTOCOL)
        val containingDir = element.containingFile?.originalFile?.containingDirectory ?: return null
        val targetFile = containingDir.virtualFile.findFileByRelativePath(filePath) ?: return null

        // Verify the file is resolvable
        PsiManager.getInstance(element.project).findFile(targetFile) ?: return null

        return LineMarkerInfo(
            element,
            element.textRange,
            AllIcons.Actions.Forward, // → arrow icon
            { "Navigate to ${targetFile.name}" },
            { _, elt ->
                val dir = elt.containingFile?.originalFile?.containingDirectory ?: return@LineMarkerInfo
                val vf = dir.virtualFile.findFileByRelativePath(filePath) ?: return@LineMarkerInfo
                val psiFile = PsiManager.getInstance(elt.project).findFile(vf) ?: return@LineMarkerInfo
                psiFile.navigate(true)
            },
            GutterIconRenderer.Alignment.LEFT,
            { "Navigate to source file" }
        )
    }
}