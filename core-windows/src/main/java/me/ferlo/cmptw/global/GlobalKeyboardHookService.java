package me.ferlo.cmptw.global;

import me.ferlo.cmptw.window.WindowService;

public interface GlobalKeyboardHookService {

    static GlobalKeyboardHookService create(WindowService windowService) {
        return new GlobalKeyboardHookServiceImpl(windowService);
    }

    void register() throws Exception;

    void unregister() throws Exception;

    void addListener(GlobalKeyboardHookListener listener);

    void removeListener(GlobalKeyboardHookListener listener);
}
