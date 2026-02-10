package com.github.xepozz.wippy.run

import com.github.xepozz.wippy.index.WippyEntryIndex
import com.github.xepozz.wippy.lang.psi.WippyLuaLocalFuncDefStat
import com.github.xepozz.wippy.lang.psi.WippyLuaTypes
import com.github.xepozz.wippy.tool.WippyToolSettings
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class WippyRunConfigurationProducer : LazyRunConfigurationProducer<WippyRunConfiguration>() {
    override fun getConfigurationFactory(): ConfigurationFactory =
        WippyRunConfigurationType.getInstance().configurationFactories[0]

    override fun setupConfigurationFromContext(
        configuration: WippyRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val location = context.location ?: return false
        val element = location.psiElement
        val functionDef = PsiTreeUtil.getParentOfType(element, WippyLuaLocalFuncDefStat::class.java, false) ?: return false
        val functionName = functionDef.node.findChildByType(WippyLuaTypes.IDENT)?.text ?: return false
        val virtualFile = functionDef.containingFile.virtualFile ?: return false

        val project = context.project
        val match = findCommandForFile(project, virtualFile.name, functionName) ?: return false

        configuration.options.namespace = match.first
        configuration.options.command = match.second

        val firstTool = WippyToolSettings.getInstance().tools.firstOrNull()
        if (firstTool != null) {
            configuration.options.toolId = firstTool.id
        }

        configuration.name = "Wippy: ${match.first}:${match.second}"
        sourceElement.set(functionDef)
        return true
    }

    override fun isConfigurationFromContext(
        configuration: WippyRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val location = context.location ?: return false
        val element = location.psiElement
        val functionDef = PsiTreeUtil.getParentOfType(element, WippyLuaLocalFuncDefStat::class.java, false) ?: return false
        val functionName = functionDef.node.findChildByType(WippyLuaTypes.IDENT)?.text ?: return false
        val virtualFile = functionDef.containingFile.virtualFile ?: return false

        val match = findCommandForFile(context.project, virtualFile.name, functionName) ?: return false

        return configuration.options.namespace == match.first
                && configuration.options.command == match.second
    }

    private fun findCommandForFile(
        project: com.intellij.openapi.project.Project,
        fileName: String,
        methodName: String,
    ): Pair<String, String>? {
        val sourceRef = "file://$fileName"
        for (ns in WippyEntryIndex.getNamespaces(project)) {
            for (entry in WippyEntryIndex.getEntries(project, ns)) {
                if (entry.sourceFile == sourceRef && entry.method == methodName && entry.commandName != null) {
                    return ns to entry.commandName
                }
            }
        }
        return null
    }
}