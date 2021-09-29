package me.ferlo.cmptw.raw;

import com.sun.jna.platform.win32.WinDef;

/**
 * @param makeCode Specifies the scan code (from Scan Code Set 1) associated with a key press. See Remarks.
 * @param flags Flags for scan code information. It can be one or more of the following:
 *              - RI_KEY_MAKE 0: The key is down.
 *              - RI_KEY_BREAK 1: The key is up.
 *             - RI_KEY_E0 2: The scan code has the E0 prefix.
 *             - RI_KEY_E1 4: The scan code has the E1 prefix.
 * @param reserved Reserved; must be zero.
 * @param vKey The corresponding legacy virtual-key code.
 * @param message The corresponding legacy keyboard window message, for example WM_KEYDOWN, WM_SYSKEYDOWN, and so forth.
 * @param extraInformation The device-specific additional information for the event.
 */
public record RawKeyboardInputEvent(RawInputDevice device,
                                    State keyState,
                                    int makeCode,
                                    int flags,
                                    int reserved,
                                    int vKey,
                                    int message,
                                    WinDef.ULONG extraInformation) {

    public enum State { UP, DOWN }
}
