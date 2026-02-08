package com.github.xepozz.wippy.reference

import com.github.xepozz.wippy.util.isInWippyDefinitionFile
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar

class WippyFileReferenceAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val yamlScalar = element as? YAMLScalar ?: return

        if (!yamlScalar.isInWippyDefinitionFile()) return

        val keyValue = PsiTreeUtil.getParentOfType(yamlScalar, YAMLKeyValue::class.java)
        if (keyValue?.keyText != "source") return

        val text = yamlScalar.textValue
        if (!text.startsWith(WippyFileReferenceSet.FILE_PROTOCOL)) return

        val filePath = text.removePrefix(WippyFileReferenceSet.FILE_PROTOCOL)
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