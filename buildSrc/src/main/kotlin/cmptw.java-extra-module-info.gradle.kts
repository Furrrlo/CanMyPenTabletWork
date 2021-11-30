@file:Suppress("UnstableApiUsage")

import de.jjohannes.gradle.javamodules.ModuleInfo
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    java
    id("de.jjohannes.extra-java-module-info")
}

val libs = the<LibrariesForLibs>()
extraJavaModuleInfo {
    val module = { dependency: MinimalExternalModuleDependency, moduleName: String, conf: Action<in ModuleInfo> ->
        module(jarNameFromModule(dependency), moduleName, versionForModule(dependency), conf)
    }
    val automaticModule = { dependency: MinimalExternalModuleDependency, moduleName: String ->
        automaticModule(jarNameFromModule(dependency), moduleName)
    }

    failOnMissingModuleInfo.set(true)
    automaticModule(jarNameFromModule("org.jetbrains:annotations:16.0.2"), "org.jetbrains.annotations")

    automaticModule(libs.taskdialogs.get(), "org.bidib.org.oxbow.swingbits")
    automaticModule(libs.swingx.get(), "org.swinglabs.swingx.core")
    automaticModule(libs.miglayout.get(), "com.miglayout")
    automaticModule(libs.jiconfont.core.get(), "com.github.jiconfont.core")
    automaticModule(libs.jiconfont.swing.get(), "com.github.jiconfont.swing")
    automaticModule(libs.jiconfont.fontawesome.get(), "com.github.jiconfont.font_awesome")
    automaticModule(libs.rsyntaxtextarea.get(), "com.fifesoft.rsyntaxtextarea")
    automaticModule(libs.autocomplete.get(), "com.fifesoft.autocomplete")

    module(libs.darklaf.rsyntaxarea.get(), "darklaf.extensions.rsyntaxarea") {
        requires("darklaf.core")
        requires("com.fifesoft.rsyntaxtextarea")

        exports("com.github.weisj.darklaf.extensions.rsyntaxarea")
    }
}

configurations.annotationProcessor {
    attributes { attribute(Attribute.of("javaModule", Boolean::class.javaObjectType), false) }
}

fun jarNameFromModule(dependency: MinimalExternalModuleDependency): String {
    return jarNameFromModule(dependency.module.group, dependency.module.name, versionForModule(dependency))
}

fun jarNameFromModule(decl: String): String {
    val split = decl.split(":")
    if(split.size != 3)
        throw UnsupportedOperationException("Invalid module declaration $decl")
    return jarNameFromModule(split[0], split[1], split[2])
}

fun jarNameFromModule(group: String, name: String, version: String): String {
    return "$name-$version.jar"
}

fun versionForModule(dependency: MinimalExternalModuleDependency): String {
    return if(dependency.versionConstraint.strictVersion.isNotEmpty())
        dependency.versionConstraint.strictVersion
    else if(dependency.versionConstraint.requiredVersion.isNotEmpty())
        dependency.versionConstraint.requiredVersion
    else if(dependency.versionConstraint.preferredVersion.isNotEmpty())
        dependency.versionConstraint.preferredVersion
    else
        throw UnsupportedOperationException("Version not specified for module ${dependency.module.group}:${dependency.module.name}")
}