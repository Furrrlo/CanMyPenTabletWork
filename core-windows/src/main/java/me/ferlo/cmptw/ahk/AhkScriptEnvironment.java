package me.ferlo.cmptw.ahk;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;
import me.ferlo.cmptw.script.ExecutableScriptEngine;
import me.ferlo.cmptw.script.ExecutableScriptEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class AhkScriptEnvironment implements ExecutableScriptEnvironment {

    private static final Logger LOGGER = LoggerFactory.getLogger(AhkScriptEnvironment.class);

    private static final String DEFAULT_AHK_EXE_NAME = "AutoHotkey.exe";
    private static final Collection<String> PROBABLE_INSTALL_LOCATIONS = List.of(
            "C:\\Program Files\\AutoHotkey\\",
            "C:\\Program Files (x86)\\AutoHotkey\\");

    @Override
    public ExecutableScriptEngine createEngineForExecutable(Path binary) throws IllegalArgumentException {
        try {
            final String version = getAhkBinaryVersion(binary);
            LOGGER.info("Found ahk \"{}\" version {}", binary.toAbsolutePath(), version);
            return new AhkExecutableScriptEngine(binary);
        } catch (IOException | TimeoutException | InterruptedException e) {
            throw new IllegalArgumentException("Invalid AHK executable " + binary, e);
        }
    }

    @Override
    public Optional<ExecutableScriptEngine> discoverEngine() {
        try {
            return discoverEngine0();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private Optional<ExecutableScriptEngine> discoverEngine0() throws IOException {
        final Path versionScript = createTempVersionScript();
        try {
            return Stream.<Stream<Supplier<Path>>>of(
                    Stream.of(() -> Paths.get(Advapi32Util.registryGetStringValue(
                            WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\AutoHotkey", "InstallDir"))),
                    Stream.of("PROGRAMFILES", "PROGRAMFILES(X86)").map(envVar -> () -> Paths
                            .get(Objects.requireNonNull(
                                    System.getenv(envVar),
                                    "Env variable " + envVar + " is null"))
                            .resolve("AutoHotkey")),
                    PROBABLE_INSTALL_LOCATIONS.stream().map(f -> () -> Paths.get(f))
            ).flatMap(s -> s).<ExecutableScriptEngine>map(supplier -> {
                final Path binary;
                try {
                    binary = supplier.get().resolve(DEFAULT_AHK_EXE_NAME);
                } catch (Exception ex) {
                    LOGGER.debug("Failed to find AHK exe", ex);
                    return null;
                }

                try {
                    if(!Files.exists(binary))
                        throw new FileNotFoundException("AutoHotkey install directory \"" + binary.toAbsolutePath() + "\" does not exist");
                    if(Files.isDirectory(binary))
                        throw new FileNotFoundException("AutoHotkey install directory \"" + binary.toAbsolutePath() + "\" is a directory");

                    final String version = getAhkBinaryVersion(binary, versionScript);
                    LOGGER.info("Found ahk \"{}\" version {}", binary.toAbsolutePath(), version);

                    return new AhkExecutableScriptEngine(binary);
                } catch (Exception ex) {
                    LOGGER.debug("Failed to find AHK exe in {}", binary, ex);
                    return null;
                }
            }).filter(Objects::nonNull).findFirst();
        } finally {
            Files.deleteIfExists(versionScript);
        }
    }

    Path createTempVersionScript() throws IOException {
        final Path tmpFile = Files.createTempFile("cmptw-version-", ".ahk");
        tmpFile.toFile().deleteOnExit();

        try {
            Files.writeString(tmpFile,
                    """
                    #NoTrayIcon
                    str := "AHK version: " . A_AhkVersion
                    FileAppend, %str%, *
                    """);
            return tmpFile;
        } catch (IOException ex) {
            throw new IOException("Failed to write version.ahk to " + tmpFile, ex);
        }
    }

    String getAhkBinaryVersion(Path binary) throws IOException, TimeoutException, InterruptedException {
        final Path versionScript = createTempVersionScript();
        try {
            return getAhkBinaryVersion(binary, versionScript);
        } finally {
            Files.deleteIfExists(versionScript);
        }
    }

    String getAhkBinaryVersion(Path binary, Path versionScript) throws IOException, TimeoutException, InterruptedException {
        final Process process = new ProcessBuilder()
                .command(binary.toAbsolutePath().toString(), "/force", "/ErrorStdOut=UTF-8", versionScript.toAbsolutePath().toString())
                .redirectErrorStream(true)
                .start();

        if(!process.waitFor(2, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new TimeoutException("Timeout expired for AHK executable " + binary.toAbsolutePath());
        }

        final String version = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        if(!version.startsWith("AHK version: "))
            throw new IOException("AHK executable \"" + binary.toAbsolutePath() + "\" returned an invalid version output: " + version);

        return version.substring("AHK version: ".length()).trim();
    }
}
