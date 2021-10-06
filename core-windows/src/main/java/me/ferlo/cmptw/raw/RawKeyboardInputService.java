package me.ferlo.cmptw.raw;

import me.ferlo.cmptw.window.WindowService;

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
