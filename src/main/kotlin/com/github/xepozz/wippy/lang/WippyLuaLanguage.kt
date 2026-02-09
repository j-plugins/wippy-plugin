package com.github.xepozz.wippy.lang

import com.intellij.lang.Language

class WippyLuaLanguage : Language("WippyLua") {
    companion object {
        @JvmStatic
        val INSTANCE = WippyLuaLanguage();
    }
}
