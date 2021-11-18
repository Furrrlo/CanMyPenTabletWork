plugins {
    `java-library`
}

dependencies {
    implementation(project(":core"))

    val jna = project.ext["jnaVersion"] as String
    api("net.java.dev.jna:jna-jpms:$jna")
    api("net.java.dev.jna:jna-platform-jpms:$jna")

    api("com.google.auto.service:auto-service-annotations:1.0")
    annotationProcessor("com.google.auto.service:auto-service:1.0")
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