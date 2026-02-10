package com.github.xepozz.wippy.project

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.WatchedRootsProvider

class MyWatchedRootsProvider : WatchedRootsProvider {
    override fun getRootsToWatch(project: Project): Set<String> {
        val service = WippyProjectService.getInstance(project)
        ReadAction.run<Throwable> { service.rescan() }

        return service.wippyProjects.map { it.root.path }.toSet()
    }
}