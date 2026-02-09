package com.github.xepozz.wippy.run

import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.components.StoredProperty

class WippyRunConfigurationOptions : RunConfigurationOptions() {
    private val scriptPathProperty: StoredProperty<String?> = string("").provideDelegate(this, "scriptPath")
    private val functionNameProperty: StoredProperty<String?> = string("").provideDelegate(this, "functionName")

    var scriptPath: String?
        get() = scriptPathProperty.getValue(this)
        set(value) = scriptPathProperty.setValue(this, value)

    var functionName: String?
        get() = functionNameProperty.getValue(this)
        set(value) = functionNameProperty.setValue(this, value)
}
