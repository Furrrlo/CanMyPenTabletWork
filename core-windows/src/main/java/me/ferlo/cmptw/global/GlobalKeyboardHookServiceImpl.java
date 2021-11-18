package me.ferlo.cmptw.global;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import me.ferlo.cmptw.window.WindowService;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import static me.ferlo.cmptw.global.NehKbdHook.WH_HOOK;

class GlobalKeyboardHookServiceImpl implements GlobalKeyboardHookService {

    private final WindowService windowService;
    private final Object lock = new Object();

    private volatile boolean registered;
    private final Collection<GlobalKeyboardHookListener> listeners = ConcurrentHashMap.newKeySet();

    GlobalKeyboardHookServiceImpl(WindowService windowService) {
        this.windowService = windowService;
    }

    @Override
    public void register() throws Exception {
        if(registered)
            return;

        synchronized (lock) {
            if(registered)
                return;

            if(!NehKbdHook.INSTANCE.StartHook(windowService.getHwnd()))
                throw new GlobalKeyboardHookException(
                        "Failed to register GlobalKeyboardHookServiceImpl",
                        new Win32Exception(Kernel32.INSTANCE.GetLastError()));
            windowService.addListener(WH_HOOK, this::wndProc);
            registered = true;
        }
    }

    @Override
    public void unregister() throws Exception {
        if(!registered)
            return;

        synchronized (lock) {
            if (!registered)
                return;

            windowService.removeListener(WH_HOOK, this::wndProc);
            if(!NehKbdHook.INSTANCE.StopHook())
                throw new GlobalKeyboardHookException(
                        "Failed to unregister GlobalKeyboardHookServiceImpl",
                        new Win32Exception(Kernel32.INSTANCE.GetLastError()));
            registered = false;
        }
    }

    @Override
    public void addListener(GlobalKeyboardHookListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(GlobalKeyboardHookListener listener) {
        listeners.remove(listener);
    }

    private LRESULT wndProc(HWND hwnd, int uMsg, WPARAM wparam, LPARAM lparam) {
        final int vKeyCode = wparam.intValue();
        final int flags = lparam.intValue();
        final GlobalKeyEvent evt = new GlobalKeyEvent(
                vKeyCode,
                flags & 0xFFFF, // 0-15
                (flags >> 16) & 0xFF, // 16-23
                ((flags >> 24) & 0x1) == 1, // 24
                ((flags >> 29) & 0x1) == 1, // 29
                ((flags >> 30) & 0x1) == 1, // 30
                ((flags >> 31) & 0x1) == 0 // 31
        );

        for(var listener : listeners)
            if(listener.onGlobalKeyEvent(evt))
                return new LRESULT(1);
        return new LRESULT(0);
    }
}
