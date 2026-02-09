package com.github.xepozz.wippy.reference

import com.github.xepozz.wippy.index.WippyConfigIndex
import com.github.xepozz.wippy.lang.psi.WippyLuaLocalFuncDefStat
import com.github.xepozz.wippy.lang.psi.WippyLuaTypes
import com.intellij.find.findUsages.CustomUsageSearcher
import com.intellij.find.findUsages.FindUsagesOptions
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.usageView.UsageInfo
import com.intellij.usages.Usage
import com.intellij.usages.UsageInfo2UsageAdapter
import com.intellij.util.Processor
import com.intellij.util.indexing.FileBasedIndex

class WippyLuaFunctionUsageSearcher : CustomUsageSearcher() {
    override fun processElementUsages(
        target: PsiElement,
        processor: Processor<in Usage>,
        options: FindUsagesOptions
    ) {
        if (target !is WippyLuaLocalFuncDefStat) return

        val project = target.project
        val methodName = ReadAction.compute<String?, Throwable> {
            target.node.findChildByType(WippyLuaTypes.IDENT)?.text
        } ?: return

        val scope = options.searchScope as? GlobalSearchScope ?: GlobalSearchScope.projectScope(project)
        
        val files = mutableSetOf<VirtualFile>()
        FileBasedIndex.getInstance().getFilesWithKey(
            WippyConfigIndex.NAME,
            setOf(WippyConfigIndex.CONFIG_KEY),
            { file -> 
                files.add(file)
                true
            },
            scope
        )

        for (file in files) {
            ReadAction.run<Throwable> {
                val psiFile = PsiManager.getInstance(project).findFile(file) ?: return@run
                psiFile.acceptChildren(object : com.intellij.psi.PsiRecursiveElementWalkingVisitor() {
                    override fun visitElement(element: PsiElement) {
                        for (reference in element.references) {
                            if (reference is WippyMethodReference && reference.isReferenceTo(target)) {
                                processor.process(UsageInfo2UsageAdapter(UsageInfo(element)))
                            }
                        }
                        super.visitElement(element)
                    }
                })
            }
        }
    }
}
