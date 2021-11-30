package com.github.furrrlo.jflex;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.*;
import org.gradle.work.Incremental;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FixJFlexTokenMakerTask extends DefaultTask {

    private static final Pattern ZZ_REFILL_REGEX = Pattern.compile(
            "boolean\\s+zzRefill\\s*\\(\\)\\s*(?:throws\\s+(?:java\\.io\\.)?IOException\\s*)?\\{");
    private static final Pattern YY_RESET_REGEX = Pattern.compile(
           "void\\s+yyreset\\s*\\((?:java\\.io\\.)?Reader\\s.*?\\)\\s*\\{");

    private final ConfigurableFileCollection sources;
    private final DirectoryProperty destination;

    public FixJFlexTokenMakerTask() {
        this.sources = getProject().getObjects().fileCollection();
        this.destination = getProject().getObjects().directoryProperty();
    }

    @TaskAction
    public void fix() throws IOException {
        final Path destination = this.destination.get().getAsFile().toPath();
        Files.walkFileTree(destination, new CleanOutputDirVisitor());

        for (File srcDir : sources.getFiles()) {
            for (File file : getProject().fileTree(srcDir, tree -> tree.include("**/*.java")).getFiles()) {
                final Path relative = srcDir.toPath().relativize(file.toPath());
                final Path target = destination.resolve(relative);

                getLogger().info(file.getAbsolutePath() + " -> " + target.toAbsolutePath());
                Files.createDirectories(target.getParent());

                try(var writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8);
                    var lines = Files.lines(file.toPath(), StandardCharsets.UTF_8)) {

                    // extremely fragile, relies on {} only being used in code and not in strings, comments, etc
                    // and also leaves hanging javadocs, but seems to be working
                    int lineNumber = 0, startRemovalLineNumber = 0;
                    boolean removing = false;
                    int parentheses = 0;
                    for(var iter = lines.iterator(); iter.hasNext(); lineNumber++) {
                        final String line = iter.next();

                        if(ZZ_REFILL_REGEX.matcher(line).find()) {
                            getLogger().info("Found zzRefill method at line {}", lineNumber);
                            startRemovalLineNumber = lineNumber;
                            parentheses = 1;
                            removing = true;
                            continue;
                        }

                        if(YY_RESET_REGEX.matcher(line).find()) {
                            getLogger().info("Found yyReset method at line {}", lineNumber);
                            startRemovalLineNumber = lineNumber;
                            parentheses = 1;
                            removing = true;
                            continue;
                        }

                        if(removing) {
                            for(var charIter = line.chars().iterator(); charIter.hasNext(); ) {
                                final int ch = charIter.next();
                                if(ch == '{')
                                    parentheses++;
                                if(ch == '}')
                                    parentheses--;
                                if(parentheses <= 0)
                                    break;
                            }

                            if(parentheses <= 0) {
                                getLogger().info("Removed lines from {} to {}", startRemovalLineNumber, lineNumber);
                                removing = false;
                            }

                            continue;
                        }

                        writer.write(line);
                        writer.newLine();
                    }
                }
            }
        }
    }

    @Internal
    public ConfigurableFileCollection getSources() {
        return sources;
    }

    @InputFiles
    @Incremental
    @IgnoreEmptyDirectories
    @PathSensitive(PathSensitivity.RELATIVE)
    public Set<File> getAllSourceFiles() {
        return sources.getFiles().stream()
                .map(srcDir -> getProject().fileTree(srcDir, tree -> tree.getIncludes().add("**/*.java")))
                .flatMap(fileTree -> fileTree.getFiles().stream())
                .collect(Collectors.toSet());
    }

    @OutputDirectory
    public DirectoryProperty getDestination() {
        return destination;
    }
}
