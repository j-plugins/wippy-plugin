package com.github.xepozz.wippy.project.module

import com.github.xepozz.wippy.WippyIcons
import com.intellij.openapi.module.ModuleType
import org.jetbrains.annotations.Nls
import javax.swing.Icon

class WippyModuleType : ModuleType<WippyModuleBuilder>("Wippy") {
    override fun createModuleBuilder(): WippyModuleBuilder {
        return WippyModuleBuilder()
    }

    override fun getName(): @Nls(capitalization = Nls.Capitalization.Title) String {
        return "Wippy"
    }

    override fun getDescription(): @Nls(capitalization = Nls.Capitalization.Sentence) String {
        return "Wippy module type"
    }

    override fun getNodeIcon(isOpened: Boolean): Icon {
        return WippyIcons.WIPPY
    }

    companion object {
        val INSTANCE = WippyModuleType()
    }
}