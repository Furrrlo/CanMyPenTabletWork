plugins {
    application
}

version = "0.1"

application {
    mainClass.set("CanMyPenTabletWork")
}

dependencies {
    implementation(project(":core"))
    runtimeOnly(project(":core-windows"))

    implementation("com.google.code.gson:gson:2.8.9")
    implementation("net.harawata:appdirs:1.2.1")

    // Logging dependencies
    val slf4j = project.ext["slf4jVersion"] as String
    // TODO: Wait for log4j to fix https://github.com/qos-ch/slf4j/commit/3e2381ea694c
    // runtimeOnly("org.slf4j:slf4j-jdk-platform-logging:$slf4j")
    implementation("org.slf4j:jul-to-slf4j:$slf4j") // Redirect jna to slf4j

    val log4j = "2.14.1"
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j18-impl:$log4j")
    runtimeOnly("org.apache.logging.log4j:log4j-api:$log4j")
    runtimeOnly("org.apache.logging.log4j:log4j-core:$log4j")

    // Gui
    implementation("com.github.weisj:darklaf-core:2.7.2")
    implementation("org.swinglabs:swingx-core:1.6.2-2")
    implementation("com.miglayout:miglayout:3.7.4")
    implementation("com.github.jiconfont:jiconfont-swing:1.0.0")
    implementation("com.github.jiconfont:jiconfont-font_awesome:4.7.0.1")
}