package com.github.furrrlo.cmptw.script;

import java.nio.file.Path;

public interface ExecutableScriptEngine extends ScriptEngine {

    Path getExecutable();
}
