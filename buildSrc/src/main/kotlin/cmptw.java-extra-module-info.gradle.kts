@file:Suppress("UnstableApiUsage")

import de.jjohannes.gradle.javamodules.ModuleInfo
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    java
    id("de.jjohannes.extra-java-module-info")
}

val libs = the<LibrariesForLibs>()
extraJavaModuleInfo {
    fun module(dependency: MinimalExternalModuleDependency,
               version: String = versionForModule(dependency),
               moduleName: String, conf: Action<in ModuleInfo>
    ) = module(jarNameFromModule(dependency, version), moduleName, version, conf)
    fun automaticModule(dependency: MinimalExternalModuleDependency, version: String = versionForModule(dependency), moduleName: String) =
        automaticModule(jarNameFromModule(dependency), moduleName)

    failOnMissingModuleInfo.set(true)
    automaticModule(jarNameFromModule("org.jetbrains:annotations:16.0.2"), "org.jetbrains.annotations")

    automaticModule(libs.taskdialogs.get(), moduleName = "org.bidib.org.oxbow.swingbits")
    automaticModule(libs.swingx.get(), moduleName = "org.swinglabs.swingx.core")
    automaticModule(libs.miglayout.get(), moduleName = "com.miglayout")
    automaticModule(libs.jiconfont.core.get(), moduleName = "com.github.jiconfont.core")
    automaticModule(libs.jiconfont.swing.get(), moduleName = "com.github.jiconfont.swing")
    automaticModule(libs.jiconfont.fontawesome.get(), moduleName = "com.github.jiconfont.font_awesome")
    automaticModule(libs.rsyntaxtextarea.get(), moduleName = "com.fifesoft.rsyntaxtextarea")
    automaticModule(libs.autocomplete.get(), moduleName = "com.fifesoft.autocomplete")

    module(libs.darklaf.rsyntaxarea.get(), moduleName = "darklaf.extensions.rsyntaxarea") {
        requires("darklaf.core")
        requires("com.fifesoft.rsyntaxtextarea")

        exports("com.github.weisj.darklaf.extensions.rsyntaxarea")
    }

    module(libs.appdirs.get(), moduleName = "net.harawata.appdirs") {
        requires("com.sun.jna")
        requires("com.sun.jna.platform")

        exports("net.harawata.appdirs")
    }

    // Fix log4j automatic module
    module(libs.log4j.slf4j.get(), moduleName = "org.apache.logging.log4j.slf4j") {
        requires("org.slf4j")
        requires("org.apache.logging.log4j")

        exports("org.apache.logging.slf4j")
    }
}

configurations.annotationProcessor {
    attributes { attribute(Attribute.of("javaModule", Boolean::class.javaObjectType), false) }
}

fun jarNameFromModule(dependency: MinimalExternalModuleDependency, version: String = versionForModule(dependency)): String {
    return jarNameFromModule(dependency.module.group, dependency.module.name, version)
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