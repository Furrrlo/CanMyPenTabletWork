package com.github.furrrlo.cmptw.windows.global;

import com.github.furrrlo.cmptw.windows.window.WindowService;

public interface GlobalKeyboardHookService {

    static GlobalKeyboardHookService create(WindowService windowService) {
        return new GlobalKeyboardHookServiceImpl(windowService);
    }

    void register() throws Exception;

    void unregister() throws Exception;

    void addListener(GlobalKeyboardHookListener listener);

    void removeListener(GlobalKeyboardHookListener listener);
}
