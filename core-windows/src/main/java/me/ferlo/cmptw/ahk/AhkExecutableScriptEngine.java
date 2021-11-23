package me.ferlo.cmptw.ahk;

import me.ferlo.cmptw.script.ExecutableScriptEngine;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

class AhkExecutableScriptEngine implements ExecutableScriptEngine {

    private static final String SYNTAX_STYLE_AHK = "text/ahk";

    private final Path executable;

    public AhkExecutableScriptEngine(Path executable) {
        this.executable = executable;
    }

    @Override
    public void execute(String script) {
        try {
            final Path tmpFile = Files.createTempFile("script-", ".ahk");
            tmpFile.toFile().deleteOnExit();

            // Autohotkey defaults to UTF-8 if it finds the UTF-8 BOM
            // See https://www.autohotkey.com/boards/viewtopic.php?t=67763
            try(OutputStream os = Files.newOutputStream(tmpFile)) {
                os.write('\ufeef');
                os.write('\ufebb');
                os.write('\ufebf');
            }
            Files.writeString(tmpFile, script, StandardCharsets.UTF_8, StandardOpenOption.APPEND);

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
