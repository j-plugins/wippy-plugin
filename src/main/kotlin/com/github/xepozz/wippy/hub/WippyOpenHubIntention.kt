package com.github.xepozz.wippy.hub

import com.github.xepozz.wippy.MyBundle
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class WippyOpenHubIntention : IntentionAction {

    private val LOG = logger<WippyOpenHubIntention>()

    override fun getText(): String = MyBundle.message("hub.openOnHub")

    override fun getFamilyName(): String = MyBundle.message("hub.familyName")

    override fun startInWriteAction(): Boolean = false

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        val element = elementAtCaret(editor, file) ?: return false
        val available = WippyHubUrl.urlFor(element) != null
        if (available) LOG.debug("[WippyOpenHubIntention] available at caret")
        return available
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        val element = elementAtCaret(editor, file) ?: return
        val url = WippyHubUrl.urlFor(element) ?: return
        LOG.info("[WippyOpenHubIntention] opening $url")
        BrowserUtil.browse(url)
    }

    private fun elementAtCaret(editor: Editor?, file: PsiFile?): PsiElement? {
        if (editor == null || file == null) return null
        return file.findElementAt(editor.caretModel.offset)
    }
}
