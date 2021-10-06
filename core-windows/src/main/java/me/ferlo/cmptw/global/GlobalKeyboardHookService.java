package me.ferlo.cmptw.global;

import me.ferlo.cmptw.window.WindowService;

public interface GlobalKeyboardHookService {

    GlobalKeyboardHookService INSTANCE = new GlobalKeyboardHookServiceImpl(WindowService.INSTANCE);

    void register() throws Exception;

    void unregister() throws Exception;

    void addListener(GlobalKeyboardHookListener listener);

    void removeListener(GlobalKeyboardHookListener listener);
}
