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
    implementation("net.java.dev.jna:jna-jpms:5.9.0")
    implementation("net.java.dev.jna:jna-platform-jpms:5.9.0")

    // Logging dependencies
    val slf4j = "2.0.0-alpha2"
    implementation("org.slf4j:slf4j-api:$slf4j")
    // TODO: Wait for log4j to fix https://github.com/qos-ch/slf4j/commit/3e2381ea694c
    // runtimeOnly("org.slf4j:slf4j-jdk-platform-logging:$slf4j")
    implementation("org.slf4j:jul-to-slf4j:$slf4j") // Redirect jna to slf4j

    val log4j = "2.14.1"
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j18-impl:$log4j")
    runtimeOnly("org.apache.logging.log4j:log4j-api:$log4j")
    runtimeOnly("org.apache.logging.log4j:log4j-core:$log4j")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.0")
}

tasks.test {
    useJUnitPlatform()
}
