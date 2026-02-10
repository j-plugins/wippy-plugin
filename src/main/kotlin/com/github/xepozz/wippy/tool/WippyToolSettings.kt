package com.github.xepozz.wippy.tool

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.annotations.XCollection
import java.util.*

@Service(Service.Level.APP)
@State(name = "WippyToolSettings", storages = [Storage("wippy-tools.xml")])
class WippyToolSettings : PersistentStateComponent<WippyToolSettings.State> {
    private var myState = State()

    class State {
        @XCollection(elementTypes = [WippyTool::class])
        var tools: MutableList<WippyTool> = mutableListOf()
    }

    override fun getState() = myState

    override fun loadState(state: State) {
        myState = state
    }

    val tools: List<WippyTool> get() = myState.tools

    fun addTool(tool: WippyTool): WippyTool {
        if (tool.id.isEmpty()) {
            tool.id = UUID.randomUUID().toString()
        }
        myState.tools.add(tool)
        return tool
    }

    fun removeTool(id: String) {
        myState.tools.removeAll { it.id == id }
    }

    fun updateTool(tool: WippyTool) {
        val idx = myState.tools.indexOfFirst { it.id == tool.id }
        if (idx >= 0) myState.tools[idx] = tool
    }

    fun findById(id: String): WippyTool? = myState.tools.find { it.id == id }

    companion object {
        fun getInstance(): WippyToolSettings = service()
    }
}