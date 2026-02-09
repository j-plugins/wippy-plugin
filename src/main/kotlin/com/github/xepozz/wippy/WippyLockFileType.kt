package com.github.xepozz.wippy

import com.intellij.openapi.fileTypes.LanguageFileType
import org.jetbrains.yaml.YAMLLanguage
import java.io.Serializable

class WippyLockFileType private constructor() : LanguageFileType(YAMLLanguage.INSTANCE), Serializable {
    override fun getName() = "Wippy Lock File"
    override fun getDescription() = "Wippy Lock file"
    override fun getDefaultExtension() = "lock"
    override fun getIcon() = WippyIcons.WIPPY
    override fun getDisplayName() = "Wippy Lock File"

    companion object {
        @JvmStatic
        val INSTANCE = WippyLockFileType()
    }
}