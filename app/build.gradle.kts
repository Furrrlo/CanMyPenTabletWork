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

dependencies {
    implementation(projects.core)
    implementation(projects.win32WindowMinsizeFix)
    runtimeOnly(projects.coreWindows)

    runtimeOnly(libs.bundles.jna)

    implementation(libs.gson)
    implementation(libs.appdirs) {
        exclude(group = libs.jna.core.get().module.group) // Already included anyway, just as a runtime dependency
    }

    // TODO: Wait for log4j to fix https://github.com/qos-ch/slf4j/commit/3e2381ea694c
    // runtimeOnly("org.slf4j:slf4j-jdk-platform-logging:$slf4j")
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
    options.addAll(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))

    // Both core and the slf4j impl are not modular
    forceMerge("log4j-api")
    // jul to slf4j is not modular
    forceMerge("slf4j")
    // Proper module support is on the next breaking version
    forceMerge("darklaf")

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
        jpackage {
            installerType = "msi"
            installerOptions = baseInstallerOptions + listOf("--win-dir-chooser", "--win-shortcut-prompt", "--win-menu", "--win-shortcut")
            imageOptions = baseImageOptions
//            icon = "icon.ico"
        }
    }
}
