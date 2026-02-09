package com.github.xepozz.wippy.run

import com.github.xepozz.wippy.lang.psi.WippyLuaLocalFuncDefStat
import com.github.xepozz.wippy.lang.psi.WippyLuaTypes
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

        configuration.scriptPath = virtualFile.path
        configuration.functionName = functionName
        configuration.name = "Wippy: $functionName"
        
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

        return configuration.scriptPath == virtualFile.path && configuration.functionName == functionName
    }
}
