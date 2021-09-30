package me.ferlo.cmptw.window;

import com.sun.jna.platform.win32.WinDef.HWND;
import me.ferlo.cmptw.raw.RawInputException;

public interface WindowService {

    WindowService INSTANCE = new WindowServiceImpl();

    void register() throws RawInputException;

    void unregister() throws RawInputException;

    HWND getHwnd();

    void addListener(int uMsg, WindowMessageListener listener);

    void removeListener(int uMsg);

    void removeListener(WindowMessageListener listener);

    void removeListener(int uMsg, WindowMessageListener listener);

    void peekMessages(int wMsgFilterMin, int wMsgFilterMax);
}
