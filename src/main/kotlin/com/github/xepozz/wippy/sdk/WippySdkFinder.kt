//package com.github.xepozz.wippy.sdk
//
//import com.intellij.openapi.project.Project
//import com.intellij.openapi.projectRoots.Sdk
//import com.intellij.openapi.projectRoots.SdkTypeId
//import com.intellij.openapi.roots.impl.SdkFinder
//import java.io.File
//
//class WippySdkFinder : SdkFinder() {
//    override fun findSdk(name: String, sdkType: String): Sdk? {
//        val homeEnv = System.getenv("WIPPY_HOME")?.trim().orEmpty()
//        val candidates = listOf(
//            "/usr/local/bin",
//            "/opt/homebrew/bin",
//            "/usr/bin",
//            System.getProperty("user.home") + "/.local/bin",
//            System.getProperty("user.home") + "/bin",
//        ).toMutableList().apply {
//            if (homeEnv.isNotEmpty()) add(homeEnv)
//        }
//
//        return candidates
//            .filter { dir ->
//                val d = File(dir)
//                d.isDirectory && (File(d, "wippy").canExecute() || File(d, "wippy.exe").canExecute())
//            }.firstOrNull()
//    }
//}