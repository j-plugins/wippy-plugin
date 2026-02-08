package com.github.xepozz.wippy

import com.intellij.spellchecker.BundledDictionaryProvider

class SpellcheckingDictionaryProvider : BundledDictionaryProvider {
    override fun getBundledDictionaries(): Array<String> = arrayOf("/wippy.dic")
}