package me.ferlo.cmptw.window;

import com.sun.jna.platform.win32.WinDef.HWND;
import me.ferlo.cmptw.raw.RawInputException;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public interface WindowService {

    static WindowService create() {
        return new WindowServiceImpl();
    }

    void register() throws RawInputException;

    void unregister() throws RawInputException;

    HWND getHwnd();

    void addListener(int uMsg, WindowMessageListener listener);

    void removeListener(int uMsg);

    void removeListener(WindowMessageListener listener);

    void removeListener(int uMsg, WindowMessageListener listener);

    CompletableFuture<Void> runOnPumpThread(Runnable runnable);

    <T> CompletableFuture<T> callOnPumpThread(Callable<T> callable);

    void peekMessages(int wMsgFilterMin, int wMsgFilterMax);
}
