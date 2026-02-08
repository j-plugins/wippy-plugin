package com.github.xepozz.wippy.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.github.xepozz.wippy.MyBundle
import com.github.xepozz.wippy.services.MyProjectService
import javax.swing.JButton

class MyToolWindow(toolWindow: ToolWindow) {

    private val service = toolWindow.project.service<MyProjectService>()

    fun getContent() = JBPanel<JBPanel<*>>().apply {
        val label = JBLabel(MyBundle.message("randomLabel", "?"))

        add(label)
        add(JButton(MyBundle.message("shuffle")).apply {
            addActionListener {
                label.text = MyBundle.message("randomLabel", service.getRandomNumber())
            }
        })
    }
}
