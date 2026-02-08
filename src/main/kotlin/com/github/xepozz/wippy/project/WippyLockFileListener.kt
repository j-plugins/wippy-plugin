package com.github.xepozz.wippy.project

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent

/**
 * Watches for wippy.lock file creation, deletion, or movement.
 * Triggers project re-scan when detected.
 */
class WippyLockFileListener : AsyncFileListener {

    companion object {
        private val LOG = logger<WippyLockFileListener>()
    }

    override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier? {
        val relevant = events.any { isWippyLockEvent(it) }
        if (!relevant) return null

        return object : AsyncFileListener.ChangeApplier {
            override fun afterVfsChange() {
                LOG.info("wippy.lock changed, triggering rescan")
                for (project in ProjectManager.getInstance().openProjects) {
                    if (project.isDisposed) continue
                    val service = WippyProjectService.getInstance(project)
                    service.rescan()
                    // Source roots will be reconfigured on next indexing pass
                    // or user can trigger manually via Wippy: Rescan Projects
                }
            }
        }
    }

    private fun isWippyLockEvent(event: VFileEvent): Boolean {
        val name = when (event) {
            is VFileCreateEvent -> event.childName
            is VFileDeleteEvent -> event.file.name
            is VFileMoveEvent -> event.file.name
            else -> event.file?.name
        }
        return name == WippyProjectDetector.LOCK_FILENAME
    }
}