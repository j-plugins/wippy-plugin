package com.github.xepozz.wippy.lang

import com.intellij.lang.Commenter

class WippyLuaCommenter : Commenter {
    override fun getLineCommentPrefix(): String = "--"

    override fun getBlockCommentPrefix(): String = "--[["

    override fun getBlockCommentSuffix(): String = "]]"

    override fun getCommentedBlockCommentPrefix(): String? = null

    override fun getCommentedBlockCommentSuffix(): String? = null
}