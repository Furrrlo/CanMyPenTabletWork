plugins {
    java
    id("cmptw.java-extra-module-info")
}

group = rootProject.group

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:${project.ext["slf4jVersion"]}")
}

tasks.test {
    useJUnitPlatform()
}