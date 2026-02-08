package com.github.xepozz.wippy.project

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import org.jetbrains.yaml.YAMLUtil
import org.jetbrains.yaml.psi.YAMLFile
import com.intellij.psi.PsiManager

/**
 * Scans the IntelliJ project for `wippy.lock` files and builds [WippyProject] models.
 *
 * Parses:
 * ```yaml
 * directories:
 *     modules: .wippy
 *     src: ./src
 * ```
 */
class WippyProjectDetector(private val project: Project) {

    companion object {
        private val LOG = logger<WippyProjectDetector>()
        const val LOCK_FILENAME = "wippy.lock"
        private const val MAX_DEPTH = 5
        private val SKIP_DIRS = setOf(
            ".git", ".idea", "node_modules", ".wippy",
            "vendor", "build", "out", "dist", ".gradle"
        )
    }

    /**
     * Finds all Wippy projects under the IntelliJ project base directory.
     */
    fun detectAll(): List<WippyProject> {
        val baseDir = project.baseDir ?: return emptyList()
        val lockFiles = mutableListOf<VirtualFile>()

        VfsUtilCore.visitChildrenRecursively(baseDir, object : VirtualFileVisitor<Unit>(limit(MAX_DEPTH)) {
            override fun visitFile(file: VirtualFile): Boolean {
                if (file.isDirectory && file.name in SKIP_DIRS) return false
                if (!file.isDirectory && file.name == LOCK_FILENAME) lockFiles.add(file)
                return true
            }
        })

        LOG.info("Found ${lockFiles.size} wippy.lock file(s)")
        return lockFiles.mapNotNull { parseLockFile(it) }
    }

    private fun parseLockFile(lockFile: VirtualFile): WippyProject? {
        val root = lockFile.parent ?: return null

        return try {
            val psiFile = PsiManager.getInstance(project).findFile(lockFile)
            if (psiFile !is YAMLFile) {
                return buildDefault(root, lockFile)
            }

            val srcPath = YAMLUtil.getQualifiedKeyInFile(psiFile, "directories", "src")
                ?.valueText
                ?.removePrefix("./")
                ?: "src"

            val modulesPath = YAMLUtil.getQualifiedKeyInFile(psiFile, "directories", "modules")
                ?.valueText
                ?.removePrefix("./")
                ?: ".wippy"

            WippyProject(
                root = root,
                lockFile = lockFile,
                srcDir = root.findFileByRelativePath(srcPath),
                modulesDir = root.findFileByRelativePath(modulesPath),
            )
        } catch (e: ReadAction.CannotReadException) {
            throw e
        } catch (e: Exception) {
            LOG.warn("Failed to parse ${lockFile.path}", e)
            buildDefault(root, lockFile)
        }
    }

    private fun buildDefault(root: VirtualFile, lockFile: VirtualFile) = WippyProject(
        root = root,
        lockFile = lockFile,
        srcDir = root.findChild("src"),
        modulesDir = root.findChild(".wippy"),
    )
}