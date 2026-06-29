package com.github.xepozz.wippy.jet.highlighting

import com.github.xepozz.wippy.jet.lexer.WippyJetLexerAdapter
import com.github.xepozz.wippy.jet.psi.WippyJetTypes
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

class WippyJetSyntaxHighlighter : SyntaxHighlighterBase() {
    companion object {
        val DELIMITER = createTextAttributesKey("WIPPY_JET_DELIMITER", DefaultLanguageHighlighterColors.KEYWORD)
        val KEYWORD = createTextAttributesKey("WIPPY_JET_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val FIELD = createTextAttributesKey("WIPPY_JET_FIELD", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
        val IDENTIFIER = createTextAttributesKey("WIPPY_JET_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)
        val NUMBER = createTextAttributesKey("WIPPY_JET_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val STRING = createTextAttributesKey("WIPPY_JET_STRING", DefaultLanguageHighlighterColors.STRING)
        val COMMENT = createTextAttributesKey("WIPPY_JET_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
        val OPERATOR = createTextAttributesKey("WIPPY_JET_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        val PARENTHESES = createTextAttributesKey("WIPPY_JET_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
        val BRACKETS = createTextAttributesKey("WIPPY_JET_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
        val COMMA = createTextAttributesKey("WIPPY_JET_COMMA", DefaultLanguageHighlighterColors.COMMA)
        val BAD_CHARACTER = createTextAttributesKey("WIPPY_JET_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

        private val KEYWORDS = setOf(
            WippyJetTypes.KW_EXTENDS, WippyJetTypes.KW_IMPORT, WippyJetTypes.KW_INCLUDE, WippyJetTypes.KW_BLOCK,
            WippyJetTypes.KW_END, WippyJetTypes.KW_YIELD, WippyJetTypes.KW_CONTENT, WippyJetTypes.KW_IF,
            WippyJetTypes.KW_ELSE, WippyJetTypes.KW_RANGE, WippyJetTypes.KW_TRY, WippyJetTypes.KW_CATCH,
            WippyJetTypes.KW_RETURN, WippyJetTypes.KW_AND, WippyJetTypes.KW_OR, WippyJetTypes.KW_NOT,
            WippyJetTypes.KW_NIL, WippyJetTypes.KW_MSG, WippyJetTypes.KW_TRANS, WippyJetTypes.BOOL,
        )

        private val OPERATORS = setOf(
            WippyJetTypes.DECL, WippyJetTypes.ASSIGN, WippyJetTypes.EQ, WippyJetTypes.NEQ, WippyJetTypes.GE,
            WippyJetTypes.LE, WippyJetTypes.GT, WippyJetTypes.LT, WippyJetTypes.PLUS, WippyJetTypes.MINUS,
            WippyJetTypes.MUL, WippyJetTypes.DIV, WippyJetTypes.MOD, WippyJetTypes.PIPE, WippyJetTypes.QUESTION,
            WippyJetTypes.COLON, WippyJetTypes.SEMI,
        )
    }

    override fun getHighlightingLexer(): Lexer = WippyJetLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> = when (tokenType) {
        WippyJetTypes.JET_OPEN, WippyJetTypes.JET_CLOSE -> arrayOf(DELIMITER)
        in KEYWORDS -> arrayOf(KEYWORD)
        in OPERATORS -> arrayOf(OPERATOR)
        WippyJetTypes.FIELD -> arrayOf(FIELD)
        WippyJetTypes.IDENT, WippyJetTypes.UNDERSCORE -> arrayOf(IDENTIFIER)
        WippyJetTypes.NUMBER -> arrayOf(NUMBER)
        WippyJetTypes.STRING, WippyJetTypes.RAW_STRING -> arrayOf(STRING)
        WippyJetTypes.JET_COMMENT -> arrayOf(COMMENT)
        WippyJetTypes.LPAREN, WippyJetTypes.RPAREN -> arrayOf(PARENTHESES)
        WippyJetTypes.LBRACK, WippyJetTypes.RBRACK -> arrayOf(BRACKETS)
        WippyJetTypes.COMMA -> arrayOf(COMMA)
        TokenType.BAD_CHARACTER -> arrayOf(BAD_CHARACTER)
        else -> emptyArray()
    }
}
