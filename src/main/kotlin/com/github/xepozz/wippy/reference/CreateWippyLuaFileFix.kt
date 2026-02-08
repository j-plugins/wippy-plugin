package com.github.xepozz.wippy.reference

import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile

/**
 * Quick fix: creates a missing Lua file referenced by `source: file://...`
 * with a basic Wippy module template.
 */
class CreateWippyLuaFileFix(
    private val filePath: String,
    private val baseDir: PsiDirectory
) : BaseIntentionAction() {

    override fun getText(): String = "Create file '$filePath'"

    override fun getFamilyName(): String = "Wippy"

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean = true

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        runWriteAction {
            val parts = filePath.split("/")
            var dir = baseDir

            // Create subdirectories if needed (e.g., file://sub/module.lua)
            for (i in 0 until parts.size - 1) {
                dir = dir.findSubdirectory(parts[i])
                    ?: dir.createSubdirectory(parts[i])
            }

            val fileName = parts.last()
            val moduleName = fileName.removeSuffix(".lua")

            val template = buildLuaTemplate(moduleName)
            val newFile = dir.createFile(fileName)

            // Write template content
            val document = newFile.viewProvider.document ?: return@runWriteAction
            document.setText(template)

            // Open the new file in editor
            newFile.virtualFile?.let {
                FileEditorManager.getInstance(project).openFile(it, true)
            }
        }
    }

    private fun buildLuaTemplate(moduleName: String): String = """
--- ${moduleName} module
--- Created by Wippy plugin

local function main(): integer
    -- TODO: implement
    return 0
end

return { main = main }
""".trimIndent() + "\n"
}