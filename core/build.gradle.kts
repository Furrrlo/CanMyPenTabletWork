plugins {
    `java-library`
}

dependencies {
    // TODO: I don't like these dependency, but I can't think  of any way of removing them
    api("com.fifesoft:rsyntaxtextarea:${project.ext["rSyntaxTextAreaVersion"]}")
    api("com.fifesoft:autocomplete:${project.ext["autocompleteVersion"]}")
}