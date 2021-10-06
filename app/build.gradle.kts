plugins {
    application
}

version = "0.1"

application {
    mainClass.set("CanMyPenTabletWork")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-windows"))

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
    implementation("com.miglayout:miglayout:3.7.4")
}