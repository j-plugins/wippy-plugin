package com.github.xepozz.wippy.hub

import com.github.xepozz.wippy.util.isInWippyDefinitionFile
import com.intellij.openapi.diagnostic.logger
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar

object WippyHubUrl {

    private val LOG = logger<WippyHubUrl>()

    const val HUB_BASE = "https://hub.wippy.ai"

    private const val DEPENDENCY_KIND = "ns.dependency"
    private const val COMPONENT_KEY = "component"
    private const val KIND_KEY = "kind"
    private const val VERSION_KEY = "version"

    data class HubDependency(val component: String, val version: String?)

    fun buildHubUrl(component: String, version: String?): String? {
        val normalizedComponent = component.trim()
        val segments = normalizedComponent.split("/")
        if (segments.size != 2 || segments.any { it.isBlank() }) {
            LOG.debug("[WippyHubUrl] skip: '$component' is not a valid org/module pair")
            return null
        }

        val normalizedVersion = version?.trim()
        val url = if (!normalizedVersion.isNullOrBlank() && normalizedVersion != "*") {
            "$HUB_BASE/$normalizedComponent?version=$normalizedVersion"
        } else {
            "$HUB_BASE/$normalizedComponent"
        }

        LOG.debug("[WippyHubUrl] resolved (component=$normalizedComponent, version=$normalizedVersion) -> $url")
        return url
    }

    fun resolveDependency(element: PsiElement): HubDependency? {
        val scalar = element as? YAMLScalar ?: element.parent as? YAMLScalar ?: return null
        if (!scalar.isInWippyDefinitionFile()) return null

        val keyValue = scalar.parent as? YAMLKeyValue ?: return null
        if (keyValue.keyText != COMPONENT_KEY) return null

        val entry = PsiTreeUtil.getParentOfType(keyValue, YAMLMapping::class.java) ?: return null
        if (entry.getKeyValueByKey(KIND_KEY)?.valueText != DEPENDENCY_KIND) return null

        val component = entry.getKeyValueByKey(COMPONENT_KEY)?.valueText ?: return null
        val version = entry.getKeyValueByKey(VERSION_KEY)?.valueText

        LOG.debug("[WippyHubUrl] resolved dependency component=$component version=$version")
        return HubDependency(component, version)
    }

    fun urlFor(element: PsiElement): String? =
        resolveDependency(element)?.let { buildHubUrl(it.component, it.version) }
}
