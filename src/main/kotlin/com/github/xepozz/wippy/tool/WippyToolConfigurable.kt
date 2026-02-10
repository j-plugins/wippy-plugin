package com.github.xepozz.wippy.tool

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.dsl.builder.COLUMNS_MEDIUM
import com.intellij.ui.dsl.builder.bindIntText
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import java.util.*
import javax.swing.DefaultListModel
import javax.swing.JComponent
import javax.swing.JPanel

class WippyToolConfigurable : Configurable {
    private val listModel = DefaultListModel<WippyTool>()
    private val toolList = JBList(listModel)
    private var panel: JPanel? = null

    override fun getDisplayName() = "Wippy"

    override fun createComponent(): JComponent {
        loadTools()
        toolList.setCellRenderer { _, value, _, _, _ ->
            com.intellij.ui.SimpleColoredComponent().apply {
                append(value.displayName)
                append("  [${value.type.name}]", com.intellij.ui.SimpleTextAttributes.GRAYED_ATTRIBUTES)
            }
        }

        val decorator = ToolbarDecorator.createDecorator(toolList)
            .setAddAction { addTool() }
            .setEditAction { editTool() }
            .setRemoveAction { removeTool() }
            .disableUpDownActions()

        panel = decorator.createPanel()
        return panel!!
    }

    private fun addTool() {
        val dialog = WippyToolDialog(null)
        if (dialog.showAndGet()) {
            val tool = dialog.result
            tool.id = UUID.randomUUID().toString()
            listModel.addElement(tool)
        }
    }

    private fun editTool() {
        val selected = toolList.selectedValue ?: return
        val dialog = WippyToolDialog(selected)
        if (dialog.showAndGet()) {
            val idx = toolList.selectedIndex
            listModel.set(idx, dialog.result)
        }
    }

    private fun removeTool() {
        val idx = toolList.selectedIndex
        if (idx >= 0) listModel.remove(idx)
    }

    private fun loadTools() {
        listModel.clear()
        WippyToolSettings.getInstance().tools.forEach { listModel.addElement(it.copy()) }
    }

    override fun isModified(): Boolean {
        val current = (0 until listModel.size()).map { listModel.getElementAt(it) }
        return current != WippyToolSettings.getInstance().tools
    }

    override fun apply() {
        val settings = WippyToolSettings.getInstance()
        settings.state.tools.clear()
        for (i in 0 until listModel.size()) {
            settings.state.tools.add(listModel.getElementAt(i))
        }
    }

    override fun reset() = loadTools()
}

class WippyToolDialog(private val existing: WippyTool?) : DialogWrapper(true) {
    private var nameField = ""
    private var type = existing?.type ?: WippyToolType.LOCAL
    private var pathField = existing?.path ?: ""
    private var hostField = existing?.host ?: "127.0.0.1"
    private var portField = existing?.port ?: 7777

    val result: WippyTool
        get() = WippyTool(
            id = existing?.id ?: "",
            name = nameField,
            type = type,
            path = pathField,
            host = hostField,
            port = portField,
        )

    init {
        title = if (existing != null) "Edit Wippy Tool" else "Add Wippy Tool"
        nameField = existing?.name ?: ""
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row("Name:") {
                textField()
                    .columns(COLUMNS_MEDIUM)
                    .bindText(::nameField)
            }
            row("Type:") {
                comboBox(WippyToolType.entries)
                    .bindItem(::type.toNullableProperty())
            }
            row("Path:") {
                textFieldWithBrowseButton("Select Wippy Binary")
                    .columns(COLUMNS_MEDIUM)
                    .bindText(::pathField)
            }.visibleIf(object : com.intellij.openapi.observable.properties.ObservableProperty<Boolean> {
                // simplified - visible for LOCAL
                override fun get() = type == WippyToolType.LOCAL
                override fun afterChange(listener: (Boolean) -> Unit) {}
            })
            row("Host:") {
                textField()
                    .columns(COLUMNS_MEDIUM)
                    .bindText(::hostField)
            }
            row("Port:") {
                intTextField(1..65535)
                    .bindIntText(::portField)
            }
        }
    }
}