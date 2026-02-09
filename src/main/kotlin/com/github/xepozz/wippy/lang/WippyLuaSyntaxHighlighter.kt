package com.github.xepozz.wippy.lang

import com.github.xepozz.wippy.lang.psi.WippyLuaTypes
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

class WippyLuaSyntaxHighlighter : SyntaxHighlighterBase() {
    companion object {
        val KEYWORD = createTextAttributesKey("WIPPY_LUA_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val NUMBER = createTextAttributesKey("WIPPY_LUA_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val STRING = createTextAttributesKey("WIPPY_LUA_STRING", DefaultLanguageHighlighterColors.STRING)
        val COMMENT = createTextAttributesKey("WIPPY_LUA_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val BRACKETS = createTextAttributesKey("WIPPY_LUA_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
        val BRACES = createTextAttributesKey("WIPPY_LUA_BRACES", DefaultLanguageHighlighterColors.BRACES)
        val PARENTHESES = createTextAttributesKey("WIPPY_LUA_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
        val COMMA = createTextAttributesKey("WIPPY_LUA_COMMA", DefaultLanguageHighlighterColors.COMMA)
        val DOT = createTextAttributesKey("WIPPY_LUA_DOT", DefaultLanguageHighlighterColors.DOT)
        val OPERATOR = createTextAttributesKey("WIPPY_LUA_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        val BAD_CHARACTER = createTextAttributesKey("WIPPY_LUA_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

        private val KEYWORDS = setOf(
            WippyLuaTypes.AND, WippyLuaTypes.BREAK, WippyLuaTypes.DO, WippyLuaTypes.ELSE, WippyLuaTypes.ELSEIF,
            WippyLuaTypes.END, WippyLuaTypes.FALSE, WippyLuaTypes.FOR, WippyLuaTypes.FUNCTION, WippyLuaTypes.GOTO,
            WippyLuaTypes.IF, WippyLuaTypes.IN, WippyLuaTypes.LOCAL, WippyLuaTypes.NIL, WippyLuaTypes.NOT,
            WippyLuaTypes.OR, WippyLuaTypes.RETURN, WippyLuaTypes.REPEAT, WippyLuaTypes.THEN, WippyLuaTypes.TRUE,
            WippyLuaTypes.UNTIL, WippyLuaTypes.WHILE, WippyLuaTypes.TYPE_KW, WippyLuaTypes.INTERFACE,
            WippyLuaTypes.READONLY, WippyLuaTypes.AS, WippyLuaTypes.ASSERTS, WippyLuaTypes.IS, WippyLuaTypes.TYPEOF,
            WippyLuaTypes.KEYOF, WippyLuaTypes.EXTENDS, WippyLuaTypes.FUN
        )

        private val OPERATORS = setOf(
            WippyLuaTypes.PLUS, WippyLuaTypes.MINUS, WippyLuaTypes.MULT, WippyLuaTypes.DIV, WippyLuaTypes.MOD,
            WippyLuaTypes.EXP, WippyLuaTypes.LEN, WippyLuaTypes.BAND, WippyLuaTypes.BOR, WippyLuaTypes.BNOT,
            WippyLuaTypes.GT, WippyLuaTypes.LT, WippyLuaTypes.ASSIGN, WippyLuaTypes.EQEQ, WippyLuaTypes.NEQ,
            WippyLuaTypes.LTE, WippyLuaTypes.GTE, WippyLuaTypes.SHL, WippyLuaTypes.SHR, WippyLuaTypes.IDIV,
            WippyLuaTypes.CONCAT, WippyLuaTypes.ARROW, WippyLuaTypes.QCOLON, WippyLuaTypes.DCOLON,
            WippyLuaTypes.ELLIPSIS, WippyLuaTypes.QUESTION, WippyLuaTypes.BANG, WippyLuaTypes.AT
        )
    }

    override fun getHighlightingLexer(): Lexer = WippyLuaLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> = when (tokenType) {
        in KEYWORDS -> arrayOf(KEYWORD)
        in OPERATORS -> arrayOf(OPERATOR)
        WippyLuaTypes.NUMBER -> arrayOf(NUMBER)
        WippyLuaTypes.STRING -> arrayOf(STRING)
        WippyLuaTypes.LINE_COMMENT, WippyLuaTypes.BLOCK_COMMENT -> arrayOf(COMMENT)
        WippyLuaTypes.LBRACK, WippyLuaTypes.RBRACK -> arrayOf(BRACKETS)
        WippyLuaTypes.LBRACE, WippyLuaTypes.RBRACE -> arrayOf(BRACES)
        WippyLuaTypes.LPAREN, WippyLuaTypes.RPAREN -> arrayOf(PARENTHESES)
        WippyLuaTypes.COMMA -> arrayOf(COMMA)
        WippyLuaTypes.DOT -> arrayOf(DOT)
        TokenType.BAD_CHARACTER -> arrayOf(BAD_CHARACTER)
        else -> emptyArray()
    }
}
