package me.ferlo.cmptw.ahk;

import com.google.auto.service.AutoService;
import me.ferlo.cmptw.script.ScriptEnvironment;
import me.ferlo.cmptw.script.ScriptEnvironmentProvider;

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
