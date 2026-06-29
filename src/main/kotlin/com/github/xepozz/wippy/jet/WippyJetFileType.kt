package com.github.xepozz.wippy.jet

import com.github.xepozz.wippy.WippyIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

class WippyJetFileType : LanguageFileType(WippyJetLanguage.INSTANCE) {
    override fun getName() = "WippyJet"
    override fun getDescription() = "Wippy Jet template"
    override fun getDefaultExtension() = "jet"
    override fun getIcon(): Icon = WippyIcons.WIPPY

    companion object {
        @JvmStatic
        val INSTANCE = WippyJetFileType()
    }
}
