package me.ferlo.cmptw.hook;

public interface KeyboardHookService {

    void register() throws Exception;

    void unregister() throws Exception;

    void addListener(KeyboardHookListener listener);

    void removeListener(KeyboardHookListener listener);
}
