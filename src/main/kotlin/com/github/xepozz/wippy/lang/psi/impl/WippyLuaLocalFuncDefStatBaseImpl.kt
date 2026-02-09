package com.github.xepozz.wippy.lang.psi.impl

import com.github.xepozz.wippy.lang.psi.WippyLuaTypes
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

abstract class WippyLuaLocalFuncDefStatBaseImpl : WippyLuaElementImpl, NavigatablePsiElement, PsiNameIdentifierOwner {
    constructor(node: ASTNode) : super(node)

    override fun getName(): String = text
    override fun setName(name: @NlsSafe String): PsiElement? {
        // todo: impplement
//        node.findChildByType(WippyLuaTypes.IDENT)?.psi?.replace(WippyLuaElementFactory.createIdentifier(project, name))
        return this
    }

    override fun getNameIdentifier(): PsiElement? = node.findChildByType(WippyLuaTypes.IDENT)?.psi

    override fun getPresentation() = PresentationData(text, null, getIcon(0), null)

    override fun getIcon(flags: Int) = AllIcons.Nodes.Function

//    override fun getUseScope(): SearchScope {
//        val block = PhpPsiUtil.getParentOfClass(this, PHPOpBlock::class.java) ?: return LocalSearchScope.EMPTY
//        return LocalSearchScope(block)
//    }
}