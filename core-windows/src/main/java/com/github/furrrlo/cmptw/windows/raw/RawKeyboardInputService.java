package com.github.furrrlo.cmptw.windows.raw;

import com.github.furrrlo.cmptw.windows.window.WindowService;

import java.util.Collection;

public interface RawKeyboardInputService {

    static RawKeyboardInputService create(WindowService windowService) {
        return new RawKeyboardInputServiceImpl(windowService);
    }

    void register() throws RawInputException;

    void unregister() throws RawInputException;

    void addListener(RawInputKeyListener listener);

    void removeListener(RawInputKeyListener listener);

    Collection<RawInputDevice> getDevices();
}
