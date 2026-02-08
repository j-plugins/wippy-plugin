package com.github.xepozz.wippy.reference

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.psi.LuaFuncDef
import com.tang.intellij.lua.psi.LuaLocalFuncDef
import com.tang.intellij.lua.psi.LuaTypes

class WippyMethodReference(
    element: PsiElement,
    private val methodName: String,
    private val luaFile: PsiFile,
    range: TextRange
) : PsiPolyVariantReferenceBase<PsiElement>(element, range, /* soft = */ false) {
    override fun multiResolve(p0: Boolean): Array<out ResolveResult?> {
        return PsiTreeUtil
            .findChildrenOfAnyType(luaFile, LuaFuncDef::class.java, LuaLocalFuncDef::class.java)
            .filter { it.node.findChildByType(LuaTypes.ID)?.text == methodName }
            .let { PsiElementResolveResult.createResults(it) }
    }

    override fun getVariants(): Array<Any> {
        return PsiTreeUtil
            .findChildrenOfAnyType(luaFile, LuaFuncDef::class.java, LuaLocalFuncDef::class.java)
            .mapNotNull { it.node.findChildByType(LuaTypes.ID)?.text }
            .map {
                LookupElementBuilder.create(it)
                    .withIcon(AllIcons.Nodes.Function)
            }
            .toTypedArray()
    }
}