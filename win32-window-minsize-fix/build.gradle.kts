plugins {
    `java-library`
}

dependencies {
    val jna = project.ext["jnaVersion"] as String
    api("net.java.dev.jna:jna-jpms:$jna")
    api("net.java.dev.jna:jna-platform-jpms:$jna")
}