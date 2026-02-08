package com.github.xepozz.wippy.lsp

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.platform.lsp.api.LspServerManager

class RestartLspAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        val lspServerManager = LspServerManager.getInstance(project)
        lspServerManager.stopAndRestartIfNeeded(WippySocketLspServerSupportProvider::class.java)
    }
    
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}