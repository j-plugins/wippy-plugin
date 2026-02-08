package com.github.xepozz.wippy.project

import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.jps.model.java.JavaSourceRootType

class WippySourceRootConfigurator : ProjectActivity {

    companion object {
        private val LOG = logger<WippySourceRootConfigurator>()
    }

    override suspend fun execute(project: Project) {
        val service = WippyProjectService.getInstance(project)
        val count = readAction { service.rescan() }

        if (count == 0) return

        writeAction {
            configureRoots(project, service.wippyProjects)
        }
    }

    private fun configureRoots(project: Project, wippyProjects: List<WippyProject>) {
        val moduleManager = ModuleManager.getInstance(project)
        val module = moduleManager.modules.firstOrNull() ?: return

        val rootModel = ModuleRootManager.getInstance(module).modifiableModel

        try {
            for (wp in wippyProjects) {
                addSourceRoot(rootModel, wp)
                excludeModulesDir(rootModel, wp)
            }
            rootModel.commit()
            LOG.info("Configured source roots for ${wippyProjects.size} Wippy project(s)")
        } catch (e: Exception) {
            rootModel.dispose()
            LOG.error("Failed to configure Wippy source roots", e)
        }
    }

    private fun addSourceRoot(rootModel: ModifiableRootModel, wp: WippyProject) {
        val srcDir = wp.srcDir ?: return

        // Check if already registered
        val existing = rootModel.contentEntries.flatMap { it.sourceFolders.toList() }
        if (existing.any { it.file == srcDir }) {
            LOG.info("Source root already registered: ${srcDir.path}")
            return
        }

        // Find or create content entry that covers this directory
        val contentEntry = rootModel.contentEntries
            .firstOrNull { covers(it.file, srcDir) }
            ?: rootModel.addContentEntry(wp.root)

        contentEntry.addSourceFolder(srcDir, JavaSourceRootType.SOURCE)
        LOG.info("Added source root: ${srcDir.path}")
    }

    private fun excludeModulesDir(rootModel: ModifiableRootModel, wp: WippyProject) {
        val modulesDir = wp.modulesDir ?: return

        val contentEntry = rootModel.contentEntries
            .firstOrNull { covers(it.file, modulesDir) }
            ?: return

        val alreadyExcluded = contentEntry.excludeFolders.any { it.file == modulesDir }
        if (alreadyExcluded) return

        contentEntry.addExcludeFolder(modulesDir)
        LOG.info("Excluded modules dir: ${modulesDir.path}")
    }

    private fun covers(parent: VirtualFile?, child: VirtualFile): Boolean {
        if (parent == null) return false
        return child.path.startsWith(parent.path)
    }
}