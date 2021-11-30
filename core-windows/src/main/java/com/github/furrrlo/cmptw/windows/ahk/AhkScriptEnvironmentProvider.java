package com.github.furrrlo.cmptw.windows.ahk;

import com.google.auto.service.AutoService;
import com.github.furrrlo.cmptw.script.ScriptEnvironment;
import com.github.furrrlo.cmptw.script.ScriptEnvironmentProvider;

@AutoService(ScriptEnvironmentProvider.class)
public class AhkScriptEnvironmentProvider implements ScriptEnvironmentProvider {

    @Override
    public boolean isSupported() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    @Override
    public ScriptEnvironment<?> create() {
        return new AhkScriptEnvironment();
    }
}
