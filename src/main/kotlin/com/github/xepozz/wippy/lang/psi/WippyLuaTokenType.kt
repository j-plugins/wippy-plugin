package com.github.xepozz.wippy.lang.psi

import com.github.xepozz.wippy.lang.WippyLuaLanguage
import com.intellij.psi.tree.IElementType

class WippyLuaTokenType(debugName: String) : IElementType(debugName, WippyLuaLanguage.INSTANCE) {
    override fun toString() = "WippyLuaTokenType." + super.toString()
}
