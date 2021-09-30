package me.ferlo.cmptw.raw;

import java.util.List;

public interface RawKeyboardInputService {

    RawKeyboardInputService INSTANCE = new RawKeyboardInputServiceImpl();

    void register() throws RawInputException;

    void unregister() throws RawInputException;

    void addListener(RawInputKeyListener listener);

    void removeListener(RawInputKeyListener listener);

    List<RawKeyboardInputEvent> peek();
}
