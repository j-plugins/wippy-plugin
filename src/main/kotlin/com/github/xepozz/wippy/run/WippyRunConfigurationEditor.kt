package com.github.xepozz.wippy.run

import com.intellij.diagnostic.logging.LogsGroupFragment
import com.intellij.execution.ui.BeforeRunComponent
import com.intellij.execution.ui.BeforeRunFragment
import com.intellij.execution.ui.CommonTags
import com.intellij.execution.ui.RunConfigurationFragmentedEditor
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.execution.ui.TargetPathFragment
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.util.Key

class WippyRunConfigurationEditor(
    private val project: Project,
    runConfiguration: WippyRunConfiguration,
) : RunConfigurationFragmentedEditor<WippyRunConfiguration>(runConfiguration) {
    companion object {
        val BUILD_TASK_KEY = Key.create<String>("wippy.build.task")
    }

    override fun createRunFragments(): List<SettingsEditorFragment<WippyRunConfiguration, *>> {
        val (commandFragment, refreshCommands) = WippyFragments.commandFragment(project)

        val commonParameterFragments = com.intellij.execution.ui.CommonParameterFragments<WippyRunConfiguration>(project, { project.modules.firstOrNull() })

        return listOf(
            TargetPathFragment(),
            BeforeRunFragment.createBeforeRun(BeforeRunComponent(this), BUILD_TASK_KEY),
            *BeforeRunFragment.createGroup<WippyRunConfiguration>().toTypedArray(),
            CommonTags.parallelRun(),
            *commonParameterFragments.fragments.toTypedArray(),
            WippyFragments.toolFragment(project),
            WippyFragments.namespaceFragment(project, onNamespaceChanged = refreshCommands),
            commandFragment,
            WippyFragments.configFileFragment(project),
            WippyFragments.additionalArgsFragment(),
            LogsGroupFragment(),
        )
    }
}