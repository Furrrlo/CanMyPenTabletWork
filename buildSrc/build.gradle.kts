plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

group = "me.ferlo.cmptw"

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        create("jflex") {
            id = "${group}.jflex"
            implementationClass = "me.ferlo.cmptw.JFlexPlugin"
        }
        create("jflex-token-maker") {
            id = "${group}.jflex-token-maker"
            implementationClass = "me.ferlo.cmptw.JFlexTokenMakerPlugin"
        }
    }
}