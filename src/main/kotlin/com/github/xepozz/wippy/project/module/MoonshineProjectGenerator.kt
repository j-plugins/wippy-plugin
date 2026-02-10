package com.github.xepozz.wippy.project.module

import com.github.xepozz.wippy.WippyIcons
import com.intellij.ide.util.projectWizard.WebProjectTemplate
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class MoonshineProjectGenerator : WebProjectTemplate<MoonshineProjectGeneratorSettings>() {
    override fun getName() = "MoonShine"
    override fun getIcon() = WippyIcons.WIPPY
    override fun generateProject(
        project: Project,
        baseDir: VirtualFile,
        settings: MoonshineProjectGeneratorSettings,
        module: Module
    ) {
//        val isDownload = PathEnvironmentVariableUtil.findExecutableInPathOnAnyOS("composer") == null
//        val composerExecutable = when {
//            isDownload -> {
//                val phpInterpretersManager = PhpInterpretersManagerImpl.getInstance(project)
//                PharComposerExecution(
//                    phpInterpretersManager.interpreters.firstOrNull()?.id,
//                    "",
//                    false,
//                )
//            }
//
//            else -> ExecutableComposerExecution("composer")
//        }
//
//        println("generateProject: $baseDir, $settings, $module")
//        val version = if (settings.version != "latest") settings.version else null
//
//        val composerSettings = ComposerProjectSettings(
//            isDownload,
//            ComposerPackage(settings.template),
//            version,
//            "--no-progress --no-interaction --ansi --remove-vcs --stability=dev",
//            composerExecutable as ComposerExecution,
//            null,
//            settings.createGit,
//        )
//        ComposerProjectGenerator().generateProject(project, baseDir, composerSettings, module)
//
//        println("generateProject: $baseDir, $settings, $module")
    }

    override fun createPeer() = MoonshineProjectPeer()

    override fun getDescription() = "MoonShine project template"
}