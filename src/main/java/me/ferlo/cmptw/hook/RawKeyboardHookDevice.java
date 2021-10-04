package me.ferlo.cmptw.hook;

import me.ferlo.cmptw.raw.RawInputDevice;

class RawKeyboardHookDevice implements KeyboardHookDevice {

    private final RawInputDevice rawDevice;
    private int modifiers;

    public RawKeyboardHookDevice(RawInputDevice rawDevice) {
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
