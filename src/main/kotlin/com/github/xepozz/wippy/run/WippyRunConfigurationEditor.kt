package com.github.xepozz.wippy.run

import com.intellij.diagnostic.logging.LogsGroupFragment
import com.intellij.execution.ui.BeforeRunComponent
import com.intellij.execution.ui.BeforeRunFragment
import com.intellij.execution.ui.CommonParameterFragments
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

//        return SettingsEditorFragmentContainer.fragments {
//            add(CommonParameterFragments.createRunHeader())
//            addAll(BeforeRunFragment.createGroup())
//            add(CommonTags.parallelRun())
//            addEnvironmentFragment(
//                object : LabeledSettingsFragmentInfo {
//                    override val editorLabel: String = ExecutionBundle.message("environment.variables.component.title")
//                    override val settingsId: String = "external.system.environment.variables.fragment"
//                    override val settingsName: String = ExecutionBundle.message("environment.variables.fragment.name")
//                    override val settingsGroup: String = ExecutionBundle.message("group.operating.system")
//                    override val settingsHint: String = ExecutionBundle.message("environment.variables.fragment.hint")
//                    override val settingsActionHint: String = ExecutionBundle.message("set.custom.environment.variables.for.the.process")
//                },
//                { settings.env },
//                { settings.env = it },
//                { settings.isPassParentEnvs },
//                { settings.isPassParentEnvs = it },
//                hideWhenEmpty = false,
//            )
//            addVmOptionsFragment()
//            add(LogsGroupFragment())
//        }
        return listOf(
            BeforeRunFragment.createBeforeRun(BeforeRunComponent(this), BUILD_TASK_KEY),
            CommonParameterFragments.createRunHeader() ,
            *BeforeRunFragment.createGroup<WippyRunConfiguration>().toTypedArray(),
            TargetPathFragment(),
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