package com.github.xepozz.wippy.lang

import com.github.xepozz.wippy.WippyIcons
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

class WippyLuaColorSettingsPage : ColorSettingsPage {
    companion object {
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Keyword", WippyLuaSyntaxHighlighter.KEYWORD),
            AttributesDescriptor("Number", WippyLuaSyntaxHighlighter.NUMBER),
            AttributesDescriptor("String", WippyLuaSyntaxHighlighter.STRING),
            AttributesDescriptor("Comment", WippyLuaSyntaxHighlighter.COMMENT),
            AttributesDescriptor("Brackets", WippyLuaSyntaxHighlighter.BRACKETS),
            AttributesDescriptor("Braces", WippyLuaSyntaxHighlighter.BRACES),
            AttributesDescriptor("Parentheses", WippyLuaSyntaxHighlighter.PARENTHESES),
            AttributesDescriptor("Comma", WippyLuaSyntaxHighlighter.COMMA),
            AttributesDescriptor("Dot", WippyLuaSyntaxHighlighter.DOT),
            AttributesDescriptor("Operator", WippyLuaSyntaxHighlighter.OPERATOR)
        )
    }

    override fun getIcon(): Icon = WippyIcons.WIPPY
    override fun getHighlighter() = WippyLuaSyntaxHighlighter()
    override fun getDemoText() = """
        local function hello(name)
            -- This is a comment
            local message = "Hello, " .. name
            print(message)
            return { 1, 2, 3 }
        end
        
        interface Person
            name: string
            age: number
        end
        
        type User = Person & { id: string }
    """.trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap() = null
    override fun getAttributeDescriptors() = DESCRIPTORS
    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
    override fun getDisplayName() = "WippyLua"
}
