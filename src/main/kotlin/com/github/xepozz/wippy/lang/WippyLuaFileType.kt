package com.github.xepozz.wippy.lang

import com.github.xepozz.wippy.WippyIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

class WippyLuaFileType : LanguageFileType(WippyLuaLanguage.INSTANCE) {
    override fun getName() = "WippyLua"
    override fun getDescription() = "Wippy Lua file"
    override fun getDefaultExtension() = "lua"
    override fun getIcon(): Icon = WippyIcons.WIPPY

    companion object {
        @JvmStatic
        val INSTANCE = WippyLuaFileType()
    }
}
