package com.github.xepozz.wippy.lsp

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspCommunicationChannel
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor

class WippySocketLspServerDescriptor(project: Project, private val port: Int = 7777) : 
    ProjectWideLspServerDescriptor(project, "Wippy LSP") {

    override val lspCommunicationChannel: LspCommunicationChannel = LspCommunicationChannel.Socket(port, false)

    override fun isSupportedFile(file: VirtualFile): Boolean {
        return file.extension == "lua"
    }

    override fun createInitializationOptions(): Any {
        return mapOf(
            "wippy" to mapOf(
                "trace" to "verbose",
                "modules" to listOf(
                    "registry", "llm", "prompt", "agent_runner", "embeddings",
                    "process", "channel", "actor",
                    "http", "http_client", "websocket",
                    "json", "yaml", "base64", "compress", "payload", "excel",
                    "sql", "store", "fs", "cloudstorage", "queue",
                    "exec", "time", "logger", "env", "crypto", "uuid", "io", "metrics", "ostime",
                    "text", "template", "treesitter",
                    "security", "hash",
                    "eval", "expression"
                )
            )
        )
    }

    override fun findFileByUri(fileUri: String): VirtualFile? {
        println("findFileByUri: $fileUri")
        return super.findFileByUri(fileUri)
            .also { println("findFileByUri result: $it") }
    }

    override fun findLocalFileByPath(path: String): VirtualFile? {
        println("findLocalFileByPath: $path")
        return super.findLocalFileByPath(path)
            .also { println("findLocalFileByPath result: $it") }
    }

    override fun getFilePath(file: VirtualFile): String {
        println("getFilePath: ${file.path}")
        return super.getFilePath(file)
            .also { println("getFilePath result: $it") }
    }

    override fun getFileUri(file: VirtualFile): String {
        println("getFileUri: ${file.path}")
//        return super.getFileUri(file)
        return "wippy://app:${file.nameWithoutExtension}"
            .also { println("getFileUri result: ${it}") }
    }
}