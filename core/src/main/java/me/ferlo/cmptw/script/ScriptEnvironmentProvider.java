package me.ferlo.cmptw.script;

public interface ScriptEnvironmentProvider {

    boolean isSupported();

    ScriptEnvironment<?> create();
}
