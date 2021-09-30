package me.ferlo.cmptw.global;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;

public interface NehKbdHook extends Library {

    NehKbdHook INSTANCE = Native.load("NEHKbdhook", NehKbdHook.class);

    int WH_HOOK = 32769;

    boolean StartHook(HWND hWnd);

    boolean StopHook();
}
