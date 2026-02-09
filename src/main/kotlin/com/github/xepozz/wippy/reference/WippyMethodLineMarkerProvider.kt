package com.github.xepozz.wippy.reference

import com.github.xepozz.wippy.util.isInWippyDefinitionFile
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.util.PsiNavigateUtil
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar

class WippyMethodLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val yamlScalar = element.parent as? YAMLScalar ?: return null
        if (!yamlScalar.isInWippyDefinitionFile()) return null

        val keyValue = yamlScalar.parent as? YAMLKeyValue ?: return null
        if (keyValue.keyText != "method") return null

        val methodName = yamlScalar.textValue
        if (methodName.isBlank()) return null

        val refs = yamlScalar.references
        val target = refs.firstOrNull { it is WippyMethodReference }?.resolve() ?: return null

        return LineMarkerInfo(
            element,
            element.textRange,
            AllIcons.Nodes.Function,
            { "Go to function '$methodName'" },
            { _, _ -> PsiNavigateUtil.navigate(target, true) },
            GutterIconRenderer.Alignment.LEFT,
            { "Navigate to Lua function" }
        )
    }
}