package com.github.xepozz.wippy.lang

import com.github.xepozz.wippy.lang.psi.WippyLuaFuncDefStat
import com.github.xepozz.wippy.lang.psi.WippyLuaLocalFuncDefStat
import com.github.xepozz.wippy.lang.psi.WippyLuaNameExpr
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.psi.PsiElement

class WippyAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is WippyLuaNameExpr -> {
                if (element.reference?.resolve() != null) {
                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(element.textRange)
                        .textAttributes(DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
                        .create()
                }
            }

            is WippyLuaLocalFuncDefStat -> {
                element.nameIdentifier?.let {
                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(it.textRange)
                        .textAttributes(DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
                        .create()
                }
            }

            is WippyLuaFuncDefStat -> {
                element.funcName?.nameOrKeyword?.let {
                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(it.textRange)
                        .textAttributes(DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
                        .create()
                }
            }
        }
    }
}