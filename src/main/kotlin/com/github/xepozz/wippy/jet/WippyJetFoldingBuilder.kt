package com.github.xepozz.wippy.jet

import com.github.xepozz.wippy.jet.psi.WippyJetBlockBlock
import com.github.xepozz.wippy.jet.psi.WippyJetIfBlock
import com.github.xepozz.wippy.jet.psi.WippyJetRangeBlock
import com.github.xepozz.wippy.jet.psi.WippyJetTryBlock
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class WippyJetFoldingBuilder : FoldingBuilderEx(), DumbAware {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val blocks = buildList<PsiElement> {
            addAll(PsiTreeUtil.findChildrenOfType(root, WippyJetIfBlock::class.java))
            addAll(PsiTreeUtil.findChildrenOfType(root, WippyJetRangeBlock::class.java))
            addAll(PsiTreeUtil.findChildrenOfType(root, WippyJetBlockBlock::class.java))
            addAll(PsiTreeUtil.findChildrenOfType(root, WippyJetTryBlock::class.java))
        }
        return blocks.mapNotNull { block ->
            val range: TextRange = block.textRange
            if (!range.isEmpty && document.getLineNumber(range.startOffset) != document.getLineNumber(range.endOffset)) {
                FoldingDescriptor(block.node, range)
            } else {
                null
            }
        }.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String = "{{...}}"

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}
