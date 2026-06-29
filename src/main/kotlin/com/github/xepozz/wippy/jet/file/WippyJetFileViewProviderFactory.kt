package com.github.xepozz.wippy.jet.file

import com.github.xepozz.wippy.jet.WippyJetLanguage
import com.intellij.lang.Language
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.FileViewProvider
import com.intellij.psi.FileViewProviderFactory
import com.intellij.psi.PsiManager

class WippyJetFileViewProviderFactory : FileViewProviderFactory {
    override fun createFileViewProvider(
        file: VirtualFile,
        language: Language,
        manager: PsiManager,
        eventSystemEnabled: Boolean,
    ): FileViewProvider = WippyJetFileViewProvider(manager, file, eventSystemEnabled, WippyJetLanguage.INSTANCE)
}
