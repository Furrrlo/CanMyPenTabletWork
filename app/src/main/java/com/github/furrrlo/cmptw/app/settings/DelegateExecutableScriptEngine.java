package com.github.furrrlo.cmptw.app.settings;

import com.github.furrrlo.cmptw.script.ExecutableScriptEngine;

import java.nio.file.Path;

abstract class DelegateExecutableScriptEngine extends DelegateScriptEngine implements ExecutableScriptEngine {

    @Override
    abstract ExecutableScriptEngine getDelegate();

    @Override
    public Path getExecutable() {
        return getDelegate().getExecutable();
    }
}
