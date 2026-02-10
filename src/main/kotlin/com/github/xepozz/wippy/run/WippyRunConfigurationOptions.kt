package com.github.xepozz.wippy.run

import com.intellij.execution.configurations.RunConfigurationOptions

class WippyRunConfigurationOptions : RunConfigurationOptions() {
    var scriptPath by string("")
    var functionName by string("")
}
