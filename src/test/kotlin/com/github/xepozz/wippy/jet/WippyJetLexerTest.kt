package com.github.xepozz.wippy.jet

import com.github.xepozz.wippy.jet.lexer.WippyJetLexerAdapter
import com.github.xepozz.wippy.jet.psi.WippyJetTypes
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class WippyJetLexerTest : BasePlatformTestCase() {

    private fun lex(input: String): List<IElementType> {
        val lexer = WippyJetLexerAdapter()
        lexer.start(input)
        val out = mutableListOf<IElementType>()
        while (lexer.tokenType != null) {
            val type = lexer.tokenType!!
            if (type != TokenType.WHITE_SPACE) out += type
            lexer.advance()
        }
        return out
    }

    fun testContentOnly() {
        assertEquals(listOf<IElementType>(WippyJetTypes.JET_CONTENT), lex("<h1>plain html</h1>"))
    }

    fun testField() {
        assertEquals(
            listOf(WippyJetTypes.JET_OPEN, WippyJetTypes.FIELD, WippyJetTypes.JET_CLOSE),
            lex("{{ .name }}"),
        )
    }

    fun testKeywordsAndEnd() {
        assertEquals(
            listOf(
                WippyJetTypes.JET_OPEN, WippyJetTypes.KW_BLOCK, WippyJetTypes.IDENT,
                WippyJetTypes.LPAREN, WippyJetTypes.RPAREN, WippyJetTypes.JET_CLOSE,
                WippyJetTypes.JET_OPEN, WippyJetTypes.KW_END, WippyJetTypes.JET_CLOSE,
            ),
            lex("{{ block title() }}{{ end }}"),
        )
    }

    fun testRangeWithVars() {
        assertEquals(
            listOf(
                WippyJetTypes.JET_OPEN, WippyJetTypes.KW_RANGE, WippyJetTypes.IDENT, WippyJetTypes.COMMA,
                WippyJetTypes.IDENT, WippyJetTypes.DECL, WippyJetTypes.IDENT, WippyJetTypes.JET_CLOSE,
            ),
            lex("{{ range i, v := items }}"),
        )
    }

    fun testPipelineAndString() {
        assertEquals(
            listOf(
                WippyJetTypes.JET_OPEN, WippyJetTypes.STRING, WippyJetTypes.PIPE, WippyJetTypes.IDENT,
                WippyJetTypes.JET_CLOSE,
            ),
            lex("""{{ "str" | lower }}"""),
        )
    }

    fun testComment() {
        val tokens = lex("{* a comment *}")
        assertTrue(tokens.all { it == WippyJetTypes.JET_COMMENT })
        assertTrue(tokens.isNotEmpty())
    }

    fun testTrimMarkers() {
        assertEquals(
            listOf(WippyJetTypes.JET_OPEN, WippyJetTypes.FIELD, WippyJetTypes.JET_CLOSE),
            lex("{{- .x -}}"),
        )
    }

    fun testAttributeContext() {
        assertEquals(
            listOf(
                WippyJetTypes.JET_CONTENT, WippyJetTypes.JET_OPEN, WippyJetTypes.FIELD,
                WippyJetTypes.JET_CLOSE, WippyJetTypes.JET_CONTENT,
            ),
            lex("<img src={{ .url }}>"),
        )
    }
}
