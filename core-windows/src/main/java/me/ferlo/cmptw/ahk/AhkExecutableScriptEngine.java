package me.ferlo.cmptw.ahk;

import me.ferlo.cmptw.script.ExecutableScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

class AhkExecutableScriptEngine implements ExecutableScriptEngine {

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
            System.out.println(Arrays.toString(new String[] {
                    executable.toAbsolutePath().toString(), "/force", "/ErrorStdOut=UTF-8", script.toAbsolutePath().toString()
            }));
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
