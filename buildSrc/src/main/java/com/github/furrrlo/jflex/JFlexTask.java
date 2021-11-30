package com.github.furrrlo.jflex;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecResult;
import org.gradle.process.internal.ExecException;
import org.gradle.work.Incremental;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class JFlexTask extends DefaultTask {

    private final ConfigurableFileCollection execClasspath;
    private final ConfigurableFileCollection sources;
    private final DirectoryProperty destination;
    private final Property<String> encoding;
    private final Property<Boolean> verbose;
    private final Property<Boolean> dump;
    private final Property<Boolean> timeStatistics;
    private final Property<Boolean> skipMinimization;
    private final Property<Boolean> generateDot;
    private final Property<Boolean> legacyDot;
    private final Property<Boolean> jlex;
    private final Property<Boolean> unusedWarning;
    private final Property<Boolean> noBackup;

    public JFlexTask() {
        this.execClasspath = getProject().getObjects().fileCollection();
        this.sources = getProject().getObjects().fileCollection();
        this.destination = getProject().getObjects().directoryProperty();
        this.encoding = getProject().getObjects().property(String.class);
        this.verbose = getProject().getObjects().property(Boolean.class).convention(false);
        this.dump = getProject().getObjects().property(Boolean.class).convention(false);
        this.timeStatistics = getProject().getObjects().property(Boolean.class).convention(false);
        this.skipMinimization = getProject().getObjects().property(Boolean.class).convention(false);
        this.generateDot = getProject().getObjects().property(Boolean.class).convention(false);
        this.legacyDot = getProject().getObjects().property(Boolean.class).convention(false);
        this.jlex = getProject().getObjects().property(Boolean.class).convention(false);
        this.unusedWarning = getProject().getObjects().property(Boolean.class).convention(false);
        this.noBackup = getProject().getObjects().property(Boolean.class).convention(false);
    }

    @TaskAction
    public void flex() throws Exception {
        getLogger().info("Test running JFlex...");

        String mainClass0 = null;
        final List<ExecResult> execResults = new ArrayList<>();
        final List<String> outputs = new ArrayList<>();
        for(String candidateMainClass : Arrays.asList("jflex.Main", "JFlex.Main")) {
            final String output;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                execResults.add(getProject().javaexec(spec -> {
                    spec.args("--version");
                    spec.setClasspath(getExecClasspath());
                    spec.getMainClass().set(candidateMainClass);
                    spec.setStandardOutput(baos);
                    spec.setErrorOutput(baos);
                    spec.setIgnoreExitValue(true);
                }));
                outputs.add(output = baos.toString(StandardCharsets.UTF_8));
            }

            if(output.startsWith("This is JFlex ")) {
                mainClass0 = candidateMainClass;
                final String version = output.substring("This is JFlex ".length()).trim();
                getLogger().info("Found JFlex version {} with main class {}", version, mainClass0);
                break;
            }
        }

        if(mainClass0 == null) {
            final Exception ex = new Exception("Failed to run JFlex. Did you forget to add it in the 'jflex' configuration?");
            IntStream.range(0, execResults.size()).forEach(idx -> {
                Exception cause = null;
                try {
                    execResults.get(idx).rethrowFailure();
                    execResults.get(idx).assertNormalExitValue();
                } catch (ExecException execEx) {
                    cause = execEx;
                }

                ex.addSuppressed(cause == null ?
                        new Exception(outputs.get(idx).trim()) :
                        new Exception(outputs.get(idx).trim(), cause));
            });
            throw ex;
        }

        final String mainClass = mainClass0;
        execResults.clear();
        outputs.clear();

        final Path destination = this.destination.get().getAsFile().toPath();
        Files.walkFileTree(destination, new CleanOutputDirVisitor());

        boolean failed = false;
        for (File srcDir : sources.getFiles()) {
            for (File file : getProject().fileTree(srcDir, tree -> tree.include("**/*.flex")).getFiles()) {
                final Path relative = srcDir.toPath().relativize(file.toPath());
                final Path target = destination.resolve(relative);

                final List<String> args = Stream.<Stream<String>>of(
                        Stream.of("-d", target.getParent().toAbsolutePath().toString()),
                        encoding.isPresent() ? Stream.of("--encoding", encoding.get()) : Stream.empty(),
                        verbose.get() ? Stream.of("--verbose") : Stream.empty(),
                        dump.get() ? Stream.of("--dump") : Stream.empty(),
                        timeStatistics.get() ? Stream.of("--time") : Stream.empty(),
                        skipMinimization.get() ? Stream.of("--nomin") : Stream.empty(),
                        generateDot.get() ? Stream.of("--dot") : Stream.empty(),
                        legacyDot.get() ? Stream.of("--legacydot") : Stream.empty(),
                        jlex.get() ? Stream.of("--jlex") : Stream.empty(),
                        unusedWarning.get() ? Stream.of("--warn-unused") : Stream.empty(),
                        noBackup.get() ? Stream.of("--nobak") : Stream.empty(),
                        Stream.of(file.getAbsolutePath())
                ).flatMap(s -> s).collect(Collectors.toList());

                try {
                    getLogger().info("Running JFlex: {}", args);
                    getProject().javaexec(spec -> {
                        spec.args(args);
                        spec.setClasspath(getExecClasspath());
                        spec.getMainClass().set(mainClass);
                    }).rethrowFailure();
                } catch (ExecException ex) {
                    getLogger().error("Failed to JFlex file {}: {}", file, ex.getMessage());
                    failed = true;
                }
            }
        }

        if(failed)
            throw new Exception("JFlex failed, check the output for errors");
    }

    @Classpath
    public ConfigurableFileCollection getExecClasspath() {
        return execClasspath;
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
                .map(srcDir -> getProject().fileTree(srcDir, tree -> tree.getIncludes().add("**/*.flex")))
                .flatMap(fileTree -> fileTree.getFiles().stream())
                .collect(Collectors.toSet());
    }

    @OutputDirectory
    public DirectoryProperty getDestination() {
        return destination;
    }

    @Input
    @Optional
    public Property<String> getEncoding() {
        return encoding;
    }

    @Input
    public Property<Boolean> getVerbose() {
        return verbose;
    }

    @Input
    public Property<Boolean> getDump() {
        return dump;
    }

    @Input
    public Property<Boolean> getTimeStatistics() {
        return timeStatistics;
    }

    @Input
    public Property<Boolean> getSkipMinimization() {
        return skipMinimization;
    }

    @Input
    public Property<Boolean> getGenerateDot() {
        return generateDot;
    }

    @Input
    public Property<Boolean> getLegacyDot() {
        return legacyDot;
    }

    @Input
    public Property<Boolean> getJlex() {
        return jlex;
    }

    @Input
    public Property<Boolean> getUnusedWarning() {
        return unusedWarning;
    }

    @Input
    public Property<Boolean> getNoBackup() {
        return noBackup;
    }
}
