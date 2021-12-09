plugins {
    id("cmptw.java-library-conventions")
}

dependencies {
    // TODO: I don't like these dependency, but I can't think  of any way of removing them
    api(libs.rsyntaxtextarea)
    api(libs.autocomplete)
}