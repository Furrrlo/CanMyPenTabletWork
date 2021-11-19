package me.ferlo.cmptw.script;

import java.nio.file.Path;
import java.util.Optional;

public interface ExecutableScriptEnvironment extends ScriptEnvironment<ExecutableScriptEngine> {

    ExecutableScriptEngine createEngineForExecutable(Path binary) throws IllegalArgumentException;

    @Override
    Optional<ExecutableScriptEngine> discoverEngine();
}
