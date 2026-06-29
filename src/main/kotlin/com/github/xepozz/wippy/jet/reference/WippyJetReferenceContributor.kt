package com.github.xepozz.wippy.jet.reference

import com.github.xepozz.wippy.jet.psi.WippyJetNameExpr
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext

class WippyJetReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(WippyJetNameExpr::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
                    val nameExpr = element as? WippyJetNameExpr ?: return PsiReference.EMPTY_ARRAY
                    return arrayOf(WippyJetVariableReference(nameExpr))
                }
            },
        )
    }
}
