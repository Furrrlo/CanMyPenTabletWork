plugins {
    application
}

group = "me.ferlo.cmptw"
version = "0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

application {
    mainClass.set("CanMyPenTabletWork")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

dependencies {
    // For raw input (Win32 API)
    implementation("net.java.dev.jna:jna-jpms:5.9.0")
    implementation("net.java.dev.jna:jna-platform-jpms:5.9.0")
    // For global hook (with the nice stuff like modifiers etc already sorted out)
    // Use the snapshot cause the release has a messed up POM
    implementation("com.github.kwhat:jnativehook:2.2-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.0")
}

tasks.test {
    useJUnitPlatform()
}
