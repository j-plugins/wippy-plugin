package com.github.xepozz.wippy.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project

class WippyRunConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        WippyRunConfiguration(project, this, "Wippy")

    override fun getId(): String = WippyRunConfigurationType.ID
    
    override fun getOptionsClass() = WippyRunConfigurationOptions::class.java
}
