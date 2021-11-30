group = "com.github.furrrlo.cmptw"

subprojects {
    if(this == project(":win32-keyboard-hook"))
        return@subprojects

    ext {
        set("slf4jVersion", "2.0.0-alpha2")
        set("jnaVersion", "5.9.0")
        set("rSyntaxTextAreaVersion", "3.1.3")
        set("autocompleteVersion", "3.1.2")
    }
}
