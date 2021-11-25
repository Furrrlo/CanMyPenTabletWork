package me.ferlo.cmptw.ahk;

import me.ferlo.cmptw.script.ExecutableScriptEngine;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class AhkExecutableScriptEngine implements ExecutableScriptEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(AhkExecutableScriptEngine.class);

    private static final String SYNTAX_STYLE_AHK = "text/ahk";
    private static final String UTF_8_BOM = "\ufeff";

    private final Path executable;
    private AhkCompletionProvider completionProvider;

    public AhkExecutableScriptEngine(Path executable) {
        this.executable = executable;
    }

    @Override
    public boolean validate(String script, boolean canShowGui) {
        Path tmpFolder = null, tmpFile = null;
        try {
            tmpFolder = Files.createTempDirectory("cmptw-script-validator-");
            tmpFile = tmpFolder.resolve("AutoHotKey Validator.ahk");

            // Autohotkey defaults to UTF-8 if it finds the UTF-8 BOM
            // See https://www.autohotkey.com/boards/viewtopic.php?t=67763
            Files.writeString(tmpFile, UTF_8_BOM + script, StandardCharsets.UTF_8);
            return validate(tmpFile, canShowGui);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to validate script", e);
        } finally {
            try {
                if(tmpFile != null)
                    Files.deleteIfExists(tmpFile);
            } catch (IOException ex) {
                LOGGER.error("Failed to delete tmp validator script {}", tmpFile.toAbsolutePath(), ex);
            }

            try {
                if(tmpFolder != null)
                    Files.deleteIfExists(tmpFolder);
            } catch (IOException ex) {
                LOGGER.error("Failed to delete tmp validator directory {}", tmpFolder.toAbsolutePath(), ex);
            }
        }
    }

    @Override
    public boolean validate(Path script, boolean canShowGui) {
        try {
            return doValidate(script, canShowGui);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to validate script " + script.toAbsolutePath(), ex);
        } catch (InterruptedException ex) {
            throw new RuntimeException("Failed to wait for script validation for script " + script.toAbsolutePath(), ex);
        }
    }

    private boolean doValidate(Path script, boolean canShowGui) throws IOException, InterruptedException {
        return new ProcessBuilder()
                .command(Stream.of(
                        executable.toAbsolutePath().toString(),
                        "/force",
                        "/iLib", "nul",
                        !canShowGui ? "/ErrorStdOut=UTF-8" : "",
                        script.toAbsolutePath().toString()
                ).filter(s -> !s.isEmpty()).collect(Collectors.toList()))
                .redirectErrorStream(true)
                .start()
                // https://www.autohotkey.com/docs/Scripts.htm#cmd -> iLib argument
                // if there is a syntax error, the exit code is 2
                .waitFor() != 2;
    }

    @Override
    public void execute(String script) {
        try {
            final Path tmpFile = Files.createTempFile("script-", ".ahk");
            tmpFile.toFile().deleteOnExit();

            // Autohotkey defaults to UTF-8 if it finds the UTF-8 BOM
            // See https://www.autohotkey.com/boards/viewtopic.php?t=67763
            Files.writeString(tmpFile, UTF_8_BOM + script, StandardCharsets.UTF_8);
            execute(tmpFile);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to execute script", e);
        }
    }

    @Override
    public void execute(Path script) {
        try {
            new ProcessBuilder()
                    .command(executable.toAbsolutePath().toString(), "/force", "/ErrorStdOut=UTF-8", script.toAbsolutePath().toString())
                    .redirectErrorStream(true)
                    .inheritIO()
                    .start();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to execute script", e);
        }
    }

    @Override
    public String getFileExtension() {
        return "ahk";
    }

    @Override
    public String getNewScript() {
        return """
                #NoEnv  ; Recommended for performance and compatibility with future AutoHotkey releases.
                #Warn  ; Enable warnings to assist with detecting common errors.
                SendMode Input  ; Recommended for new scripts due to its superior speed and reliability.
                SetWorkingDir %A_ScriptDir%  ; Ensures a consistent starting directory.
                """;
    }

    @Override
    public void createSyntaxStyle(AbstractTokenMakerFactory tokenMakerFactory, FoldParserManager foldParserManager) {
        tokenMakerFactory.putMapping(SYNTAX_STYLE_AHK, AhkTokenMaker.class.getName());
        foldParserManager.addFoldParserMapping(SYNTAX_STYLE_AHK, new CurlyFoldParser());
    }

    @Override
    public String getSyntaxStyle() {
        return SYNTAX_STYLE_AHK;
    }

    @Override
    public Optional<CompletionProvider> getCompletionProvider() {
        if(completionProvider == null)
            completionProvider = new AhkCompletionProvider();
        return Optional.of(completionProvider);
    }

    @Override
    public Path getExecutable() {
        return executable;
    }

    @Override
    public String toString() {
        return "AhkExecutableScriptEngine{" +
                "executable=" + executable +
                '}';
    }
}
