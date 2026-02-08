package com.github.xepozz.wippy.reference

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar

class WippyMethodLineMarkerProvider : LineMarkerProvider {

    companion object {
        private const val INDEX_FILENAME = "_index.yaml"
    }

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val yamlScalar = element as? YAMLScalar ?: return null
        if (element.containingFile?.name != INDEX_FILENAME) return null

        val keyValue = PsiTreeUtil.getParentOfType(yamlScalar, YAMLKeyValue::class.java)
        if (keyValue?.keyText != "method") return null

        val methodName = yamlScalar.textValue
        if (methodName.isBlank()) return null

        val refs = yamlScalar.references
        val target = refs.firstOrNull { it is WippyMethodReference }
            ?.resolve()
            as? NavigatablePsiElement
            ?: return null

        return LineMarkerInfo(
            element,
            element.textRange,
            AllIcons.Nodes.Function,
            { "Go to function '$methodName'" },
            { _, _ -> target.navigate(true) },
            GutterIconRenderer.Alignment.LEFT,
            { "Navigate to Lua function" }
        )
    }
}