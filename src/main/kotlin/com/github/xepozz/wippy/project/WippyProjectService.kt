package com.github.xepozz.wippy.project

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project

/**
 * Holds all detected Wippy projects within the IntelliJ project.
 * Other plugin components use this to check if a file belongs to a Wippy project.
 */
@Service(Service.Level.PROJECT)
class WippyProjectService(private val project: Project) {

    companion object {
        private val LOG = logger<WippyProjectService>()

        fun getInstance(project: Project): WippyProjectService = project.service()
    }

    @Volatile
    var wippyProjects: List<WippyProject> = emptyList()
        private set

    /**
     * Re-scans the project for wippy.lock files and updates the model.
     * Returns the number of detected Wippy projects.
     */
    fun rescan(): Int {
        val detector = WippyProjectDetector(project)
        wippyProjects = detector.detectAll()
        LOG.info("Detected ${wippyProjects.size} Wippy project(s): ${wippyProjects.map { it.name }}")
        return wippyProjects.size
    }

    /**
     * Finds which Wippy project (if any) a given file path belongs to.
     */
    fun findProjectForFile(filePath: String): WippyProject? {
        return wippyProjects.firstOrNull { filePath.startsWith(it.root.path) }
    }
}