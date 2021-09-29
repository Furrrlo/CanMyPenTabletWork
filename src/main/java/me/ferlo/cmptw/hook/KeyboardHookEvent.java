package me.ferlo.cmptw.hook;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import me.ferlo.cmptw.raw.RawInputDevice;
import me.ferlo.cmptw.raw.RawKeyboardInputEvent;

/**
 * Extends JNativeHook event to add keyboard information fetched by raw input,
 * such as the device which pressed the key.
 */
public class KeyboardHookEvent extends NativeKeyEvent {

    private final RawInputDevice rawInputDevice;
    private final boolean isKeyPress;
    private boolean cancelled;

    KeyboardHookEvent(RawKeyboardInputEvent rawEvt, NativeKeyEvent nativeEvt, boolean isKeyPress) {
        super(nativeEvt.getID(), nativeEvt.getModifiers(), nativeEvt.getRawCode(), nativeEvt.getKeyCode(), nativeEvt.getKeyChar(), nativeEvt.getKeyLocation());
        rawInputDevice = rawEvt.device();
        this.isKeyPress = isKeyPress;
    }

    public RawInputDevice getRawInputDevice() {
        return rawInputDevice;
    }

    public boolean isKeyPress() {
        return isKeyPress;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public String toString() {
        return "KeyboardHookEvent{" +
                "rawInputDevice=" + rawInputDevice.hwid() +
                ", isKeyPress=" + isKeyPress +
                ", cancelled=" + cancelled +
                "} " + super.paramString();
    }
}
