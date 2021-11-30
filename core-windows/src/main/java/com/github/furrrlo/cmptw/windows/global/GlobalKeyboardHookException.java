package com.github.furrrlo.cmptw.windows.global;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Win32Exception;

public class GlobalKeyboardHookException extends RuntimeException {

    GlobalKeyboardHookException() {
        this(Kernel32.INSTANCE.GetLastError());
    }

    GlobalKeyboardHookException(int code) {
        super(new Win32Exception(code));
    }

    GlobalKeyboardHookException(String message) {
        super(message);
    }

    GlobalKeyboardHookException(String message, Throwable cause) {
        super(message, cause);
    }
}
