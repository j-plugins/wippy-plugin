package com.github.xepozz.wippy.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
import org.jetbrains.yaml.psi.YAMLScalar

class WippyFileReference(
    element: PsiElement,
    private val filePath: String,
    range: TextRange
) : PsiReferenceBase<PsiElement>(element, range, /* soft = */ false) {

    override fun resolve(): PsiElement? {
        val containingDir = element.containingFile?.originalFile?.containingDirectory
            ?: return null

        val targetVirtualFile = containingDir.virtualFile.findFileByRelativePath(filePath)
            ?: return null

        return PsiManager.getInstance(element.project).findFile(targetVirtualFile)
    }

    override fun getVariants(): Array<Any> {
        val containingDir = element.containingFile?.originalFile?.containingDirectory
            ?: return emptyArray()

        return containingDir.virtualFile.children
            .filter { it.extension == "lua" && !it.isDirectory }
            .map { "file://${it.name}" }
            .toTypedArray()
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        val newValue = "file://$newElementName"
        val yamlScalar = element as? YAMLScalar ?: return element
        return ElementManipulators.handleContentChange(yamlScalar, newValue)
    }
}
