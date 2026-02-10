package com.github.xepozz.wippy.sdk

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.AdditionalDataConfigurable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkModel
import com.intellij.openapi.projectRoots.SdkModificator
import com.intellij.openapi.projectRoots.SdkType
import java.io.File
import java.util.concurrent.TimeUnit
import javax.swing.Icon

class WippySdkType : SdkType("Wippy SDK") {
    override fun suggestHomePath(): String? {
        return null
    }

    companion object {
        fun getInstance(): WippySdkType = SdkType.EP_NAME.findExtensionOrFail(WippySdkType::class.java)
        private const val EXECUTABLE_UNIX = "wippy"
        private const val EXECUTABLE_WIN = "wippy.exe"
    }

    override fun suggestHomePaths(project: Project?): Collection<String> {
        val paths = mutableListOf<String>()
        val candidates = listOf(
            "/usr/local/bin",
            "/opt/homebrew/bin",
            "/usr/bin",
            System.getProperty("user.home") + "/.local/bin",
            System.getProperty("user.home") + "/bin",
        )
        candidates.mapNotNull { path ->
            val dir = File(path)
            if (dir.isDirectory && findExecutable(dir) != null) path else null
        }.forEach { paths.add(it) }

        return paths
    }

    override fun isValidSdkHome(path: String): Boolean = findExecutable(File(path)) != null

    override fun suggestSdkName(currentSdkName: String?, sdkHome: String): String = "Wippy"

    override fun getPresentableName(): String = "Wippy SDK"

    override fun createAdditionalDataConfigurable(sdkModel: SdkModel, sdkModificator: SdkModificator): AdditionalDataConfigurable? = null

    override fun getVersionString(sdkHome: String): String? {
        val exe = findExecutable(File(sdkHome)) ?: return null
        return runCatching {
            val proc = ProcessBuilder(exe.absolutePath, "--version")
                .redirectErrorStream(true)
                .start()
            if (!proc.waitFor(1500, TimeUnit.MILLISECONDS)) return null
            val out = proc.inputStream.bufferedReader().readText().trim()
            out.ifEmpty { null }
        }.getOrNull()
    }

    override fun setupSdkPaths(sdk: Sdk) {
        val mod: SdkModificator = sdk.sdkModificator
        mod.commitChanges()
    }

    override fun isRootTypeApplicable(type: com.intellij.openapi.roots.OrderRootType): Boolean = false

    override fun getIcon(): Icon? = null

    override fun saveAdditionalData(additionalData: com.intellij.openapi.projectRoots.SdkAdditionalData, additional: org.jdom.Element) {}

    override fun loadAdditionalData(currentSdk: Sdk, additional: org.jdom.Element): com.intellij.openapi.projectRoots.SdkAdditionalData? = null

    override fun getDefaultDocumentationUrl(sdk: Sdk): String? = null

    private fun findExecutable(dir: File): File? {
        val unix = File(dir, EXECUTABLE_UNIX)
        if (unix.exists() && unix.canExecute()) return unix
        val win = File(dir, EXECUTABLE_WIN)
        if (win.exists() && win.canExecute()) return win
        return null
    }
}
