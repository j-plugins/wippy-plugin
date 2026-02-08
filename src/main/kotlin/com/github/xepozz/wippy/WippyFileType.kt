package com.github.xepozz.wippy

import com.intellij.openapi.fileTypes.LanguageFileType
import org.jetbrains.yaml.YAMLLanguage

class WippyFileType private constructor() : LanguageFileType(YAMLLanguage.INSTANCE), java.io.Serializable {
    override fun getName() = "Wippy File"

    override fun getDescription() = "Wippy configuration file"

    override fun getDefaultExtension() = "yaml"

    override fun getIcon() = WippyIcons.WIPPY

    companion object {
        @JvmStatic
        val INSTANCE = WippyFileType()
    }
}