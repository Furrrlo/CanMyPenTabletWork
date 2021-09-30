package me.ferlo.cmptw.hook;

import me.ferlo.cmptw.global.GlobalKeyEvent;
import me.ferlo.cmptw.raw.RawInputDevice;
import me.ferlo.cmptw.raw.RawKeyEvent;

/**
 * Extends JNativeHook event to add keyboard information fetched by raw input,
 * such as the device which pressed the key.
 */
public record KeyboardHookEvent(
        RawInputDevice rawInputDevice,
        int vKeyCode,
        int repeatCount,
        int scanCode,
        boolean isExtendedKey,
        boolean isAltPressed,
        boolean wasKeyDown,
        boolean isKeyDown
) {

    KeyboardHookEvent(RawKeyEvent rawEvt, GlobalKeyEvent nativeEvt) {
        this(
                rawEvt.device(),
                nativeEvt.vKeyCode(),
                nativeEvt.repeatCount(),
                nativeEvt.scanCode(),
                nativeEvt.isExtendedKey(),
                nativeEvt.isAltPressed(),
                nativeEvt.wasKeyDown(),
                nativeEvt.isKeyDown()
        );
    }
}
