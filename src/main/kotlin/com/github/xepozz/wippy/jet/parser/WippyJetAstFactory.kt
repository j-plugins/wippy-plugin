package com.github.xepozz.wippy.jet.parser

import com.github.xepozz.wippy.jet.psi.WippyJetTypes
import com.intellij.lang.ASTFactory
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.templateLanguages.OuterLanguageElementImpl
import com.intellij.psi.tree.IElementType

class WippyJetAstFactory : ASTFactory() {
    override fun createLeaf(type: IElementType, text: CharSequence): LeafElement? {
        if (type === WippyJetTypes.JET_FRAGMENT) {
            return OuterLanguageElementImpl(type, text)
        }
        return null
    }
}
