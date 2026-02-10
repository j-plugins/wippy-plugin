package com.github.xepozz.wippy.index

import com.intellij.openapi.project.Project
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLSequence
import java.io.DataInput
import java.io.DataOutput

class WippyEntryIndex : FileBasedIndexExtension<String, List<WippyEntryData>>() {

    companion object {
        val NAME = ID.create<String, List<WippyEntryData>>("WippyEntryIndex")

        fun getNamespaces(project: Project): Set<String> {
            val result = mutableSetOf<String>()
            FileBasedIndex.getInstance().processAllKeys(NAME, { key ->
                result.add(key)
                true
            }, project)
            return result
        }

        fun getEntries(project: Project, namespace: String): List<WippyEntryData> {
            val result = mutableListOf<WippyEntryData>()
            FileBasedIndex.getInstance().processValues(
                NAME, namespace,
                null,
                { _, value ->
                    result.addAll(value)
                    true
                },
                com.intellij.psi.search.GlobalSearchScope.projectScope(project)
            )
            return result
        }

        fun getCommands(project: Project, namespace: String): List<WippyEntryData> =
            getEntries(project, namespace).filter { it.commandName != null }

        fun getAllCommands(project: Project): List<Pair<String, WippyEntryData>> {
            val result = mutableListOf<Pair<String, WippyEntryData>>()
            for (ns in getNamespaces(project)) {
                for (entry in getCommands(project, ns)) {
                    result.add(ns to entry)
                }
            }
            return result
        }
    }

    override fun getName() = NAME

    override fun getVersion() = 3

    override fun dependsOnFileContent() = true

    override fun getInputFilter() = FileBasedIndex.InputFilter { file ->
        file.fileType == YAMLFileType.YML && file.name == "_index.yaml"
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getValueExternalizer() = object : DataExternalizer<List<WippyEntryData>> {
        override fun save(out: DataOutput, value: List<WippyEntryData>) {
            out.writeInt(value.size)
            value.forEach { it.write(out) }
        }

        override fun read(input: DataInput): List<WippyEntryData> {
            val size = input.readInt()
            return (0 until size).map { WippyEntryData.read(input) }
        }
    }

    override fun getIndexer() = DataIndexer<String, List<WippyEntryData>, FileContent> { content ->
        val result = mutableMapOf<String, List<WippyEntryData>>()
        val psiFile = content.psiFile as? YAMLFile ?: return@DataIndexer result
        val doc = psiFile.documents.firstOrNull() ?: return@DataIndexer result
        val root = doc.topLevelValue as? YAMLMapping ?: return@DataIndexer result

        val namespace = root.getKeyValueByKey("namespace")?.valueText ?: return@DataIndexer result
        val entriesSeq = root.getKeyValueByKey("entries")?.value as? YAMLSequence ?: return@DataIndexer result

        val entries = entriesSeq.items.mapNotNull { item ->
            val mapping = item.value as? YAMLMapping ?: return@mapNotNull null
            val name = mapping.getKeyValueByKey("name")?.valueText ?: return@mapNotNull null
            val kind = mapping.getKeyValueByKey("kind")?.valueText ?: ""
            val meta = mapping.getKeyValueByKey("meta")?.value as? YAMLMapping
            val command = meta?.getKeyValueByKey("command")?.value as? YAMLMapping
            val source = mapping.getKeyValueByKey("source")?.valueText
            val method = mapping.getKeyValueByKey("method")?.valueText

            WippyEntryData(
                name = name,
                kind = kind,
                commandName = command?.getKeyValueByKey("name")?.valueText,
                commandShort = command?.getKeyValueByKey("short")?.valueText,
                sourceFile = source,
                method = method,
            )
        }

        if (entries.isNotEmpty()) {
            result[namespace] = entries
        }
        result
    }
}