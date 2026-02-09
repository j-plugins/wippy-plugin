package com.github.xepozz.wippy.run

import com.github.xepozz.wippy.WippyIcons
import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.execution.configurations.ConfigurationTypeUtil

class WippyRunConfigurationType : ConfigurationTypeBase(
    ID, "Wippy", "Wippy run configuration", WippyIcons.WIPPY
) {
    init {
        addFactory(WippyRunConfigurationFactory(this))
    }

    companion object {
        const val ID = "WippyRunConfiguration"
        
        fun getInstance(): WippyRunConfigurationType =
            ConfigurationTypeUtil.findConfigurationType(WippyRunConfigurationType::class.java)
    }
}
