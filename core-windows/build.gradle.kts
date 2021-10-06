plugins {
    `java-library`
}

dependencies {
    implementation(project(":core"))

    val jna = project.ext["jnaVersion"] as String
    api("net.java.dev.jna:jna-jpms:$jna")
    api("net.java.dev.jna:jna-platform-jpms:$jna")
}