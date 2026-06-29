package com.github.xepozz.wippy.jet.psi

import com.github.xepozz.wippy.jet.WippyJetLanguage
import com.intellij.psi.tree.IElementType

class WippyJetTokenType(debugName: String) : IElementType(debugName, WippyJetLanguage.INSTANCE) {
    override fun toString() = "WippyJetTokenType." + super.toString()
}
