package com.github.xepozz.wippy.lang

import com.github.xepozz.wippy.lang.psi.WippyLuaBlock
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class WippyLuaFoldingBuilder : FoldingBuilderEx(), DumbAware {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        return PsiTreeUtil
            .findChildrenOfType(root, WippyLuaBlock::class.java)
            .mapNotNull { block ->
                val range = block.textRange
                when {
                    !range.isEmpty -> FoldingDescriptor(block, range)
                    else -> null
                }
            }
            .toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String = "..."

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}
