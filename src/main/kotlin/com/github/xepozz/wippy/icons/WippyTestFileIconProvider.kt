package com.github.xepozz.wippy.icons

import com.github.xepozz.wippy.WippyIcons
import com.github.xepozz.wippy.index.WippyEntryIndex
import com.github.xepozz.wippy.lang.WippyLuaFileType
import com.intellij.ide.IconProvider
import com.intellij.openapi.diagnostic.logger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import javax.swing.Icon

class WippyTestFileIconProvider : IconProvider() {

    private val LOG = logger<WippyTestFileIconProvider>()

    override fun getIcon(element: PsiElement, flags: Int): Icon? {
        val psiFile = element as? PsiFile ?: return null
        val file = psiFile.virtualFile ?: return null
        if (file.fileType != WippyLuaFileType.INSTANCE) return null
        if (!WippyEntryIndex.isWippyTestFile(psiFile.project, file)) return null

        LOG.debug("[WippyTestFileIconProvider] test icon for ${file.path}")
        return WippyIcons.WIPPY_TEST
    }
}
