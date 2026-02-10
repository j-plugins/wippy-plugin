package com.github.xepozz.wippy.index

import java.io.DataInput
import java.io.DataOutput

data class WippyEntryData(
    val name: String,
    val kind: String,
    val commandName: String?,
    val commandShort: String?,
    val sourceFile: String?,
    val method: String?,
) {
    fun write(out: DataOutput) {
        out.writeUTF(name)
        out.writeUTF(kind)
        out.writeUTF(commandName ?: "")
        out.writeUTF(commandShort ?: "")
        out.writeUTF(sourceFile ?: "")
        out.writeUTF(method ?: "")
    }

    companion object {
        fun read(input: DataInput): WippyEntryData {
            return WippyEntryData(
                name = input.readUTF(),
                kind = input.readUTF(),
                commandName = input.readUTF().ifEmpty { null },
                commandShort = input.readUTF().ifEmpty { null },
                sourceFile = input.readUTF().ifEmpty { null },
                method = input.readUTF().ifEmpty { null },
            )
        }
    }
}