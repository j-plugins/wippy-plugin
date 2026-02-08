package com.github.xepozz.wippy.index

import com.intellij.psi.PsiFile
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.YAMLUtil

class WippyConfigIndex : ScalarIndexExtension<String>() {
    companion object {
        val NAME = ID.create<String, Void>("com.github.xepozz.wippy.WippyConfigIndex")
        const val CONFIG_KEY = "wippy.config"
    }

    override fun getName(): ID<String, Void> = NAME

    override fun getIndexer(): DataIndexer<String, Void, FileContent> = DataIndexer { inputData ->
        val psiFile = inputData.psiFile as? YAMLFile ?: return@DataIndexer emptyMap()

        if(!WippyConfigUtil.isWippyConfigFile(psiFile)) return@DataIndexer emptyMap()

        return@DataIndexer mapOf(CONFIG_KEY to null)
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getVersion(): Int = 1

    override fun getInputFilter(): FileBasedIndex.InputFilter = FileBasedIndex.InputFilter { file ->
        file.fileType == YAMLFileType.YML
    }

    override fun dependsOnFileContent(): Boolean = true
}
