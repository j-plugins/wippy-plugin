package com.github.xepozz.wippy.project.module

data class MoonshineProjectGeneratorSettings(
    var version: String = "latest",
    var createGit: Boolean = true,
    var template: String = "moonshine/app",
)