package com.github.xepozz.wippy.lang

import com.github.xepozz.wippy.lang.parser.WippyLuaParser
import com.github.xepozz.wippy.lang.psi.WippyLuaTypes
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

class WippyLuaParserDefinition : ParserDefinition {
    companion object {
        val FILE = IFileElementType(WippyLuaLanguage.INSTANCE)
        val COMMENTS = TokenSet.create(WippyLuaTypes.LINE_COMMENT, WippyLuaTypes.BLOCK_COMMENT)
        val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
        val STRINGS = TokenSet.create(WippyLuaTypes.STRING)
    }

    override fun createLexer(project: Project): Lexer = WippyLuaLexerAdapter()
    override fun createParser(project: Project): PsiParser = WippyLuaParser()
    override fun getFileNodeType() = FILE
    override fun getCommentTokens() = COMMENTS
    override fun getWhitespaceTokens() = WHITE_SPACES
    override fun getStringLiteralElements() = STRINGS
    override fun createElement(node: ASTNode): PsiElement = WippyLuaTypes.Factory.createElement(node)
    override fun createFile(viewProvider: FileViewProvider) = WippyLuaFile(viewProvider)
}


//internal class PHPOpParserDefinition : ParserDefinition {
//    override fun createLexer(project: Project) = PHPOpLexerAdapter()
//
//    override fun getCommentTokens() = PHPOpTokenSets.COMMENTS
//
//    override fun getWhitespaceTokens(): TokenSet = PHPOpTokenSets.WHITESPACES
//
//    override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY
//
//    override fun createParser(project: Project?) = PHPOpParser()
//
//    override fun getFileNodeType() = FILE
//
//    override fun createFile(viewProvider: FileViewProvider) = PHPOpFile(viewProvider)
//
//    override fun createElement(node: ASTNode): PsiElement = PHPOpTypes.Factory.createElement(node)
//
//    companion object {
//        val FILE = IFileElementType(PHPOpLanguage.INSTANCE)
//    }
//}
