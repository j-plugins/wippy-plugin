package com.github.xepozz.wippy.jet

import com.intellij.lang.Language
import com.intellij.psi.templateLanguages.TemplateLanguage

class WippyJetLanguage : Language("WippyJet"), TemplateLanguage {
    companion object {
        @JvmStatic
        val INSTANCE = WippyJetLanguage()
    }
}
