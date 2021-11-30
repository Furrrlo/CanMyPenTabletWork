plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

group = "com.github.furrrlo"

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

dependencies {
    implementation("de.jjohannes.gradle:extra-java-module-info:0.9")
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
    }
}