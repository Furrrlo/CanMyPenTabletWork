plugins {
    id("cmptw.java-library-conventions")
    id("com.github.furrrlo.jflex-token-maker")
}

dependencies {
    api(projects.core)
    api(libs.bundles.jna)

    api(libs.autoservice.annotations)
    annotationProcessor(libs.autoservice.processor)

    jflex("de.jflex:jflex:1.4.1")
}

tasks.processResources {
    val doNotUsePreBuilt = rootProject.ext.has("win32KeyboardHook.prebuilt") &&
            !(rootProject.ext["win32KeyboardHook.prebuilt"] as String).toBoolean()

    // x86
    from(
        if(doNotUsePreBuilt)
            project(":win32-keyboard-hook").tasks
                .named<LinkSharedLibrary>("linkReleaseX86")
                .map { it.linkedFile }
        else
            project(":win32-keyboard-hook").file("prebuilt/x86/win32-keyboard-hook.dll")
    ) { rename(".*", "win32-x86/global_keyboard_hook.dll") }
    // x86-64
    from(
        if(doNotUsePreBuilt)
            project(":win32-keyboard-hook").tasks
                .named<LinkSharedLibrary>("linkReleaseX86-64")
                .map { it.linkedFile }
        else
            project(":win32-keyboard-hook").file("prebuilt/x86-64/win32-keyboard-hook.dll")
    ) { rename(".*", "win32-x86-64/global_keyboard_hook.dll") }
}
