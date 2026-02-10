package com.github.xepozz.wippy.run

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project

class WippyRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<WippyRunConfigurationOptions>(project, factory, name) {

    public override fun getOptions(): WippyRunConfigurationOptions = super.getOptions() as WippyRunConfigurationOptions

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = WippySettingsEditor()

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return object : CommandLineState(environment) {
            override fun startProcess(): ProcessHandler {
                val commandLine = GeneralCommandLine()
                commandLine.setWorkDirectory(project.basePath)
                // logic for finding wippy executable should be here
                // for now we assume it's in PATH
                commandLine.exePath = "wippy"

                val namespace = "app"
                val command = options.scriptPath?.substringAfterLast("/")?.substringBeforeLast(".")
                    ?: throw ExecutionException("Cannot determine script name from path: ${options.scriptPath}")

                options.functionName?.let {
                    commandLine.addParameter("run")
                    commandLine.addParameter("-x")
                    commandLine.addParameter("$namespace:$command")
                }

                val processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
                ProcessTerminatedListener.attach(processHandler)
                return processHandler
            }
        }
    }
}
