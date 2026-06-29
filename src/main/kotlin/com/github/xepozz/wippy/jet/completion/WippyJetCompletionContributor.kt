package com.github.xepozz.wippy.jet.completion

import com.github.xepozz.wippy.jet.WippyJetLanguage
import com.github.xepozz.wippy.jet.psi.WippyJetTypes
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.diagnostic.logger
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext

class WippyJetCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(WippyJetLanguage.INSTANCE),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet,
                ) {
                    if (!isInsideAction(parameters.position)) return
                    LOG.debug("[WippyJetCompletionContributor] contributing keywords/builtins in action")
                    KEYWORDS.forEach { result.addElement(LookupElementBuilder.create(it).bold()) }
                    BUILTINS.forEach {
                        result.addElement(LookupElementBuilder.create(it).withTypeText("builtin", true))
                    }
                }
            },
        )
    }

    private fun isInsideAction(position: PsiElement): Boolean {
        var leaf = PsiTreeUtil.prevLeaf(position)
        while (leaf != null) {
            when (leaf.node.elementType) {
                WippyJetTypes.JET_OPEN -> return true
                WippyJetTypes.JET_CLOSE, WippyJetTypes.JET_CONTENT, WippyJetTypes.JET_COMMENT -> return false
            }
            leaf = PsiTreeUtil.prevLeaf(leaf)
        }
        return false
    }

    companion object {
        private val LOG = logger<WippyJetCompletionContributor>()

        private val KEYWORDS = listOf(
            "extends", "import", "include", "block", "end", "yield", "content", "if", "else",
            "range", "try", "catch", "return", "and", "or", "not", "nil", "msg", "trans", "true", "false",
        )

        private val BUILTINS = listOf(
            "lower", "upper", "hasPrefix", "hasSuffix", "repeat", "replace", "split", "trimSpace",
            "html", "url", "json", "safeHtml", "safeJs", "raw", "writeJson",
            "len", "isset", "exec", "ints", "dump",
        )
    }
}
