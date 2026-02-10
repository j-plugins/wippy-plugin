package com.github.xepozz.wippy.run

import com.github.xepozz.wippy.tool.WippyToolSettings
import com.github.xepozz.wippy.tool.WippyToolType
import com.intellij.execution.CommonProgramRunConfigurationParameters
import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.InputRedirectAware
import com.intellij.execution.InputRedirectAware.InputRedirectOptionsImpl
import com.intellij.execution.configuration.AbstractRunConfiguration
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.target.LanguageRuntimeType
import com.intellij.execution.target.TargetEnvironment
import com.intellij.execution.target.TargetEnvironmentAwareRunProfile
import com.intellij.execution.target.TargetEnvironmentAwareRunProfileState
import com.intellij.execution.target.TargetEnvironmentConfiguration
import com.intellij.execution.target.TargetEnvironmentRequest
import com.intellij.execution.target.TargetProgressIndicator
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project


class WippyRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String,
) : AbstractRunConfiguration(project, factory),
    TargetEnvironmentAwareRunProfile,
    InputRedirectAware,
    CommonProgramRunConfigurationParameters
{
    public override fun getOptions(): WippyRunConfigurationOptions =
        super.getOptions() as WippyRunConfigurationOptions

    override fun getValidModules(): Collection<Module?>? {
        return getValidModules(project)
    }

    fun getValidModules(project: Project): MutableList<Module?> {
        val modules = ModuleManager.getInstance(project).modules
        val result: MutableList<Module?> = ArrayList<Module?>()
        for (module in modules) {
            result.add(module)
//            if (PythonSdkUtil.findPythonSdk(module) != null) {
//                result.add(module)
//            }
        }
        return result
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
        WippyRunConfigurationEditor(project, this)

    override fun checkConfiguration() {
        val opts = options
        val toolId = opts.toolId
        if (toolId.isNullOrEmpty()) {
            throw RuntimeConfigurationError("Wippy tool is not selected")
        }
        val tool = WippyToolSettings.getInstance().findById(toolId)
            ?: throw RuntimeConfigurationError("Selected Wippy tool not found")
        if (tool.type == WippyToolType.LOCAL && tool.path.isEmpty()) {
            throw RuntimeConfigurationError("Wippy binary path is not configured")
        }
        if (opts.namespace.isNullOrEmpty()) {
            throw RuntimeConfigurationError("Namespace is not specified")
        }
        if (opts.command.isNullOrEmpty()) {
            throw RuntimeConfigurationError("Command is not specified")
        }
    }

    override fun needPrepareTarget(): Boolean {
        return true
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return object : CommandLineState(environment), TargetEnvironmentAwareRunProfileState {
            override fun startProcess(): ProcessHandler {
                val opts = options
                val toolId = opts.toolId ?: throw ExecutionException("Wippy tool is not selected")

                val tool = WippyToolSettings.getInstance().findById(toolId)
                    ?: throw ExecutionException("Wippy tool not found")

                val configFile = opts.configFile
                val additionalArgs = opts.additionalArgs

                val cmd = GeneralCommandLine().apply {
                    exePath = when (tool.type) {
                        WippyToolType.LOCAL -> tool.path
                        WippyToolType.REMOTE -> "wippy"
                    }

                    addParameter("run")
                    addParameter("-x")
                    addParameter("${opts.namespace}:${opts.command}")

                    if (!configFile.isNullOrEmpty()) {
                        addParameter("--config")
                        addParameter(configFile)
                    }

                    if (!additionalArgs.isNullOrEmpty()) {
                        addParameters(additionalArgs.split(" "))
                    }

                    setWorkDirectory(opts.workingDirectory ?: project.basePath)
                }

                val handler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(cmd)
                ProcessTerminatedListener.attach(handler)
                return handler
            }

            override fun prepareTargetEnvironmentRequest(
                request: TargetEnvironmentRequest,
                targetProgressIndicator: TargetProgressIndicator
            ) {

            }

            override fun handleCreatedTargetEnvironment(
                targetEnvironment: TargetEnvironment,
                targetProgressIndicator: TargetProgressIndicator
            ) {
            }

        //
//            override fun createCustomTargetEnvironmentRequest(): TargetEnvironmentRequest? {
//                return LocalTargetEnvironmentRequest()
//            }
        }
    }

    override fun canRunOn(target: TargetEnvironmentConfiguration): Boolean {
        println("canRunOn: $target")
        return true
//        return target.runtimes.findByType(JavaLanguageRuntimeConfiguration::class.java) != null
    }

    override fun getDefaultLanguageRuntimeType(): LanguageRuntimeType<*>? {
        println("getDefaultLanguageRuntimeType")
        return LanguageRuntimeType.EXTENSION_NAME.findExtension(WippyRuntimeType::class.java);
    }

    private var myDefaultTargetName: String? = null;
    //com.intellij.execution.target.local.LocalTargetType.LOCAL_TARGET_NAME

    override fun getDefaultTargetName(): String? {
        return myDefaultTargetName
    }

    override fun setDefaultTargetName(targetName: String?) {
        myDefaultTargetName = targetName
    }

    private var myProgramParameters: String? = null
    override fun setProgramParameters(value: String?) {
        myProgramParameters = value
    }

    override fun getProgramParameters(): String? {
        return myProgramParameters
    }

    override fun setWorkingDirectory(value: String?) {
        options.workingDirectory = value
    }

    override fun getWorkingDirectory(): String? {
        return options.workingDirectory
    }

    private var myEnvs: Map<String?, String?> = emptyMap()
    override fun setEnvs(envs: Map<String?, String?>) {
        myEnvs = envs
    }

    override fun getEnvs(): Map<String?, String?> {
        return myEnvs
    }

    private var myPassParentEnvs: Boolean = false
    override fun setPassParentEnvs(passParentEnvs: Boolean) {
        myPassParentEnvs = passParentEnvs
    }

    override fun isPassParentEnvs(): Boolean {
        return myPassParentEnvs
    }

    private val myInputRedirectOptions = InputRedirectOptionsImpl()
    override fun getInputRedirectOptions(): InputRedirectAware.InputRedirectOptions {
        return myInputRedirectOptions
    }
}