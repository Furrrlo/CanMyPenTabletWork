plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

group = "com.github.furrrlo"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

dependencies {
    implementation("de.jjohannes.gradle:extra-java-module-info:0.9")
    implementation("org.beryx:badass-jlink-plugin:2.24.4")
    // Workaround to import the TOML file, see https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        create("jflex") {
            id = "${group}.jflex"
            implementationClass = "com.github.furrrlo.jflex.JFlexPlugin"
        }
        create("jflex-token-maker") {
            id = "${group}.jflex-token-maker"
            implementationClass = "com.github.furrrlo.jflex.JFlexTokenMakerPlugin"
        }
        create("jpackage") {
            id = "${group}.jpackage"
            implementationClass = "com.github.furrrlo.jpackage.JPackagePlugin"
        }
    }
}