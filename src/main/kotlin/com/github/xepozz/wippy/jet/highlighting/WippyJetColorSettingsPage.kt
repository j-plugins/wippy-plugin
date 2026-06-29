package com.github.xepozz.wippy.jet.highlighting

import com.github.xepozz.wippy.WippyIcons
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

class WippyJetColorSettingsPage : ColorSettingsPage {
    companion object {
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Delimiter ({{ }})", WippyJetSyntaxHighlighter.DELIMITER),
            AttributesDescriptor("Keyword", WippyJetSyntaxHighlighter.KEYWORD),
            AttributesDescriptor("Field", WippyJetSyntaxHighlighter.FIELD),
            AttributesDescriptor("Identifier", WippyJetSyntaxHighlighter.IDENTIFIER),
            AttributesDescriptor("Number", WippyJetSyntaxHighlighter.NUMBER),
            AttributesDescriptor("String", WippyJetSyntaxHighlighter.STRING),
            AttributesDescriptor("Comment", WippyJetSyntaxHighlighter.COMMENT),
            AttributesDescriptor("Operator", WippyJetSyntaxHighlighter.OPERATOR),
            AttributesDescriptor("Parentheses", WippyJetSyntaxHighlighter.PARENTHESES),
            AttributesDescriptor("Brackets", WippyJetSyntaxHighlighter.BRACKETS),
            AttributesDescriptor("Comma", WippyJetSyntaxHighlighter.COMMA),
        )
    }

    override fun getIcon(): Icon = WippyIcons.WIPPY
    override fun getHighlighter() = WippyJetSyntaxHighlighter()
    override fun getDemoText() = """
        {{ extends "layout" }}

        {* a comment *}
        {{ block title() }}{{ data.title }}{{ end }}

        <ul>
        {{ range i, item := data.items }}
            <li>{{ i }}: {{ item.name | lower }}</li>
        {{ end }}
        </ul>
    """.trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap() = null
    override fun getAttributeDescriptors() = DESCRIPTORS
    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
    override fun getDisplayName() = "WippyJet"
}
