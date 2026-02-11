package com.github.xepozz.wippy.highlighting

import com.github.xepozz.wippy.lang.WippyLuaFile
import com.github.xepozz.wippy.lang.psi.WippyLuaBreakStat
import com.github.xepozz.wippy.lang.psi.WippyLuaFuncBody
import com.github.xepozz.wippy.lang.psi.WippyLuaIfStat
import com.github.xepozz.wippy.lang.psi.WippyLuaRepeatStat
import com.github.xepozz.wippy.lang.psi.WippyLuaReturnStat
import com.github.xepozz.wippy.lang.psi.WippyLuaTypes.BREAK
import com.github.xepozz.wippy.lang.psi.WippyLuaTypes.DO
import com.github.xepozz.wippy.lang.psi.WippyLuaTypes.ELSE
import com.github.xepozz.wippy.lang.psi.WippyLuaTypes.ELSEIF
import com.github.xepozz.wippy.lang.psi.WippyLuaTypes.END
import com.github.xepozz.wippy.lang.psi.WippyLuaTypes.FUNCTION
import com.github.xepozz.wippy.lang.psi.WippyLuaTypes.IF
import com.github.xepozz.wippy.lang.psi.WippyLuaTypes.REPEAT
import com.github.xepozz.wippy.lang.psi.WippyLuaTypes.RETURN
import com.github.xepozz.wippy.lang.psi.WippyLuaTypes.THEN
import com.github.xepozz.wippy.lang.psi.WippyLuaTypes.UNTIL
import com.github.xepozz.wippy.lang.psi.WippyLuaTypes.WHILE
import com.github.xepozz.wippy.lang.psi.WippyLuaWhileStat
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.util.Consumer

class WippyLuaHighlightUsagesHandlerFactory : HighlightUsagesHandlerFactory {
    override fun createHighlightUsagesHandler(editor: Editor, file: PsiFile): HighlightUsagesHandlerBase<*>? {
        val offset = editor.caretModel.offset
        val element = file.findElementAt(offset) ?: return null

        return when (element.elementType) {
            RETURN, BREAK -> WippyLuaExitPointsHandler(editor, file, element)
            IF, THEN, ELSEIF, ELSE -> {
                val ifStat = element.parent as? WippyLuaIfStat
                if (ifStat != null) WippyLuaKeywordHighlighterHandler(editor, file, ifStat, element) else null
            }
            WHILE, DO -> {
                val whileStat = element.parent as? WippyLuaWhileStat
                if (whileStat != null) WippyLuaKeywordHighlighterHandler(editor, file, whileStat, element) else null
            }
            REPEAT, UNTIL -> {
                val repeatStat = element.parent as? WippyLuaRepeatStat
                if (repeatStat != null) WippyLuaKeywordHighlighterHandler(editor, file, repeatStat, element) else null
            }
            END -> {
                val parent = element.parent
                when (parent) {
                    is WippyLuaIfStat -> WippyLuaKeywordHighlighterHandler(editor, file, parent, element)
                    is WippyLuaWhileStat -> WippyLuaKeywordHighlighterHandler(editor, file, parent, element)
                    is WippyLuaRepeatStat -> WippyLuaKeywordHighlighterHandler(editor, file, parent, element)
                    is WippyLuaFuncBody -> WippyLuaKeywordHighlighterHandler(editor, file, parent.parent, element)
                    else -> null
                }
            }
            else -> null
        }
    }
}

class WippyLuaExitPointsHandler(editor: Editor, file: PsiFile, private val target: PsiElement) :
    HighlightUsagesHandlerBase<PsiElement>(editor, file) {

    override fun getTargets() = listOf(target)

    override fun computeUsages(targets: MutableList<out PsiElement>) {
        val root = PsiTreeUtil.getParentOfType(target, WippyLuaFuncBody::class.java, WippyLuaFile::class.java) ?: return
        
        PsiTreeUtil.findChildrenOfAnyType(root, WippyLuaReturnStat::class.java, WippyLuaBreakStat::class.java)
            .forEach {
                val keyword = it.firstChild
                if (keyword != null) {
                    readUsages.add(keyword.textRange)
                }
            }
    }

    override fun selectTargets(targets: MutableList<out PsiElement>, selectionConsumer: Consumer<in MutableList<out PsiElement>>) {
        selectionConsumer.consume(targets)
    }
}

class WippyLuaKeywordHighlighterHandler(
    editor: Editor,
    file: PsiFile,
    private val container: PsiElement,
    private val target: PsiElement,
) :
    HighlightUsagesHandlerBase<PsiElement>(editor, file) {

    override fun getTargets() = listOf(container)

    override fun computeUsages(targets: MutableList<out PsiElement>) {
        val keywords = mutableListOf(target)
        collectKeywords(container, keywords)
        keywords.forEach { readUsages.add(it.textRange) }
    }

    private fun collectKeywords(element: PsiElement, keywords: MutableList<PsiElement>) {
        var child = element.firstChild
        while (child != null) {
            if (isKeyword(child.elementType)) {
                keywords.add(child)
            }
            child = child.nextSibling
        }
    }

    private fun isKeyword(type: com.intellij.psi.tree.IElementType?): Boolean {
        return type in listOf(IF, THEN, ELSEIF, ELSE, END, WHILE, DO, REPEAT, UNTIL, FUNCTION, RETURN, BREAK)
    }

    override fun selectTargets(targets: MutableList<out PsiElement>, selectionConsumer: Consumer<in MutableList<out PsiElement>>) {
        selectionConsumer.consume(targets)
    }
}
