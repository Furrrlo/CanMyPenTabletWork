plugins {
    `java-library`
}

dependencies {
    // TODO: I don't like this dependency, but I can't think  of any way of removing it
    api("com.fifesoft:rsyntaxtextarea:${project.ext["rSyntaxTextAreaVersion"]}")
}