import org.gradle.internal.jvm.Jvm
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    application
    id("cmptw.java-conventions")
    id("com.github.furrrlo.jpackage")
}

version = "0.1"
group = "${group}.app"

application {
    mainClass.set("com.github.furrrlo.cmptw.app.CanMyPenTabletWork")
    mainModule.set(group.toString())
}

tasks.named<JavaExec>("run") {
    jvmArgs("-Dcmptw.ide")
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
    implementation(libs.jlaunchcmd)
    implementation(libs.bundles.unique4j)

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
    forceMerge("log4j")
    // jul to slf4j and log4j are not modular
    forceMerge("slf4j")

    mergedModule {
        // Log4J uses a custom ServiceLoader utils class, so the JLink plugin SuggestedMergedModuleInfoBuilder
        // can't properly find its uses. Manually add them
        additive = true
        // Log4j API impl
        uses("org.apache.logging.log4j.util.PropertySource")
        uses("org.apache.logging.log4j.spi.Provider")
        uses("org.apache.logging.log4j.message.ThreadDumpMessage.ThreadInfoFactory")
        // Log4j core stuff
        uses("org.apache.logging.log4j.core.util.WatchEventService")
        uses("org.apache.logging.log4j.core.util.ContextDataProvider")
    }

    launcher {
        name = "CanMyPenTabletWork"
    }

    val baseImageOptions: List<String> = listOf(
        "--description", "CanMyPenTabletWork",
        "--vendor", "Francesco Ferlin",
        "--copyright", "Copyright (C) 2021 Francesco Ferlin",
    )
    val baseInstallerOptions: List<String> = listOf(
        "--verbose",
//        "--license-file", "", TODO
//        "--temp", File(project.buildDir, "tmp-jpackage").absolutePath,
    )
    val modules: MutableList<String> = mutableListOf(
        // Add logging implementation modules, noone references it
        // (when their modularity is finally fixed ;-;)
//        "org.slf4j.jdk.platform.logging",
//        "org.apache.logging.log4j.core",
//        "org.apache.logging.log4j.slf4j",
    )

    targetPlatform("current") {
        setJdkHome(Jvm.current().javaHome)
    }

    if(DefaultNativePlatform.getCurrentOperatingSystem().isWindows) {
        // Add windows specific core module as a root module, noone references it otherwise
        modules.add("com.github.furrrlo.cmptw.windows");

        jpackage {
            installerType = "msi"
            installerOptions = baseInstallerOptions + listOf(
                "--win-dir-chooser",
                "--win-shortcut-prompt", "--win-menu", "--win-shortcut",
            )
            imageOptions = baseImageOptions
//            icon = "icon.ico"

            wix(project) {
                variable("JpxLauncher", launcherData.map { it.name })
            }
        }
    }

    options.addAll(listOf("--add-modules", modules.joinToString(",")))
}
