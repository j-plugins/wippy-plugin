package com.github.xepozz.wippy.lang

import com.github.xepozz.wippy.lang.lexer._WippyLuaLexer
import com.intellij.lexer.FlexAdapter

class WippyLuaLexerAdapter : FlexAdapter(_WippyLuaLexer(null))
