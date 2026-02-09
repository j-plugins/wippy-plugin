package com.github.xepozz.wippy.lang.psi

import com.github.xepozz.wippy.lang.WippyLuaLanguage
import com.intellij.psi.tree.IElementType

class WippyLuaElementType(debugName: String) : IElementType("WippyLuaElementType($debugName)", WippyLuaLanguage.INSTANCE)
