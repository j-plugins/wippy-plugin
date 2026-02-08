package com.github.xepozz.wippy.reference

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar

/**
 * Registers PsiReference for `source: file://...` values in Wippy _index.yaml files.
 * Enables Cmd/Ctrl+Click navigation from YAML to the referenced Lua file.
 */
class WippyFileReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(YAMLScalar::class.java),
            WippyFileReferenceProvider()
        )
    }
}

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

        // Only process _index.yaml files
        val fileName = element.containingFile?.name ?: return PsiReference.EMPTY_ARRAY
        if (fileName != INDEX_FILENAME) return PsiReference.EMPTY_ARRAY

        // Only process `source:` keys
        val keyValue = PsiTreeUtil.getParentOfType(yamlScalar, YAMLKeyValue::class.java)
        if (keyValue?.keyText != "source") return PsiReference.EMPTY_ARRAY

        val text = yamlScalar.textValue
        if (!text.startsWith(FILE_PROTOCOL)) return PsiReference.EMPTY_ARRAY

        val filePath = text.removePrefix(FILE_PROTOCOL)
        if (filePath.isBlank()) return PsiReference.EMPTY_ARRAY

        // TextRange within the scalar value (offset for quotes if present)
        val rawText = yamlScalar.text
        val protocolStart = rawText.indexOf(FILE_PROTOCOL)
        if (protocolStart < 0) return PsiReference.EMPTY_ARRAY

        val pathStart = protocolStart + FILE_PROTOCOL.length
        val pathEnd = pathStart + filePath.length
        val range = TextRange(pathStart, pathEnd)

        return arrayOf(WippyFileReference(yamlScalar, filePath, range))
    }
}

/**
 * Resolves a `file://` path relative to the containing YAML file's directory.
 * Provides navigation, rename refactoring, and "unresolved reference" inspection.
 */
class WippyFileReference(
    element: PsiElement,
    private val filePath: String,
    range: TextRange
) : PsiReferenceBase<PsiElement>(element, range, /* soft = */ false) {

    override fun resolve(): PsiElement? {
        val containingDir = element.containingFile?.originalFile?.containingDirectory
            ?: return null

        // Resolve relative path (supports nested: file://sub/module.lua)
        val targetVirtualFile = containingDir.virtualFile.findFileByRelativePath(filePath)
            ?: return null

        return PsiManager.getInstance(element.project).findFile(targetVirtualFile)
    }

    /**
     * Provides completion variants — lists .lua files in the same directory.
     */
    override fun getVariants(): Array<Any> {
        val containingDir = element.containingFile?.originalFile?.containingDirectory
            ?: return emptyArray()

        return containingDir.virtualFile.children
            .filter { it.extension == "lua" && !it.isDirectory }
            .map { "file://${it.name}" }
            .toTypedArray()
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        // When the Lua file is renamed, update the `source: file://...` value
        val newValue = "file://$newElementName"
        val yamlScalar = element as? YAMLScalar ?: return element
        return ElementManipulators.handleContentChange(yamlScalar, newValue)
    }
}