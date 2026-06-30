package com.github.xepozz.wippy.jet.highlighting

import com.github.xepozz.wippy.jet.psi.WippyJetTypes
import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.ex.util.LayerDescriptor
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings

class WippyJetTemplateHighlighter(
    project: Project?,
    virtualFile: VirtualFile?,
    colors: EditorColorsScheme,
) : LayeredLexerEditorHighlighter(WippyJetSyntaxHighlighter(), colors) {

    init {
        val dataLanguage = if (project != null && virtualFile != null) {
            TemplateDataLanguageMappings.getInstance(project).getMapping(virtualFile) ?: HTMLLanguage.INSTANCE
        } else {
            HTMLLanguage.INSTANCE
        }

        val dataHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(dataLanguage, project, virtualFile)
        if (dataHighlighter != null) {
            registerLayer(WippyJetTypes.JET_CONTENT, LayerDescriptor(dataHighlighter, ""))
            LOG.debug("[WippyJetTemplateHighlighter] layered ${dataLanguage.id} over JET_CONTENT")
        } else {
            LOG.debug("[WippyJetTemplateHighlighter] no syntax highlighter for ${dataLanguage.id}; layer skipped")
        }
    }

    companion object {
        private val LOG = logger<WippyJetTemplateHighlighter>()
    }
}
