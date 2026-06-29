package com.github.xepozz.wippy.jet.file

import com.github.xepozz.wippy.jet.psi.WippyJetTypes
import com.intellij.lang.Language
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.psi.templateLanguages.ConfigurableTemplateLanguageFileViewProvider
import com.intellij.psi.templateLanguages.TemplateDataElementType
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings
import java.util.concurrent.ConcurrentHashMap

class WippyJetFileViewProvider(
    manager: PsiManager,
    virtualFile: VirtualFile,
    eventSystemEnabled: Boolean,
    private val baseLanguage: Language,
) : MultiplePsiFilesPerDocumentFileViewProvider(manager, virtualFile, eventSystemEnabled),
    ConfigurableTemplateLanguageFileViewProvider {

    private val LOG = logger<WippyJetFileViewProvider>()

    override fun getBaseLanguage(): Language = baseLanguage

    override fun getTemplateDataLanguage(): Language {
        val mapped = TemplateDataLanguageMappings.getInstance(manager.project).getMapping(virtualFile)
        val lang = mapped ?: HTMLLanguage.INSTANCE
        LOG.debug("[WippyJetFileViewProvider] template-data language for ${virtualFile.name}: ${lang.id}")
        return lang
    }

    override fun getLanguages(): Set<Language> = setOf(baseLanguage, templateDataLanguage)

    override fun createFile(lang: Language): PsiFile? {
        val parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(lang) ?: return null
        if (lang === templateDataLanguage) {
            val file = parserDefinition.createFile(this) as PsiFileImpl
            file.contentElementType = templateDataElementType(templateDataLanguage)
            return file
        }
        if (lang === baseLanguage) {
            return parserDefinition.createFile(this)
        }
        return null
    }

    override fun cloneInner(virtualFile: VirtualFile): MultiplePsiFilesPerDocumentFileViewProvider =
        WippyJetFileViewProvider(manager, getVirtualFile(), false, baseLanguage)

    companion object {
        private val TEMPLATE_DATA_BY_LANGUAGE = ConcurrentHashMap<Language, TemplateDataElementType>()

        private fun templateDataElementType(dataLanguage: Language): TemplateDataElementType =
            TEMPLATE_DATA_BY_LANGUAGE.computeIfAbsent(dataLanguage) {
                TemplateDataElementType(
                    "WIPPY_JET_TEMPLATE_DATA_${it.id}",
                    it,
                    WippyJetTypes.JET_CONTENT,
                    WippyJetTypes.JET_FRAGMENT,
                )
            }
    }
}
