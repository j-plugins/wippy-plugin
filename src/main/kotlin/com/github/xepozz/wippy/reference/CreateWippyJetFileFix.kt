package com.github.xepozz.wippy.reference

import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile

class CreateWippyJetFileFix(
    private val filePath: String,
    private val baseDir: PsiDirectory,
) : BaseIntentionAction() {

    override fun getText(): String = "Create file '$filePath'"

    override fun getFamilyName(): String = "Wippy"

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean = true

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        runWriteAction {
            val parts = filePath.split("/")
            var dir = baseDir
            for (i in 0 until parts.size - 1) {
                dir = dir.findSubdirectory(parts[i]) ?: dir.createSubdirectory(parts[i])
            }

            val fileName = parts.last()
            val newFile = dir.createFile(fileName)
            val document = newFile.viewProvider.document ?: return@runWriteAction
            document.setText(JET_TEMPLATE)

            newFile.virtualFile?.let {
                FileEditorManager.getInstance(project).openFile(it, true)
            }
        }
    }

    companion object {
        private val JET_TEMPLATE = """
            {{ extends "layout" }}

            {{ block body() }}
            {{ end }}
        """.trimIndent() + "\n"
    }
}
