package me.ferlo.cmptw.global;

/**
 * See <a>https://docs.microsoft.com/en-us/previous-versions/windows/desktop/legacy/ms644984(v=vs.85)</a>
 *
 * @param vKeyCode      The virtual-key code of the key that generated the keystroke message.
 * @param repeatCount   The repeat count. The value is the number of times the keystroke is repeated as a result of
 *                      the user's holding down the key.
 * @param scanCode      The scan code. The value depends on the OEM.
 * @param isExtendedKey Indicates whether the key is an extended key, such as a function key or a key on the numeric
 *                      keypad. The value is 1 if the key is an extended key; otherwise, it is 0.
 * @param isAltPressed  The context code. The value is 1 if the ALT key is down; otherwise, it is 0.
 * @param wasKeyDown    The previous key state. The value is 1 if the key is down before the message is sent;
 *                      it is 0 if the key is up.
 * @param isKeyDown    The transition state. The value is 0 if the key is being pressed and 1 if it is being released.
 */
public record GlobalKeyEvent(int vKeyCode,
                             int repeatCount,
                             int scanCode,
                             boolean isExtendedKey,
                             boolean isAltPressed,
                             boolean wasKeyDown,
                             boolean isKeyDown) {
}
