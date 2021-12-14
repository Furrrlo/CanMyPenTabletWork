import org.gradle.internal.jvm.Jvm
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    application
    id("cmptw.java-conventions")
    id("org.beryx.jlink") version "2.24.4"
}

version = "0.1"
group = "${group}.app"

application {
    mainClass.set("com.github.furrrlo.cmptw.app.CanMyPenTabletWork")
    mainModule.set(group.toString())
}

// TODO: To use darklaf nightly build (for module support)
repositories {
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}
// --------------------------------------

dependencies {
    implementation(projects.core)
    implementation(projects.win32WindowMinsizeFix)
    runtimeOnly(projects.coreWindows)

    runtimeOnly(libs.bundles.jna)

    implementation(libs.gson)
    implementation(libs.appdirs) {
        exclude(group = libs.jna.core.get().module.group) // Already included anyway, just as a runtime dependency
    }

    implementation(libs.bundles.logging.compile)
    runtimeOnly(libs.bundles.logging.runtime)

    // Gui
    implementation(libs.bundles.darklaf)
    implementation(libs.taskdialogs)
    implementation(libs.swingx)
    implementation(libs.miglayout)
    implementation(libs.bundles.jiconfont)
    implementation(libs.rsyntaxtextarea)
    implementation(libs.autocomplete)
}

jlink {
    // Fix task inputs, it uses the configuration name insteaf of the file collection
    listOf(
        "prepareModulesDir",
        "prepareMergedJarsDir",
    ).forEach { taskName -> tasks.named(taskName) {
        inputs.files(configuration.map { project.configurations.named(it) })
    }}

    options.addAll(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))

    // log4j-core is not modular
    forceMerge("log4j-api")
    // jul to slf4j is not modular
    forceMerge("slf4j")

    launcher {
        name = "CanMyPenTabletWork"
    }

    val baseImageOptions = listOf(
        "--description", "CanMyPenTabletWork",
        "--vendor", "Francesco Ferlin",
        "--copyright", "Copyright (C) 2021 Francesco Ferlin",
    )
    val baseInstallerOptions = listOf<String>(
//        "--license-file", "", TODO
    )

    targetPlatform("current") {
        setJdkHome(Jvm.current().javaHome)
    }

    if(DefaultNativePlatform.getCurrentOperatingSystem().isWindows) {
        // Add windows specific core module as a root module, noone references it otherwise
        options.addAll(listOf("--add-modules", "com.github.furrrlo.cmptw.windows"))

        jpackage {
            installerType = "msi"
            installerOptions = baseInstallerOptions + listOf("--win-dir-chooser", "--win-shortcut-prompt", "--win-menu", "--win-shortcut")
            imageOptions = baseImageOptions
//            icon = "icon.ico"
        }
    }
}
