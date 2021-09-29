package me.ferlo.cmptw.raw;

public interface RawKeyboardInputService {

    RawKeyboardInputService INSTANCE = new RawKeyboardInputServiceImpl();

    void register() throws RawInputException;

    void unregister() throws RawInputException;

    void addListener(RawInputKeyListener listener);

    void removeListener(RawInputKeyListener listener);
}
