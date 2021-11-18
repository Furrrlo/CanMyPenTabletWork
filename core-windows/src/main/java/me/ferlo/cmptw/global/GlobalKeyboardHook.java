package me.ferlo.cmptw.global;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;

public interface GlobalKeyboardHook extends Library {

    GlobalKeyboardHook INSTANCE = Native.load("global_keyboard_hook", GlobalKeyboardHook.class);

    int HC_ACTION = 0;
    int HC_NOREMOVE = 3;

    int WH_HOOK = 32769;
    int WH_HOOK_ACTION = WH_HOOK + HC_ACTION;
    int WH_HOOK_NOREMOVE = WH_HOOK + HC_NOREMOVE;

    boolean StartHook(HWND hWnd);

    boolean StopHook();
}
