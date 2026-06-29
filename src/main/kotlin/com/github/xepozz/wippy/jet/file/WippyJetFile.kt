package com.github.xepozz.wippy.jet.file

import com.github.xepozz.wippy.jet.WippyJetFileType
import com.github.xepozz.wippy.jet.WippyJetLanguage
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider

class WippyJetFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, WippyJetLanguage.INSTANCE) {
    override fun getFileType() = WippyJetFileType.INSTANCE
    override fun toString() = "Wippy Jet File"
}
