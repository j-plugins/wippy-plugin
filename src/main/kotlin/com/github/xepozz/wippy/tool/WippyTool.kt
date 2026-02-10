package com.github.xepozz.wippy.tool

import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Tag

@Tag("wippy-tool")
data class WippyTool(
    @Attribute("id") var id: String = "",
    @Attribute("name") var name: String = "",
    @Attribute("type") var type: WippyToolType = WippyToolType.LOCAL,
    @Attribute("path") var path: String = "",
    @Attribute("host") var host: String = "127.0.0.1",
    @Attribute("port") var port: Int = 7777,
) {
    val displayName: String
        get() = name.ifEmpty {
            when (type) {
                WippyToolType.LOCAL -> "Local: $path"
                WippyToolType.REMOTE -> "Remote: $host:$port"
            }
        }

    fun copy() = WippyTool(id, name, type, path, host, port)
}

enum class WippyToolType {
    LOCAL, REMOTE
}