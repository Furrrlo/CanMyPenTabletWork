package me.ferlo.cmptw.hook;

import java.util.Collection;

public interface KeyboardHookService {

    void register() throws Exception;

    void unregister() throws Exception;

    void addListener(KeyboardHookListener listener);

    void removeListener(KeyboardHookListener listener);

    Collection<KeyboardHookDevice> getDevices();
}
