package com.github.xepozz.wippy.jet

import com.github.xepozz.wippy.jet.parser.WippyJetParserDefinition
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.ParsingTestCase

class WippyJetParsingTest : ParsingTestCase("", "jet", WippyJetParserDefinition()) {

    private fun assertParses(name: String, code: String) {
        val file: PsiFile = createPsiFile(name, code)
        val errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
        assertTrue(
            "Unexpected parse errors: ${errors.joinToString { "'${it.text}' (${it.errorDescription})" }}",
            errors.isEmpty(),
        )
    }

    fun testExpressions() {
        assertParses("expressions", """{{ .a.b | lower }}{{ x := 1 + 2 * 3 }}{{ x }}{{ items[0] }}{{ cond ? a : b }}""")
    }

    fun testBlocks() {
        assertParses(
            "blocks",
            """{{ if .x }}a{{ else if .y }}b{{ else }}c{{ end }}{{ range i, v := .items }}<li>{{ v }}</li>{{ end }}""",
        )
    }

    fun testTemplateStructure() {
        assertParses(
            "structure",
            """
            {{ extends "layout" }}
            {{ block title() }}{{ data.title }}{{ end }}
            <ul>
            {{ range .kinds }}<li>{{ .kind }}: {{ .count }}</li>{{ end }}
            </ul>
            {* a comment *}
            """.trimIndent(),
        )
    }

    override fun getTestDataPath(): String = "src/test/testData"

    override fun skipSpaces(): Boolean = false

    override fun includeRanges(): Boolean = true
}
