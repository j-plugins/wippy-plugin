package com.github.xepozz.wippy.run

import com.github.xepozz.wippy.tool.WippyTool
import com.github.xepozz.wippy.tool.WippyToolSettings
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.SimpleListCellRenderer
import java.awt.BorderLayout
import javax.swing.DefaultComboBoxModel
import javax.swing.JPanel

class WippyToolFragment(private val project: Project) : SettingsEditorFragment<WippyRunConfiguration, LabeledComponent<JPanel>>(
    "wippy.tool",
    "Wippy Tool",
    COMMAND_GROUP,
    createComponent(),
    { config, component -> getInstance(component).reset(config) },
    { config, component -> getInstance(component).apply(config) },
    { true }
) {
    companion object {
        const val COMMAND_GROUP = "command"

        private fun createComponent(): LabeledComponent<JPanel> {
            val comboModel = DefaultComboBoxModel<WippyTool>()
            val combo = ComboBox(comboModel).apply {
                renderer = SimpleListCellRenderer.create("No tools configured") { it.displayName }
            }
            val panel = JPanel(BorderLayout(8, 0)).apply {
                add(combo, BorderLayout.CENTER)
            }
            return LabeledComponent.create(panel, "Wippy tool:")
        }

        private fun getInstance(component: LabeledComponent<JPanel>): WippyToolFragmentHelper {
            return WippyToolFragmentHelper(component)
        }
    }

    init {
        isRemovable = false
    }

    private class WippyToolFragmentHelper(private val labeled: LabeledComponent<JPanel>) {
        private val panel get() = labeled.component
        private val combo: ComboBox<*> get() = panel.getComponent(0) as ComboBox<*>

        @Suppress("UNCHECKED_CAST")
        private val comboModel get() = combo.model as DefaultComboBoxModel<WippyTool>

        fun reset(config: WippyRunConfiguration) {
            val selected = comboModel.selectedItem as? WippyTool
            comboModel.removeAllElements()
            WippyToolSettings.getInstance().tools.forEach { comboModel.addElement(it) }
            val toolId = config.options.toolId
            val restored = (0 until comboModel.size)
                .map { comboModel.getElementAt(it) }
                .find { it.id == toolId }
            comboModel.selectedItem = restored ?: selected
        }

        fun apply(config: WippyRunConfiguration) {
            config.options.toolId = (combo.selectedItem as? WippyTool)?.id ?: ""
        }
    }
}