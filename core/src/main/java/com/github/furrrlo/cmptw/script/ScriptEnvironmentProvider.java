package com.github.furrrlo.cmptw.script;

public interface ScriptEnvironmentProvider {

    boolean isSupported();

    ScriptEnvironment<?> create();
}
