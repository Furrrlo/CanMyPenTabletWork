package me.ferlo.cmptw.hook;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import me.ferlo.cmptw.script.ScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileBasedHookService implements HookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedHookService.class);
    private static final String HOOK_JSON_FILE_NAME = "hook.json";
    static final String UTF_8_BOM = "\ufeff";

    private final ScriptEngine scriptEngine;
    private final Path rootFolder;
    private final Gson gson = HookGsonSerializer.SERIALIZER.newBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private volatile Set<Hook> saved;
    private volatile Collection<Hook> unmodifiableSaved;

    public FileBasedHookService(ScriptEngine scriptEngine, Path rootFolder) throws IOException {
        this.scriptEngine = scriptEngine;
        this.rootFolder = rootFolder;
        this.saved = load();
        this.unmodifiableSaved = Collections.unmodifiableCollection(this.saved);
    }

    @Override
    public Collection<Hook> getSaved() {
        return unmodifiableSaved;
    }

    @Override
    public void save(Collection<Hook> hooks) throws IOException {
        tryCleaningRootDir();
        Files.createDirectories(rootFolder);

        final Set<Hook> saved = new LinkedHashSet<>();
        for(Hook hook : new ArrayList<>(hooks)) {
            hook = hook.folder() != null ?
                    hook :
                    hook.withFolder(createPossiblyDupeDirectory(rootFolder, sanitizePathSegment(hook.device().id())));

            for(Hook.ApplicationHook appHook : new ArrayList<>(hook.applicationHooks())) {
                final var startAppHook = appHook;
                appHook = appHook.folder() != null ?
                        appHook :
                        appHook.withFolder(createPossiblyDupeDirectory(
                                hook.folder(),
                                sanitizePathSegment(appHook.application().process())));

                for(Hook.HookScript script : new ArrayList<>(appHook.scripts())) {
                    if (script.scriptFile() == null) {
                        final var startScript = script;
                        script = script.withScriptFile(createPossiblyDupeFile(
                                appHook.folder(),
                                sanitizePathSegment(script.name()), scriptEngine.getFileExtension()));
                        appHook = appHook.replaceScript(startScript, script);
                    }

                    // Add the utf-8 BOM as AutoHotkey defaults to UTF-8 if it finds it
                    // See https://www.autohotkey.com/boards/viewtopic.php?t=67763
                    Files.createDirectories(script.scriptFile().getParent());
                    Files.writeString(script.scriptFile(), UTF_8_BOM + script.script(), StandardCharsets.UTF_8);
                }

                hook = hook.replaceApplicationHook(startAppHook, appHook);
            }

            try(Writer writer = Files.newBufferedWriter(hook.folder().resolve(HOOK_JSON_FILE_NAME), StandardCharsets.UTF_8)) {
                gson.toJson(hook, writer);
            } catch (JsonIOException ex) {
                throw new IOException("Failed to serialize hook " + hook, ex);
            }

            saved.add(hook);
        }

        this.saved = saved;
        this.unmodifiableSaved = Collections.unmodifiableSet(this.saved);
    }

    private Set<Hook> load() throws IOException {
        final Set<Hook> saved = new LinkedHashSet<>();

        try(var files = Files.list(rootFolder)) {
            for (Iterator<Path> iterator = files.iterator(); iterator.hasNext(); ) {
                final Path hookFolder = iterator.next();
                final Path hookJson = hookFolder.resolve(HOOK_JSON_FILE_NAME);
                if(!Files.exists(hookJson) || Files.isDirectory(hookJson))
                    continue;

                try(Reader reader = Files.newBufferedReader(hookJson, StandardCharsets.UTF_8)) {
                    saved.add(gson
                            .fromJson(reader, Hook.class)
                            .withFolder(hookFolder));
                } catch (JsonIOException ex) {
                    throw new IOException("Failed to deserialize hook in folder " + hookFolder.toAbsolutePath(), ex);
                }
            }
        }

        return saved;
    }

    private void tryCleaningRootDir() {
        for (var hook : saved) {
            final Path hookFolder = hook.folder();
            final String generatedHookFolderName = sanitizePathSegment(hook.device().id());

            for (var appHook : hook.applicationHooks()) {
                final Path appHookFolder = appHook.folder();
                final String generatedAppHookFolderName = sanitizePathSegment(appHook.application().process());

                for (var script : appHook.scripts()) {
                    final Path scriptFile = script.scriptFile();
                    final String generatedScriptFileName = sanitizePathSegment(script.name());

                    if (scriptFile.getParent() != null &&
                            startsWith(scriptFile.getParent().getParent(), rootFolder, generatedHookFolderName) &&
                            nameStartsWith(scriptFile.getParent(), generatedAppHookFolderName) &&
                            nameStartsWith(scriptFile, generatedScriptFileName) &&
                            nameEndsWith(scriptFile, '.' + scriptEngine.getFileExtension())) {
                        try {
                            Files.deleteIfExists(scriptFile);
                        } catch (IOException ex) {
                            LOGGER.error("Failed to delete file {}", scriptFile.toAbsolutePath(), ex);
                        }
                    }
                }

                if (startsWith(appHookFolder.getParent(), rootFolder, generatedHookFolderName) &&
                        nameStartsWith(appHookFolder, generatedAppHookFolderName))
                    deleteFolderIfEmptyOrLog(appHookFolder);
            }

            final Path hookJson = hookFolder.resolve(HOOK_JSON_FILE_NAME);
            try {
                Files.deleteIfExists(hookJson);
            } catch (IOException ex) {
                LOGGER.error("Failed to delete file {}", hookJson.toAbsolutePath(), ex);
            }

            if (startsWith(hookFolder, rootFolder, sanitizePathSegment(hook.device().id())))
                deleteFolderIfEmptyOrLog(hookFolder);
        }
    }

    private void deleteFolderIfEmptyOrLog(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (DirectoryNotEmptyException ex) {
            // Ignored
        } catch (IOException ex) {
            LOGGER.error("Failed to delete folder {}", path.toAbsolutePath(), ex);
        }
    }

    private static Path createPossiblyDupeDirectory(Path parent, String folderName) throws IOException {
        Path appFolder = parent.resolve(folderName);
        for(int i = 1; ; i++) {
            try {
                Files.createDirectory(appFolder);
                break;
            } catch (FileAlreadyExistsException ignored) {
            }

            appFolder = parent.resolve(folderName + " (" + i + ')');
        }

        return appFolder;
    }

    private static Path createPossiblyDupeFile(Path parent, String fileName, String extension) throws IOException {
        Path appFolder = parent.resolve(fileName + '.' + extension);
        for(int i = 1; ; i++) {
            try {
                Files.createFile(appFolder);
                break;
            } catch (FileAlreadyExistsException ignored) {
            }

            appFolder = parent.resolve(fileName + " (" + i + ")." + extension);
        }

        return appFolder;
    }

    @SuppressWarnings("RegExpRedundantEscape")
    private static String sanitizePathSegment(String segment) {
        // Make it safe, replace anything that isn't basic characters
        return segment.replaceAll("[^a-zA-Z0-9 \\-_\\#.]", "");
    }

    private static boolean startsWith(Path path, Path parent, String name) {
        return path != null && path.getParent().equals(parent) && nameStartsWith(path, name);
    }

    private static boolean nameStartsWith(Path path, String name) {
        return path != null && path.getFileName().toString().startsWith(name);
    }

    private static boolean nameEndsWith(Path path, String name) {
        return path != null && path.getFileName().toString().endsWith(name);
    }
}
