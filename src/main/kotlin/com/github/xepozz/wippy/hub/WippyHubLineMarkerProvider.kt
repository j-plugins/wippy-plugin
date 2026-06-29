package com.github.xepozz.wippy.hub

import com.github.xepozz.wippy.MyBundle
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.YAMLScalar

class WippyHubLineMarkerProvider : LineMarkerProvider {

    private val LOG = logger<WippyHubLineMarkerProvider>()

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val scalar = element.parent as? YAMLScalar ?: return null

        val dependency = WippyHubUrl.resolveDependency(scalar) ?: return null
        val url = WippyHubUrl.buildHubUrl(dependency.component, dependency.version) ?: return null

        LOG.debug("[WippyHubLineMarkerProvider] marker for ${dependency.component} -> $url")

        return LineMarkerInfo(
            element,
            element.textRange,
            AllIcons.Ide.External_link_arrow,
            { MyBundle.message("hub.lineMarker.tooltip", dependency.component) },
            { _, _ ->
                LOG.info("[WippyHubLineMarkerProvider] opening $url")
                BrowserUtil.browse(url)
            },
            GutterIconRenderer.Alignment.LEFT,
            { MyBundle.message("hub.lineMarker.accessibleName") }
        )
    }
}
