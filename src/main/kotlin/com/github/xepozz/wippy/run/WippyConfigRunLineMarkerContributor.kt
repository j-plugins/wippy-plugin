package com.github.xepozz.wippy.run

import com.github.xepozz.wippy.index.WippyConfigUtil
import com.github.xepozz.wippy.tool.WippyToolSettings
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.jetbrains.yaml.YAMLTokenTypes
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue

class WippyConfigRunLineMarkerContributor : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        if (element.elementType != YAMLTokenTypes.SCALAR_KEY) return null
        val keyValue = element.parent as? YAMLKeyValue ?: return null
        if (keyValue.keyText != "lsp") return null
        val psiFile = keyValue.containingFile as? YAMLFile ?: return null
        if (!WippyConfigUtil.isWippyLspConfigFile(psiFile)) return null

        val runAction = RunWippyConfigAction(psiFile)
        return Info(
            AllIcons.Actions.Execute,
            arrayOf(runAction),
            { "Run Wippy (LSP)" }
        )
    }

    private class RunWippyConfigAction(private val config: YAMLFile) : AnAction(), DumbAware {
        override fun actionPerformed(e: AnActionEvent) {
            val project = e.project ?: return

            val configurationFactory = WippyRunConfigurationType.getInstance().configurationFactories.first()
            val runManager = RunManager.getInstance(project)
            val settings = runManager.createConfiguration("Wippy: LSP", configurationFactory)
            val configuration = settings.configuration as WippyRunConfiguration

            val firstTool = WippyToolSettings.getInstance().tools.firstOrNull()
            if (firstTool != null) {
                configuration.options.toolId = firstTool.id
            }

            configuration.options.workingDirectory = config.virtualFile.parent?.path
            configuration.options.configFile = config.virtualFile.path
            configuration.options.namespace = ""
            configuration.options.command = ""

            ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance())
        }

        override fun update(e: AnActionEvent) {
            e.presentation.icon = AllIcons.Actions.Execute
            e.presentation.text = "Run Wippy (LSP)"
        }
    }
}
