package com.github.xepozz.wippy.reference

import com.github.xepozz.wippy.util.isInWippyDefinitionFile
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar

class WippyMethodReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(YAMLScalar::class.java)
                .withParent(YAMLKeyValue::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    val yamlScalar = element as? YAMLScalar ?: return PsiReference.EMPTY_ARRAY

                    if (!yamlScalar.isInWippyDefinitionFile()) return PsiReference.EMPTY_ARRAY

                    val keyValue = yamlScalar.parent as? YAMLKeyValue ?: return PsiReference.EMPTY_ARRAY
                    if (keyValue.keyText != "method") return PsiReference.EMPTY_ARRAY

                    val methodName = yamlScalar.textValue
                    if (methodName.isBlank()) return PsiReference.EMPTY_ARRAY

                    val sourceFile = findSiblingSource(keyValue) ?: return PsiReference.EMPTY_ARRAY

                    val rawText = yamlScalar.text
                    val nameStart = rawText.indexOf(methodName)
                    if (nameStart < 0) return PsiReference.EMPTY_ARRAY
                    val range = TextRange(nameStart, nameStart + methodName.length)

                    return arrayOf(WippyMethodReference(yamlScalar, methodName, sourceFile, range))
                }

                private fun findSiblingSource(methodKey: YAMLKeyValue): PsiFile? {
                    val mapping = methodKey.parent as? YAMLMapping ?: return null

                    val sourceValue = mapping.getKeyValueByKey("source")
                        ?.value?.let { it as? YAMLScalar }?.textValue
                        ?: return null

                    if (!sourceValue.startsWith(WippyFileReferenceSet.FILE_PROTOCOL)) return null

                    val filePath = sourceValue.removePrefix(WippyFileReferenceSet.FILE_PROTOCOL)
                    val containingDir = methodKey.containingFile?.originalFile?.containingDirectory
                        ?: return null
                    val vf = containingDir.virtualFile.findFileByRelativePath(filePath)
                        ?: return null

                    return PsiManager.getInstance(methodKey.project).findFile(vf)
                }
            }
        )
    }
}


