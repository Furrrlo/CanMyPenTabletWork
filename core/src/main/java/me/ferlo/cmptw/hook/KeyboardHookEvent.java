package me.ferlo.cmptw.hook;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * @param modifiers      the extended modifier mask for this event.
 *                       <p>
 *                       These are NOT the AWT modifiers.
 *                       <p>
 *                       Modifiers represent the state of all modal keys,
 *                       such as ALT, CTRL, META, and the mouse buttons just after
 *                       the event occurred.
 *                       <p>
 *                       For example, if the user presses <b>button 1</b> followed by
 *                       <b>button 2</b>, and then releases them in the same order,
 *                       the following sequence of events is generated:
 *                       <PRE>
 *                       {@code MOUSE_PRESSED}:  {@code BUTTON1_DOWN_MASK}
 *                       {@code MOUSE_PRESSED}:  {@code BUTTON1_DOWN_MASK | BUTTON2_DOWN_MASK}
 *                       {@code MOUSE_RELEASED}: {@code BUTTON2_DOWN_MASK}
 *                       {@code MOUSE_CLICKED}:  {@code BUTTON2_DOWN_MASK}
 *                       {@code MOUSE_RELEASED}:
 *                       {@code MOUSE_CLICKED}:
 *                       </PRE>
 *                       <p>
 *                       It is not recommended to compare the return value of this method
 *                       using {@code ==} because new modifiers can be added in the future.
 *                       For example, the appropriate way to check that SHIFT and BUTTON1 are
 *                       down, but CTRL is up is demonstrated by the following code:
 *                       <PRE>
 *                       int onmask = SHIFT_DOWN_MASK | BUTTON1_DOWN_MASK;
 *                       int offmask = CTRL_DOWN_MASK;
 *                       if ((event.getModifiersEx() &amp; (onmask | offmask)) == onmask) {
 *                       ...
 *                       }
 *                       </PRE>
 *                       The above code will work even if new modifiers are added.
 * @param awtKeyCode     The unique value assigned by AWT to each of the keys on the keyboard.
 *                       There is a common set of key codes that  can be fired by most keyboards.
 *                       The symbolic name for a key code should be used rather  than the code value itself.
 * @param awtKeyLocation The location of the key on the keyboard as defined by AWT.
 *                       <p>
 *                       Some keys occur more than once on a keyboard, e.g. the left and
 *                       right shift keys.  Additionally, some keys occur on the numeric
 *                       keypad.  This variable is used to distinguish such keys.
 *                       <p>
 *                       The only legal values are {@code KEY_LOCATION_UNKNOWN},
 *                       {@code KEY_LOCATION_STANDARD}, {@code KEY_LOCATION_LEFT},
 *                       {@code KEY_LOCATION_RIGHT}, and {@code KEY_LOCATION_NUMPAD}.
 */
public record KeyboardHookEvent(
        int pid,
        KeyboardHookDevice device,
        int vKeyCode,
        int scanCode,
        boolean isExtendedKey,
        int modifiers,
        int awtKeyCode,
        int awtKeyLocation,
        boolean wasKeyDown,
        boolean isKeyDown,
        int repeatCount
) {

    public static int LSHIFT_MASK = 0x1;
    public static int RSHIFT_MASK = 0x2;

    public static int LCTRL_MASK = 0x4;
    public static int RCTRL_MASK = 0x8;

    public static int LMETA_MASK = 0x10;
    public static int RMETA_MASK = 0x20;

    public static int LALT_MASK = 0x40;
    public static int RALT_MASK = 0x80;

    public static int NUM_LOCK_MASK = 0x100;
    public static int CAPS_LOCK_MASK = 0x200;
    public static int SCROLL_LOCK_MASK = 0x400;

    public static int SHIFT_MASK = LSHIFT_MASK | RSHIFT_MASK;
    public static int CTRL_MASK = LCTRL_MASK | RCTRL_MASK;
    public static int META_MASK = LMETA_MASK | RMETA_MASK;
    public static int ALT_MASK = LALT_MASK | RALT_MASK;

    /**
     * Returns whether or not the AltGraph modifier is down on this event.
     * @return whether or not the AltGraph modifier is down on this event
     */
    public boolean isModifierKey() {
        return switch (awtKeyCode) {
            case KeyEvent.VK_SHIFT,
                    KeyEvent.VK_CONTROL,
                    KeyEvent.VK_ALT, KeyEvent.VK_ALT_GRAPH,
                    KeyEvent.VK_META, KeyEvent.VK_NUM_LOCK,
                    KeyEvent.VK_CAPS_LOCK, KeyEvent.VK_SCROLL_LOCK -> true;
            default -> false;
        };
    }

    /**
     * Returns whether or not the Shift modifier is down on this event.
     * @return whether or not the Shift modifier is down on this event
     */
    public boolean isShiftDown() {
        return (modifiers & SHIFT_MASK) != 0;
    }

    /**
     * Returns whether or not the Control modifier is down on this event.
     * @return whether or not the Control modifier is down on this event
     */
    public boolean isControlDown() {
        return (modifiers & CTRL_MASK) != 0;
    }

    /**
     * Returns whether or not the Meta modifier is down on this event.
     * @return whether or not the Meta modifier is down on this event
     */
    public boolean isMetaDown() {
        return (modifiers & META_MASK) != 0;
    }

    /**
     * Returns whether or not the Alt modifier is down on this event.
     * @return whether or not the Alt modifier is down on this event
     */
    public boolean isAltDown() {
        return (modifiers & ALT_MASK) != 0;
    }

    /**
     * Returns whether or not the AltGraph modifier is down on this event.
     * @return whether or not the AltGraph modifier is down on this event
     */
    public boolean isAltGraphDown() {
        return (modifiers & RALT_MASK) != 0;
    }

    public String getModifiersText() {
        return getModifiersText(modifiers);
    }

    public String getModifiersText(String separator) {
        return getModifiersText(modifiers, separator);
    }

    public static String getModifiersText(int modifiers) {
        return getModifiersText(modifiers, "+");
    }

    public static String getModifiersText(int modifiers, String separator) {
        StringBuilder buf = new StringBuilder();
        if ((modifiers & META_MASK) != 0) {
            buf.append((modifiers & LMETA_MASK) != 0 ? "Left " : "Right ");
            buf.append(Toolkit.getProperty("AWT.meta", "Meta"));
            buf.append(separator);
        }
        if ((modifiers & CTRL_MASK) != 0) {
            buf.append((modifiers & LCTRL_MASK) != 0 ? "Left " : "Right ");
            buf.append(Toolkit.getProperty("AWT.control", "Ctrl"));
            buf.append(separator);
        }
        if ((modifiers & LALT_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.alt", "Alt"));
            buf.append(separator);
        }
        if ((modifiers & RALT_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.altGraph", "Alt Graph"));
            buf.append(separator);
        }
        if ((modifiers & SHIFT_MASK) != 0) {
            buf.append((modifiers & LSHIFT_MASK) != 0 ? "Left " : "Right ");
            buf.append(Toolkit.getProperty("AWT.shift", "Shift"));
            buf.append(separator);
        }
        if ((modifiers & CAPS_LOCK_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.capsLock", "Caps Lock"));
            buf.append(separator);
        }
        if ((modifiers & NUM_LOCK_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.numLock", "Num Lock"));
            buf.append(separator);
        }
        if ((modifiers & SCROLL_LOCK_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.scrollLock", "Scroll Lock"));
            buf.append(separator);
        }

        if (buf.length() > 0) {
            buf.setLength(buf.length() - separator.length()); // remove trailing '+'
        }
        return buf.toString();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(100);

        str.append(isKeyDown ? "KEY_PRESSED" : "KEY_RELEASED");

        if (isKeyDown && wasKeyDown) str.append(",repeated");
        str.append(",repeatCount=").append(repeatCount);

        str.append(",device").append(device.getId());

        str.append(",awtKeyCode=").append(awtKeyCode);
        str.append(",awtKeyText=").append(KeyEvent.getKeyText(awtKeyCode));

        if (modifiers != 0)
            str.append(",modifiers=").append(getModifiersText());

        str.append(",keyLocation=");
        str.append(switch (awtKeyLocation) {
            case KeyEvent.KEY_LOCATION_STANDARD -> "KEY_LOCATION_STANDARD";
            case KeyEvent.KEY_LOCATION_LEFT -> "KEY_LOCATION_LEFT";
            case KeyEvent.KEY_LOCATION_RIGHT -> "KEY_LOCATION_RIGHT";
            case KeyEvent.KEY_LOCATION_NUMPAD -> "KEY_LOCATION_NUMPAD";
            default -> "KEY_LOCATION_UNKNOWN";
        });
        str.append(",vKeyCode=").append(vKeyCode);
        str.append(",scancode=").append(scanCode);
        str.append(",isExtendedKey=").append(isExtendedKey);

        return "KeyboardHookEvent[" + str + "]";
    }
}
