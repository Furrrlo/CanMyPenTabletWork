package me.ferlo.cmptw.script;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

public interface ScriptEnvironment<T extends ScriptEngine> {

    static ScriptEnvironment<?> create() {
        final List<ScriptEnvironmentProvider> services = ServiceLoader.load(ScriptEnvironmentProvider.class).stream()
                .map(ServiceLoader.Provider::get)
                .filter(ScriptEnvironmentProvider::isSupported)
                .toList();

        if(services.isEmpty())
            throw new IllegalStateException("Couldn't find any supported ScriptEnvironmentProvider");
        if(services.size() > 1)
            throw new IllegalStateException("Multiple valid ScriptEnvironmentProvider found, don't know which one to choose");
        return services.get(0).create();
    }

    Optional<T> discoverEngine();
}
