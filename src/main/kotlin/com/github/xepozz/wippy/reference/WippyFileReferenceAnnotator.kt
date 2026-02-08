package com.github.xepozz.wippy.reference

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar

class WippyFileReferenceAnnotator : Annotator {

    companion object {
        private const val FILE_PROTOCOL = "file://"
        private const val INDEX_FILENAME = "_index.yaml"
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val yamlScalar = element as? YAMLScalar ?: return

        if (element.containingFile?.name != INDEX_FILENAME) return

        val keyValue = PsiTreeUtil.getParentOfType(yamlScalar, YAMLKeyValue::class.java)
        if (keyValue?.keyText != "source") return

        val text = yamlScalar.textValue
        if (!text.startsWith(FILE_PROTOCOL)) return

        val filePath = text.removePrefix(FILE_PROTOCOL)
        if (filePath.isBlank()) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Empty file path")
                .range(yamlScalar)
                .create()
            return
        }

        val containingDir = element.containingFile?.originalFile?.containingDirectory ?: return
        val targetFile = containingDir.virtualFile.findFileByRelativePath(filePath)

        if (targetFile == null || targetFile.isDirectory) {
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                "Cannot resolve file: $filePath"
            )
                .range(yamlScalar)
                .withFix(CreateWippyLuaFileFix(filePath, containingDir))
                .create()
        }
    }
}