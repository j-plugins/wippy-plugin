package com.github.xepozz.wippy.jet.reference

import com.github.xepozz.wippy.jet.psi.WippyJetNameExpr
import com.github.xepozz.wippy.jet.psi.WippyJetParam
import com.github.xepozz.wippy.jet.psi.WippyJetRangeVars
import com.github.xepozz.wippy.jet.psi.WippyJetSetTarget
import com.github.xepozz.wippy.jet.psi.WippyJetTypes
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil

class WippyJetVariableReference(element: WippyJetNameExpr) :
    PsiReferenceBase<WippyJetNameExpr>(element, TextRange(0, element.textLength), true) {

    override fun resolve(): PsiElement? {
        val name = element.text
        val file = element.containingFile ?: return null
        val usageOffset = element.textRange.startOffset

        val declarations = collectDeclarations(file).filter { it.text == name }
        if (declarations.isEmpty()) return null

        val resolved = declarations
            .filter { it.textRange.startOffset <= usageOffset }
            .maxByOrNull { it.textRange.startOffset }
            ?: declarations.first()

        LOG.debug("[WippyJetVariableReference] '$name' -> ${if (resolved == element) "self" else "decl@${resolved.textRange.startOffset}"}")
        return if (resolved === element) null else resolved
    }

    private fun collectDeclarations(file: PsiElement): List<PsiElement> {
        val result = mutableListOf<PsiElement>()
        PsiTreeUtil.findChildrenOfType(file, WippyJetSetTarget::class.java).forEach { result += identsOf(it) }
        PsiTreeUtil.findChildrenOfType(file, WippyJetRangeVars::class.java).forEach { result += identsOf(it) }
        PsiTreeUtil.findChildrenOfType(file, WippyJetParam::class.java).forEach { result += identsOf(it) }
        return result
    }

    private fun identsOf(holder: PsiElement): List<PsiElement> =
        holder.node.getChildren(null)
            .filter { it.elementType == WippyJetTypes.IDENT }
            .mapNotNull { it.psi }

    companion object {
        private val LOG = logger<WippyJetVariableReference>()
    }
}
