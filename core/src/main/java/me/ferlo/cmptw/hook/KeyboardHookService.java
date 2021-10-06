package me.ferlo.cmptw.hook;

import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

public interface KeyboardHookService {

    static KeyboardHookService create() {
        final List<KeyboardHookServiceProvider> services = ServiceLoader.load(KeyboardHookServiceProvider.class).stream()
                .map(ServiceLoader.Provider::get)
                .filter(KeyboardHookServiceProvider::isSupported)
                .toList();

        if(services.isEmpty())
            throw new IllegalStateException("Couldn't find any supported KeyboardHookServiceProvider");
        if(services.size() > 1)
            throw new IllegalStateException("Multiple valid KeyboardHookServiceProvider found, don't know which one to choose");
        return services.get(0).create();
    }

    void register() throws Exception;

    void unregister() throws Exception;

    void addListener(KeyboardHookListener listener);

    void removeListener(KeyboardHookListener listener);

    Collection<KeyboardHookDevice> getDevices();
}
