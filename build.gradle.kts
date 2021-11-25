
subprojects {
    if(this == project(":win32-keyboard-hook"))
        return@subprojects

    apply(plugin = "java")

    group = "me.ferlo.cmptw"

    extensions.configure<JavaPluginExtension>("java") {
        sourceCompatibility = JavaVersion.VERSION_16
        targetCompatibility = JavaVersion.VERSION_16
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    ext {
        set("slf4jVersion", "2.0.0-alpha2")
        set("jnaVersion", "5.9.0")
        set("rSyntaxTextAreaVersion", "3.1.3")
        set("autocompleteVersion", "3.1.2")
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        "implementation"("org.slf4j:slf4j-api:${project.ext["slf4jVersion"]}")
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }
}
