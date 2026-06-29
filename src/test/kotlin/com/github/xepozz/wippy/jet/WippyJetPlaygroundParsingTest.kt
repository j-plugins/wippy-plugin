package com.github.xepozz.wippy.jet

import com.github.xepozz.wippy.jet.parser.WippyJetParserDefinition
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.ParsingTestCase
import java.io.File

class WippyJetPlaygroundParsingTest : ParsingTestCase("", "jet", WippyJetParserDefinition()) {

    fun testAllPlaygroundTemplatesParseWithoutErrors() {
        val playground = File("playground")
        if (!playground.isDirectory) return
        val jetFiles = playground.walkTopDown().filter { it.isFile && it.extension == "jet" }.toList()
        if (jetFiles.isEmpty()) return

        val failures = mutableListOf<String>()
        for (jet in jetFiles) {
            val psi = createPsiFile(jet.nameWithoutExtension, jet.readText())
            val errors = PsiTreeUtil.findChildrenOfType(psi, PsiErrorElement::class.java)
            if (errors.isNotEmpty()) {
                failures += "${jet.path}: ${errors.joinToString { "'${it.text}' (${it.errorDescription})" }}"
            }
        }
        assertTrue("Parse errors in playground templates:\n${failures.joinToString("\n")}", failures.isEmpty())
    }

    override fun getTestDataPath(): String = "src/test/testData"

    override fun skipSpaces(): Boolean = false

    override fun includeRanges(): Boolean = true
}
