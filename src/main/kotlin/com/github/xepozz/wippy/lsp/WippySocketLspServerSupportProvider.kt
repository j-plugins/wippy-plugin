package com.github.xepozz.wippy.lsp

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider

class WippySocketLspServerSupportProvider : LspServerSupportProvider {
    
    override fun fileOpened(
        project: Project, 
        file: VirtualFile, 
        serverStarter: LspServerSupportProvider.LspServerStarter
    ) {
        val ext = file.extension ?: return
        if (ext == "lua" ) {
            serverStarter.ensureServerStarted(WippySocketLspServerDescriptor(project))
        }
    }
}