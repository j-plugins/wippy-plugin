package com.github.xepozz.wippy.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar

class WippyFileReferenceProvider : PsiReferenceProvider() {

    companion object {
        private const val FILE_PROTOCOL = "file://"
        private const val INDEX_FILENAME = "_index.yaml"
    }

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext
    ): Array<PsiReference> {
        val yamlScalar = element as? YAMLScalar ?: return PsiReference.EMPTY_ARRAY

        val fileName = element.containingFile?.name ?: return PsiReference.EMPTY_ARRAY
        if (fileName != INDEX_FILENAME) return PsiReference.EMPTY_ARRAY

        val keyValue = PsiTreeUtil.getParentOfType(yamlScalar, YAMLKeyValue::class.java)
        if (keyValue?.keyText != "source") return PsiReference.EMPTY_ARRAY

        val text = yamlScalar.textValue
        if (!text.startsWith(FILE_PROTOCOL)) return PsiReference.EMPTY_ARRAY

        val filePath = text.removePrefix(FILE_PROTOCOL)
        if (filePath.isBlank()) return PsiReference.EMPTY_ARRAY

        val rawText = yamlScalar.text
        val protocolStart = rawText.indexOf(FILE_PROTOCOL)
        if (protocolStart < 0) return PsiReference.EMPTY_ARRAY

        val pathStart = protocolStart + FILE_PROTOCOL.length
        val pathEnd = pathStart + filePath.length
        val range = TextRange(pathStart, pathEnd)

        return arrayOf(WippyFileReference(yamlScalar, filePath, range))
    }
}
