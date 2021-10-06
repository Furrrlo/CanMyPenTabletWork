package me.ferlo.cmptw.window;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Win32Exception;

public class WindowException extends RuntimeException {

    WindowException() {
        this(Kernel32.INSTANCE.GetLastError());
    }

    WindowException(int code) {
        super(new Win32Exception(code));
    }

    WindowException(String message) {
        super(message);
    }

    WindowException(String message, Throwable cause) {
        super(message, cause);
    }
}
