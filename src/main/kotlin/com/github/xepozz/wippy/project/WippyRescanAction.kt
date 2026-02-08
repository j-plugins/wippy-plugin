package com.github.xepozz.wippy.project

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.roots.ModuleRootManager
import kotlinx.coroutines.runBlocking

/**
 * Action: "Wippy: Rescan Projects"
 * Re-detects wippy.lock files and reconfigures source roots.
 * Available via Tools menu and Find Action (Cmd+Shift+A).
 */
class WippyRescanAction : AnAction(
    "Wippy: Rescan Projects",
    "Detect Wippy projects and configure source roots",
    null
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val service = WippyProjectService.getInstance(project)
        val count = service.rescan()

        // Re-run the configurator
        runBlocking {
            WippySourceRootConfigurator().execute(project)
        }

        NotificationGroupManager.getInstance()
            .getNotificationGroup("Wippy")
            .createNotification(
                "Wippy",
                "Detected $count Wippy project(s)",
                if (count > 0) NotificationType.INFORMATION else NotificationType.WARNING
            )
            .notify(project)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}