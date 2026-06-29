package com.github.xepozz.wippy.jet

import com.github.xepozz.wippy.jet.psi.WippyJetTypes
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

class WippyJetBraceMatcher : PairedBraceMatcher {
    override fun getPairs(): Array<BracePair> = PAIRS

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset

    companion object {
        private val PAIRS = arrayOf(
            BracePair(WippyJetTypes.JET_OPEN, WippyJetTypes.JET_CLOSE, true),
            BracePair(WippyJetTypes.LPAREN, WippyJetTypes.RPAREN, false),
            BracePair(WippyJetTypes.LBRACK, WippyJetTypes.RBRACK, false),
        )
    }
}
