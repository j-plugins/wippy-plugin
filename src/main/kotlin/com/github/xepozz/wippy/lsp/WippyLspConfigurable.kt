package com.github.xepozz.wippy.lsp

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class WippyLspConfigurable : Configurable {
    
    private var mainPanel: JPanel? = null
    private val enabledCheckbox = JBCheckBox("Enable Wippy LSP")
    private val portField = JBTextField()
    private val hostField = JBTextField()
    private val autoConnectCheckbox = JBCheckBox("Auto-connect on file open")
    
    override fun getDisplayName() = "Wippy LSP"
    
    override fun createComponent(): JComponent {
        mainPanel = FormBuilder.createFormBuilder()
            .addComponent(enabledCheckbox)
            .addLabeledComponent("Host:", hostField)
            .addLabeledComponent("Port:", portField)
            .addComponent(autoConnectCheckbox)
            .addComponentFillVertically(JPanel(), 0)
            .panel
        return mainPanel!!
    }
    
    override fun isModified(): Boolean {
        val settings = WippyLspSettings.getInstance().state
        return enabledCheckbox.isSelected != settings.enabled ||
               portField.text != settings.port.toString() ||
               hostField.text != settings.host ||
               autoConnectCheckbox.isSelected != settings.autoConnect
    }
    
    override fun apply() {
        val settings = WippyLspSettings.getInstance()
        settings.state.enabled = enabledCheckbox.isSelected
        settings.state.port = portField.text.toIntOrNull() ?: 7777
        settings.state.host = hostField.text
        settings.state.autoConnect = autoConnectCheckbox.isSelected
    }
    
    override fun reset() {
        val settings = WippyLspSettings.getInstance().state
        enabledCheckbox.isSelected = settings.enabled
        portField.text = settings.port.toString()
        hostField.text = settings.host
        autoConnectCheckbox.isSelected = settings.autoConnect
    }
}