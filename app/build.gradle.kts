plugins {
    application
    id("cmptw.java-conventions")
}

version = "0.1"

application {
    mainClass.set("CanMyPenTabletWork")
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