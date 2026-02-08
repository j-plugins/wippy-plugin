package com.github.xepozz.wippy.reference

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar

/**
 * Resolves `method: call` → the Lua function `call` in the file from `source: file://...`
 * within the same entry block in _index.yaml.
 */
class WippyMethodReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(YAMLScalar::class.java)
                .withParent(YAMLKeyValue::class.java)
            ,
            object : PsiReferenceProvider() {
                private val INDEX_FILENAME = "_index.yaml"
                private val FILE_PROTOCOL = "file://"

                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    val yamlScalar = element as? YAMLScalar ?: return PsiReference.EMPTY_ARRAY
                    val keyValue = yamlScalar.parent as? YAMLKeyValue ?: return PsiReference.EMPTY_ARRAY
                    if (keyValue.keyText != "method") return PsiReference.EMPTY_ARRAY

                    val methodName = yamlScalar.textValue
                    if (methodName.isBlank()) return PsiReference.EMPTY_ARRAY

                    // Find sibling `source:` in the same mapping
                    val sourceFile = findSiblingSource(keyValue) ?: return PsiReference.EMPTY_ARRAY

                    // TextRange for the method name value
                    val rawText = yamlScalar.text
                    val nameStart = rawText.indexOf(methodName)
                    if (nameStart < 0) return PsiReference.EMPTY_ARRAY
                    val range = TextRange(nameStart, nameStart + methodName.length)

                    return arrayOf(WippyMethodReference(yamlScalar, methodName, sourceFile, range))
                }

                /**
                 * Walks up to the entry's YAML mapping and finds `source: file://...`,
                 * then resolves it to a PsiFile.
                 */
                private fun findSiblingSource(methodKey: YAMLKeyValue): PsiFile? {
                    val mapping = methodKey.parent as? YAMLMapping ?: return null

                    val sourceValue = mapping.getKeyValueByKey("source")
                        ?.value?.let { it as? YAMLScalar }?.textValue
                        ?: return null

                    if (!sourceValue.startsWith(FILE_PROTOCOL)) return null

                    val filePath = sourceValue.removePrefix(FILE_PROTOCOL)
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


