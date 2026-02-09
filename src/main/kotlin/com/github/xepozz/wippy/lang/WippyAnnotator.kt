package com.github.xepozz.wippy.lang

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement

class WippyAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
//            is WippyLuaLocalStat -> {
//                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
//                    .range(element)
//                    .textAttributes(DefaultLanguageHighlighterColors.LOCAL_VARIABLE)
//                    .create()
//            }
//
//            is PHPOpLineNumber -> {
//                if (element.parent is PHPOpStatement) {
//                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
//                        .range(element)
//                        .highlightType(ProblemHighlightType.LIKE_UNUSED_SYMBOL)
//                        .create()
//                } else {
//                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
//                        .range(element)
//                        .textAttributes(PhpHighlightingData.NUMBER)
//                        .create()
//                }
//            }
//
//            is PHPOpParameter -> {
//                val attributesKey = when {
//                    element.isVariable && element.parent !is PHPOpParenParameter -> PhpHighlightingData.VAR
//                    element.text == Opcodes.THIS.name -> PhpHighlightingData.THIS_VAR
//                    isPrimitive(element) -> PhpHighlightingData.PRIMITIVE_TYPE_HINT
//                    else -> PhpHighlightingData.FUNCTION_CALL
//                }
//
//                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
//                    .range(element)
//                    .textAttributes(attributesKey)
//                    .create()
//
//            }
//
//            is PHPOpFunctionName -> {
//                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
//                    .range(element.textRange)
//                    .textAttributes(PhpHighlightingData.FUNCTION)
//                    .create()
//            }
//
//            is PHPOpClassName -> {
//                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
//                    .range(element.textRange)
//                    .textAttributes(PhpHighlightingData.CLASS)
//                    .create()
//            }
//
//            is PHPOpMethodName -> {
//                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
//                    .range(element.methodNameRange.shiftRight(element.textOffset))
//                    .textAttributes(PhpHighlightingData.STATIC_METHOD_CALL)
//                    .create()
//            }
//
//            is PHPOpPropertyName -> {
//                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
//                    .range(element.propertyNameRange.shiftRight(element.textOffset))
//                    .textAttributes(PhpHighlightingData.INSTANCE_FIELD)
//                    .create()
//            }
//
//            is PHPOpPropertyHookName -> {
//                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
//                    .range(element.propertyHookNameRange.shiftRight(element.textOffset))
//                    .textAttributes(PhpHighlightingData.INSTANCE_METHOD_CALL)
//                    .create()
//            }
        }
    }
}