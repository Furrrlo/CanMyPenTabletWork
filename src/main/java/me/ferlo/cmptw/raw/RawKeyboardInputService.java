package me.ferlo.cmptw.raw;

import me.ferlo.cmptw.window.WindowService;

import java.util.Collection;
import java.util.List;

public interface RawKeyboardInputService {

    RawKeyboardInputService INSTANCE = new RawKeyboardInputServiceImpl(WindowService.INSTANCE);

    void register() throws RawInputException;

    void unregister() throws RawInputException;

    void addListener(RawInputKeyListener listener);

    void removeListener(RawInputKeyListener listener);

    Collection<RawInputDevice> getDevices();

    List<RawKeyEvent> peek();
}
