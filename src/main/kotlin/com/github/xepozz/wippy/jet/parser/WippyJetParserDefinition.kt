package com.github.xepozz.wippy.jet.parser

import com.github.xepozz.wippy.jet.WippyJetLanguage
import com.github.xepozz.wippy.jet.file.WippyJetFile
import com.github.xepozz.wippy.jet.lexer.WippyJetLexerAdapter
import com.github.xepozz.wippy.jet.psi.WippyJetTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

class WippyJetParserDefinition : ParserDefinition {
    companion object {
        val FILE = IFileElementType(WippyJetLanguage.INSTANCE)
        val COMMENTS = TokenSet.create(WippyJetTypes.JET_COMMENT)
        val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
        val STRINGS = TokenSet.create(WippyJetTypes.STRING, WippyJetTypes.RAW_STRING)
    }

    override fun createLexer(project: Project): Lexer = WippyJetLexerAdapter()
    override fun createParser(project: Project): PsiParser = WippyJetParser()
    override fun getFileNodeType() = FILE
    override fun getCommentTokens() = COMMENTS
    override fun getWhitespaceTokens() = WHITE_SPACES
    override fun getStringLiteralElements() = STRINGS
    override fun createElement(node: ASTNode): PsiElement = WippyJetTypes.Factory.createElement(node)
    override fun createFile(viewProvider: FileViewProvider) = WippyJetFile(viewProvider)
}
