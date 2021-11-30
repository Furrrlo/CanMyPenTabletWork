package com.github.furrrlo.cmptw.windows.raw;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;

public class RawInputException extends RuntimeException {

    private final int lastErrorCode;

    RawInputException(String msg) {
        super(msg);
        this.lastErrorCode = -1;
    }

    RawInputException(String msg, Throwable cause) {
        super(msg, cause);
        this.lastErrorCode = -1;
    }

    RawInputException() {
        this(Kernel32.INSTANCE.GetLastError());
    }

    RawInputException(int lastErrorCode) {
        super(Kernel32Util.formatMessageFromLastErrorCode(lastErrorCode));
        this.lastErrorCode = lastErrorCode;
    }

    public int getLastErrorCode() {
        return lastErrorCode;
    }
}
