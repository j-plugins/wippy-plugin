
package com.github.xepozz.wippy.project

import com.intellij.openapi.vfs.VirtualFile

data class WippyProject(
    val root: VirtualFile,
    val lockFile: VirtualFile,
    val srcDir: VirtualFile?,
    val modulesDir: VirtualFile?,
) {
    val name: String get() = root.name

    val displayPath: String get() = root.path
}