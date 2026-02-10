// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.xepozz.wippy.run

import com.github.xepozz.wippy.MyBundle
import com.github.xepozz.wippy.WippyIcons
import com.github.xepozz.wippy.tool.WippyToolConfigurable
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.target.LanguageRuntimeType
import com.intellij.execution.target.TargetEnvironmentConfiguration
import com.intellij.execution.target.TargetEnvironmentType
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import java.util.function.Supplier

class WippyRuntimeType : LanguageRuntimeType<WippyRuntimeTargetConfiguration>(TYPE_ID) {
  override val icon = WippyIcons.WIPPY

  override val displayName = "Gradle"

  override val configurableDescription = MyBundle.message("gradle.target.configure.label")

  override val launchDescription = MyBundle.message("gradle.target.run.label")

  override fun isApplicableTo(runConfig: RunnerAndConfigurationSettings) = true

  override fun createDefaultConfig() = WippyRuntimeTargetConfiguration()

  override fun createSerializer(config: WippyRuntimeTargetConfiguration): PersistentStateComponent<*> = config

  override fun createConfigurable(project: Project,
                                  config: WippyRuntimeTargetConfiguration,
                                  targetEnvironmentType: TargetEnvironmentType<*>,
                                  targetSupplier: Supplier<TargetEnvironmentConfiguration>): Configurable {
//    return GradleRuntimeTargetUI(config, targetEnvironmentType, targetSupplier, project)
    return WippyToolConfigurable()
  }

  override fun findLanguageRuntime(target: TargetEnvironmentConfiguration): WippyRuntimeTargetConfiguration? {
    return target.runtimes.findByType()
  }

  override fun duplicateConfig(config: WippyRuntimeTargetConfiguration): WippyRuntimeTargetConfiguration =
    duplicatePersistentComponent(this, config)

  companion object {
    @JvmStatic
    val TYPE_ID = "GradleRuntime"

    @JvmStatic
    val PROJECT_FOLDER_VOLUME = VolumeDescriptor(WippyRuntimeType::class.qualifiedName + ":projectFolder",
        MyBundle.message("gradle.target.execution.project.folder.label"),
        MyBundle.message("gradle.target.execution.project.folder.description"),
        MyBundle.message("gradle.target.execution.project.folder.title"),
                                                 "")
  }
}