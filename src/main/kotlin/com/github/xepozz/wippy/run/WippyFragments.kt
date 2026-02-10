package com.github.xepozz.wippy.run

import com.github.xepozz.wippy.index.WippyEntryIndex
import com.github.xepozz.wippy.tool.WippyTool
import com.github.xepozz.wippy.tool.WippyToolConfigurable
import com.github.xepozz.wippy.tool.WippyToolSettings
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.EditorTextField
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.ActionLink
import com.intellij.ui.components.fields.ExpandableTextField
import java.awt.BorderLayout
import javax.swing.DefaultComboBoxModel
import javax.swing.JPanel


object WippyFragments {
    private const val GROUP_COMMAND = "command"
    private const val GROUP_ADVANCED = "Advanced"

    fun toolFragment(project: Project): SettingsEditorFragment<WippyRunConfiguration, *> {
        val comboModel = DefaultComboBoxModel<WippyTool>()
        val combo = ComboBox(comboModel).apply {
            renderer = SimpleListCellRenderer.create("No tools configured") { it.displayName }
        }
        val configureLink = ActionLink("Configure...") {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, WippyToolConfigurable::class.java)
            refreshTools(comboModel)
        }
        val panel = JPanel(BorderLayout(8, 0)).apply {
            add(combo, BorderLayout.CENTER)
            add(configureLink, BorderLayout.EAST)
        }
        refreshTools(comboModel)

        return SettingsEditorFragment<WippyRunConfiguration, LabeledComponent<JPanel>>(
            "wippy.tool",
            "Wippy Tool",
            GROUP_COMMAND,
            LabeledComponent.create(panel, "Wippy tool:",BorderLayout.WEST),
            { config, _ ->
                refreshTools(comboModel)
                val tool = (0 until comboModel.size)
                    .map { comboModel.getElementAt(it) }
                    .find { it.id == config.options.toolId }
                combo.selectedItem = tool
            },
            { config, _ ->
                config.options.toolId = (combo.selectedItem as? WippyTool)?.id ?: ""
            },
            { true }
        ).apply { isRemovable = false }
    }

    fun namespaceFragment(
        project: Project,
        onNamespaceChanged: (String) -> Unit,
    ): SettingsEditorFragment<WippyRunConfiguration, *> {
//        val comboModel = DefaultComboBoxModel<String>()
//        val combo = ComboBox(comboModel).apply {
//            isEditable = true
//            addActionListener { onNamespaceChanged(selectedItem as? String ?: "") }
//        }
        val textField = EditorTextField()
//        val fragment = SettingsEditorFragment.createTag<WippyRunConfiguration>(
//            "test.use.module.path",
//            ExecutionBundle.message("do.not.use.module.path.tag"),
//            ExecutionBundle.message("group.java.options"),
//             { configuration -> configuration.options.namespace },
//             { configuration, value ->
//                configuration.options.namespace = value as? String ?: ""
//
//            })

        val fragment = SettingsEditorFragment<WippyRunConfiguration, LabeledComponent<*>>(
            "wippy.namespace",
            "Namespace",
            GROUP_COMMAND,
            LabeledComponent.create(textField, "Namespace:", BorderLayout.WEST),
            { config, _ -> textField.text = config.options.namespace ?:"" },
            { config, _ -> config.options.namespace = textField.text },
            { true }
        ).apply { isRemovable = false }


//        ReadAction
//            .nonBlocking(Callable {
//                WippyEntryIndex.getNamespaces(project)
////                        .forEach { comboModel.addElement(it) }
//                    .map { it }
//            })
//            .finishOnUiThread(ModalityState.current(), {
//                comboModel.addAll(it)
//            })
//            .expireWith(fragment)
//            .submit(NonUrgentExecutor.getInstance())

        return fragment
    }

    fun commandFragment(
        project: Project,
    ): Pair<SettingsEditorFragment<WippyRunConfiguration, *>, (String) -> Unit> {
        val comboModel = DefaultComboBoxModel<String>()
        val combo = ComboBox(comboModel).apply { isEditable = true }

        val refreshCommands = { namespace: String ->
            comboModel.removeAllElements()
            if (namespace.isNotEmpty()) {
                WippyEntryIndex.getCommands(project, namespace).forEach { entry ->
                    entry.commandName?.let { comboModel.addElement(it) }
                }
            }
        }

        val fragment = SettingsEditorFragment<WippyRunConfiguration, LabeledComponent<ComboBox<String>>>(
            "wippy.command",
            "Command",
            GROUP_COMMAND,
            LabeledComponent.create(combo, "Command:",BorderLayout.WEST),
            { config, _ ->
                val ns = config.options.namespace ?: ""
                refreshCommands(ns)
                combo.selectedItem = config.options.command
            },
            { config, _ ->
                config.options.command = combo.selectedItem as? String ?: ""
            },
            { true }
        ).apply { isRemovable = false }

        return fragment to refreshCommands
    }

    fun configFileFragment(project: Project): SettingsEditorFragment<WippyRunConfiguration, *> {
        val field = TextFieldWithBrowseButton()
        field.addBrowseFolderListener(
            "Select Wippy Config",
            "Select .wippy.yaml configuration file",
            project,
            FileChooserDescriptorFactory.createSingleFileDescriptor("yaml")
        )

        return SettingsEditorFragment<WippyRunConfiguration, LabeledComponent<TextFieldWithBrowseButton>>(
            "wippy.configFile",
            "Config file",
            GROUP_ADVANCED,
            LabeledComponent.create(field, "Config file:", BorderLayout.WEST),
            { config, _ -> field.text = config.options.configFile ?: "" },
            { config, _ -> config.options.configFile = field.text },
            { true }
        )
    }

    fun additionalArgsFragment(): SettingsEditorFragment<WippyRunConfiguration, *> {
        val field = ExpandableTextField()

        return SettingsEditorFragment<WippyRunConfiguration, LabeledComponent<ExpandableTextField>>(
            "wippy.additionalArgs",
            "Additional Arguments",
            GROUP_ADVANCED,
            LabeledComponent.create(field, "Arguments:",BorderLayout.WEST),
            { config, _ -> field.text = config.options.additionalArgs },
            { config, _ -> config.options.additionalArgs = field.text },
            { true }
        )
    }

    private fun refreshTools(model: DefaultComboBoxModel<WippyTool>) {
        val selected = model.selectedItem as? WippyTool
        model.removeAllElements()
        WippyToolSettings.getInstance().tools.forEach { model.addElement(it) }
        if (selected != null) {
            val restored = (0 until model.size).map { model.getElementAt(it) }.find { it.id == selected.id }
            model.selectedItem = restored
        }
    }
}