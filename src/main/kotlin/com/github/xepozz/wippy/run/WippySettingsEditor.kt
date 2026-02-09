package com.github.xepozz.wippy.run

import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

class WippySettingsEditor : SettingsEditor<WippyRunConfiguration>() {
    private val myPanel: JPanel
    private val scriptPathField = TextFieldWithBrowseButton()
    private val functionNameField = JTextField()

    init {
        myPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Script path", scriptPathField)
            .addLabeledComponent("Function name", functionNameField)
            .panel
    }

    override fun resetEditorFrom(demoRunConfiguration: WippyRunConfiguration) {
        scriptPathField.text = demoRunConfiguration.scriptPath ?: ""
        functionNameField.text = demoRunConfiguration.functionName ?: ""
    }

    override fun applyEditorTo(demoRunConfiguration: WippyRunConfiguration) {
        demoRunConfiguration.scriptPath = scriptPathField.text
        demoRunConfiguration.functionName = functionNameField.text
    }

    override fun createEditor(): JComponent = myPanel
}
