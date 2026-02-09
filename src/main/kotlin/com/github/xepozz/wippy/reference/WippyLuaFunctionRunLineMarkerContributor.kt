package com.github.xepozz.wippy.reference

import com.github.xepozz.wippy.lang.psi.WippyLuaLocalFuncDefStat
import com.github.xepozz.wippy.lang.psi.WippyLuaTypes
import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

class WippyLuaFunctionRunLineMarkerContributor : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        if (element.elementType != WippyLuaTypes.IDENT) return null
        val parent = element.parent as? WippyLuaLocalFuncDefStat ?: return null
        if (parent.ident != element) return null

        val actions = ExecutorAction.getActions(0)
        return Info(
            AllIcons.Nodes.Function,
            actions,
            { "Run Wippy Function" }
        )
    }
}
