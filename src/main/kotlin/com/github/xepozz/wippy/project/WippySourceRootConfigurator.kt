package com.github.xepozz.wippy.project

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.jps.entities.ContentRootEntity
import com.intellij.platform.workspace.jps.entities.ExcludeUrlEntity
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.jps.entities.ModuleId
import com.intellij.platform.workspace.jps.entities.ModuleTypeId
import com.intellij.platform.workspace.jps.entities.SourceRootEntity
import com.intellij.platform.workspace.jps.entities.SourceRootTypeId
import com.intellij.workspaceModel.ide.legacyBridge.LegacyBridgeJpsEntitySourceFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val sourceRootTypeId = SourceRootTypeId("wippy-resource")
val wippyModuleTypeId = ModuleTypeId("wippy-module")

class WippySourceRootConfigurator : ProjectActivity {

    override suspend fun execute(project: Project) {
        val service = WippyProjectService.getInstance(project)
        val count = readAction { service.rescan() }
        if (count == 0) return

        try {
            configureModules(project, service.wippyProjects)
        } catch (t: Throwable) {
            thisLogger().error(t)
        }
    }

    private suspend  fun configureModules(project: Project, wippyProjects: List<WippyProject>) {
        val workspaceModel = WorkspaceModel.getInstance(project)
        val vfu = workspaceModel.getVirtualFileUrlManager()
        val snapshot = workspaceModel.currentSnapshot

        val moduleEntities = wippyProjects.mapNotNull { wp ->
            val moduleName = "wippy-${wp.root.name}"
            if (ModuleId(moduleName) in snapshot) {
                thisLogger().info("Module already exists: $moduleName")
                return@mapNotNull null
            }

            val baseDirUrl = vfu.getOrCreateFromUrl(wp.root.url)
            val entitySource = LegacyBridgeJpsEntitySourceFactory
                .getInstance(project)
                .createEntitySourceForModule(baseDirUrl, null)

            val sourceRoots = buildList {
                wp.srcDir?.let {
                    add(SourceRootEntity(vfu.getOrCreateFromUrl(it.url), sourceRootTypeId, entitySource))
                }
            }

            val excludeUrls = buildList {
                wp.modulesDir?.let {
                    add(ExcludeUrlEntity(vfu.getOrCreateFromUrl(it.url), entitySource))
                }
            }

            val contentRoot = ContentRootEntity(baseDirUrl, emptyList(), entitySource) {
                this.sourceRoots = sourceRoots.toMutableList()
                this.excludedUrls = excludeUrls.toMutableList()
            }

            ModuleEntity(moduleName, emptyList(), entitySource) {
                this.contentRoots = mutableListOf(contentRoot)
                this.type = wippyModuleTypeId
            }
        }

        println("modules: $moduleEntities")
        if (moduleEntities.isEmpty()) return

        ApplicationManager.getApplication().executeOnPooledThread{

            CoroutineScope(Dispatchers.IO).launch {
//            withContext(Dispatchers.IO) {
//        ApplicationManager.getApplication().invokeAndWait {
                println("inside invokeAndWait")
//            WriteAction.run<Throwable> {
                try {
                println("inside WriteAction.run")
                workspaceModel.update("Configure Wippy modules") { builder ->
                    println("inside builder.addEntity")
                    for (entity in moduleEntities) {
                        builder.addEntity(entity)
                        thisLogger().info("Created Wippy module: ${entity.name}")
                    }

                }
                } catch (t: Throwable) {
                    println("Error configuring Wippy modules: ${t.message}")
                }


            }
        }
//            }

        thisLogger().info("Configured ${wippyProjects.size} Wippy project(s)")
    }
}