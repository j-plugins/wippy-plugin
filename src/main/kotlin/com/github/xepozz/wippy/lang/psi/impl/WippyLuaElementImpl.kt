package com.github.xepozz.wippy.lang.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.SharedPsiElementImplUtil
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry

open class WippyLuaElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    override fun getReferences(): Array<PsiReference> = buildList {
        addAll(ReferenceProvidersRegistry.getReferencesFromProviders(this@WippyLuaElementImpl))
        addAll(SharedPsiElementImplUtil.getReferences(this@WippyLuaElementImpl))
    }.toTypedArray()
}