package com.github.xepozz.wippy.sdk

import java.net.URI

class WippySdkDownload {
    fun resolveDownloadUrl(version: String = "latest"): URI {
        val os = detectOs()
        val arch = detectArch()
        val v = if (version == "latest") "latest" else "v$version"
        val file = when (os) {
            Os.MAC -> "wippy-${v}-${arch}-macos.tar.gz"
            Os.LINUX -> "wippy-${v}-${arch}-linux.tar.gz"
            Os.WINDOWS -> "wippy-${v}-${arch}-windows.zip"
        }
        return URI("https://example.com/wippy/releases/${file}")
    }

    private fun detectOs(): Os = when {
        System.getProperty("os.name").contains("Mac", ignoreCase = true) -> Os.MAC
        System.getProperty("os.name").contains("Windows", ignoreCase = true) -> Os.WINDOWS
        else -> Os.LINUX
    }

    private fun detectArch(): String = when (System.getProperty("os.arch").lowercase()) {
        "aarch64", "arm64" -> "arm64"
        else -> "x86_64"
    }
}

enum class Os { MAC, LINUX, WINDOWS }
