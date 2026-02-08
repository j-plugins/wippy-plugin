package com.github.xepozz.wippy.reference

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar

class WippyFileReferenceProvider : PsiReferenceProvider() {

    companion object {
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
        if (!text.startsWith(WippyFileReferenceSet.FILE_PROTOCOL)) return PsiReference.EMPTY_ARRAY

        return WippyFileReferenceSet(yamlScalar).allReferences.map { it as PsiReference }.toTypedArray()
    }
}
