package com.github.furrrlo.cmptw.windows.hook;

import com.github.furrrlo.cmptw.hook.KeyboardHookDevice;
import com.github.furrrlo.cmptw.windows.raw.RawInputDevice;

class WinRawKeyboardHookDevice implements KeyboardHookDevice {

    private final RawInputDevice rawDevice;
    private int modifiers;

    public WinRawKeyboardHookDevice(RawInputDevice rawDevice) {
        this.rawDevice = rawDevice;
    }

    @Override
    public String getId() {
        return rawDevice.hwid();
    }

    @Override
    public String getDesc() {
        return rawDevice.name();
    }

    int getModifiersMask() {
        return modifiers;
    }

    void setModifiersMask(int modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public String toString() {
        return "RawKeyboardHookDevice{" +
                "rawDevice=" + rawDevice +
                ", modifiers=" + modifiers +
                '}';
    }
}
