package com.github.xepozz.wippy.lang

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider

class WippyLuaFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, WippyLuaLanguage.INSTANCE) {
    override fun getFileType() = WippyLuaFileType.INSTANCE
    override fun toString() = "Wippy Lua File"
}
