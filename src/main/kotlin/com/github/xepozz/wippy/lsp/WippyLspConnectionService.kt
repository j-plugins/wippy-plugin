package com.github.xepozz.wippy.lsp

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import java.io.IOException
import java.net.Socket

@Service(Service.Level.PROJECT)
class WippyLspConnectionService(private val project: Project) : Disposable {
    
    private var connectionAttempts = 0
    private val maxAttempts = 3
    
    fun isServerAvailable(): Boolean {
        val settings = WippyLspSettings.getInstance().state
        return try {
            Socket(settings.host, settings.port).use { true }
        } catch (e: IOException) {
            false
        }
    }
    
    fun checkConnection(): Boolean {
        if (!isServerAvailable()) {
            connectionAttempts++
            if (connectionAttempts >= maxAttempts) {
                thisLogger().warn("Wippy LSP server not available after $maxAttempts attempts")
                return false
            }
            thisLogger().info("Wippy LSP server not available, attempt $connectionAttempts/$maxAttempts")
            return false
        }
        connectionAttempts = 0
        return true
    }
    
    override fun dispose() {
    }
    
    companion object {
        fun getInstance(project: Project): WippyLspConnectionService = project.service()
    }
}