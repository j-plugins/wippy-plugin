package com.github.xepozz.wippy.project.module

import com.intellij.ide.util.projectWizard.ModuleBuilder

class WippyModuleBuilder: ModuleBuilder() {
    override fun getModuleType() = WippyModuleType.INSTANCE
}