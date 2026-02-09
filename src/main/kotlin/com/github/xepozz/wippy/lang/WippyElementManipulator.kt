package com.github.xepozz.wippy.lang

import com.github.xepozz.wippy.lang.psi.WippyLuaElementFactory
import com.github.xepozz.wippy.lang.psi.impl.WippyLuaElementImpl
import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException

class WippyElementManipulator : AbstractElementManipulator<WippyLuaElementImpl>() {

    @Throws(IncorrectOperationException::class)
    override fun handleContentChange(entry: WippyLuaElementImpl, range: TextRange, newContent: String): WippyLuaElementImpl {
        val language = entry.language as? WippyLuaLanguage ?: return entry

        val fileType = language.associatedFileType as WippyLuaFileType
        val file = WippyLuaElementFactory
            .createFile(entry.project, entry.text)

        val newEntry = PsiTreeUtil.findChildOfType(file, WippyLuaElementImpl::class.java)

        return when (newEntry) {
            null -> entry
            else -> entry.replace(newEntry) as WippyLuaElementImpl
        }
    }
}