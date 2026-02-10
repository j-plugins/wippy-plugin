package com.github.xepozz.wippy.project.module

import com.github.xepozz.wippy.WippyIcons
import com.github.xepozz.wippy.project.module.override.WebTemplateProjectWizardStep
import com.intellij.ide.util.projectWizard.WebTemplateNewProjectWizardBase
import com.intellij.ide.wizard.NewProjectWizardBaseStep
import com.intellij.ide.wizard.NewProjectWizardChainStep.Companion.nextStep
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.ide.wizard.RootNewProjectWizardStep

class MoonshineGeneratorProjectWizard : WebTemplateNewProjectWizardBase() {
    override val id = "moonshine-project"
    override val name = "MoonShine"
    override val icon = WippyIcons.WIPPY

    val template = MoonshineProjectGenerator()

    override fun createTemplateStep(parent: NewProjectWizardBaseStep): NewProjectWizardStep {
        return RootNewProjectWizardStep(parent.context)
            .nextStep { WebTemplateProjectWizardStep(parent, template) }
    }
}