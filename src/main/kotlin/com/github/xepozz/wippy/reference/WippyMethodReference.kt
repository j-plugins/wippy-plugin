package com.github.xepozz.wippy.reference

import com.github.xepozz.wippy.lang.psi.WippyLuaLocalFuncDefStat
import com.github.xepozz.wippy.lang.psi.WippyLuaTypes
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.PsiTreeUtil

class WippyMethodReference(
    element: PsiElement,
    private val methodName: String,
    private val luaFile: PsiFile,
    range: TextRange
) : PsiPolyVariantReferenceBase<PsiElement>(element, range, /* soft = */ false) {
    override fun multiResolve(p0: Boolean): Array<out ResolveResult?> {
//        return emptyArray()
        return findFunctionByName(luaFile, methodName)
            .let { PsiElementResolveResult.createResults(it) }
    }

    override fun getVariants(): Array<Any> {
        return getLocalFunctions(luaFile)
            .mapNotNull { it.node.findChildByType(WippyLuaTypes.IDENT)?.text }
            .map {
                LookupElementBuilder.create(it)
                    .withIcon(AllIcons.Nodes.Function)
            }
            .toTypedArray()
    }

    private fun getLocalFunctions(luaFile: PsiFile): Collection<WippyLuaLocalFuncDefStat> = PsiTreeUtil
        .findChildrenOfAnyType(luaFile, WippyLuaLocalFuncDefStat::class.java)

    private fun findFunctionByName(luaFile: PsiFile, methodName: String): List<WippyLuaLocalFuncDefStat?> =
        getLocalFunctions(luaFile)
            .filter { it.node.findChildByType(WippyLuaTypes.IDENT)?.text == methodName }
}