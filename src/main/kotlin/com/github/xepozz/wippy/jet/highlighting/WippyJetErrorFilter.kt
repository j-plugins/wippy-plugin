package com.github.xepozz.wippy.jet.highlighting

import com.github.xepozz.wippy.jet.WippyJetLanguage
import com.intellij.codeInsight.highlighting.HighlightErrorFilter
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.templateLanguages.OuterLanguageElement
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider
import com.intellij.psi.util.PsiTreeUtil

class WippyJetErrorFilter : HighlightErrorFilter() {
    override fun shouldHighlightErrorElement(element: PsiErrorElement): Boolean {
        val viewProvider = element.containingFile?.viewProvider as? TemplateLanguageFileViewProvider ?: return true
        if (viewProvider.baseLanguage !== WippyJetLanguage.INSTANCE) return true
        return !isNearOuter(element)
    }

    private fun isNearOuter(element: PsiElement): Boolean {
        if (PsiTreeUtil.getParentOfType(element, OuterLanguageElement::class.java, false) != null) return true
        val prev = PsiTreeUtil.skipWhitespacesBackward(element)
        val next = PsiTreeUtil.skipWhitespacesForward(element)
        return prev is OuterLanguageElement || next is OuterLanguageElement
    }
}
