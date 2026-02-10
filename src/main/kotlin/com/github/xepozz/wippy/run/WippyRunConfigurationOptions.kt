package com.github.xepozz.wippy.run

import com.intellij.execution.configurations.ModuleBasedConfigurationOptions

class WippyRunConfigurationOptions : ModuleBasedConfigurationOptions() {
    var toolId by string("")
    var namespace by string("")
    var command by string("")
    var configFile by string("")
    var additionalArgs by string("")
}