package com.github.furrrlo.cmptw.windows.window;

import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;

public interface WindowMessageListener {

    LRESULT wndProc(HWND hwnd, int uMsg, WPARAM wParam, LPARAM lParam);
}
