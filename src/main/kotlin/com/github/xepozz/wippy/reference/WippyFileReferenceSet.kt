package com.github.xepozz.wippy.reference

import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet
import org.jetbrains.yaml.psi.YAMLScalar

class WippyFileReferenceSet(element: YAMLScalar) : FileReferenceSet(
    element.textValue.removePrefix(FILE_PROTOCOL),
    element,
    element.text.indexOf(FILE_PROTOCOL) + FILE_PROTOCOL.length,
    null,
    true,
    true
) {
    companion object {
        const val FILE_PROTOCOL = "file://"
    }

    override fun isCaseSensitive(): Boolean = false
}
