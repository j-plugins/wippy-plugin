package com.github.xepozz.wippy.lang

import com.github.xepozz.wippy.lang.psi.WippyLuaTypes
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

class WippyLuaBraceMatcher : PairedBraceMatcher {
    override fun getPairs(): Array<BracePair> = PAIRS

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset

    companion object {
        private val PAIRS = arrayOf(
            BracePair(WippyLuaTypes.LPAREN, WippyLuaTypes.RPAREN, false),
            BracePair(WippyLuaTypes.LBRACE, WippyLuaTypes.RBRACE, true),
            BracePair(WippyLuaTypes.LBRACK, WippyLuaTypes.RBRACK, false)
        )
    }
}
