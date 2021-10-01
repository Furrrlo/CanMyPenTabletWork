package me.ferlo.cmptw.hook;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WTypes.LPWSTR;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef.HKL;
import com.sun.jna.platform.win32.WinDef.LCID;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.W32APITypeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import static com.sun.jna.platform.win32.WinNT.*;
import static com.sun.jna.platform.win32.WinUser.MAPVK_VK_TO_VSC;
import static com.sun.jna.win32.W32APIOptions.DEFAULT_OPTIONS;

public class WinToAwtHelper implements WinVK {

    private static final Logger LOGGER = LoggerFactory.getLogger(WinToAwtHelper.class);

    private WinToAwtHelper() {
    }

    /**
     * @implNote See <a>https://github.com/openjdk/jdk/blob/9bc023220fbbb0b6ea1ed1a0ca2aa3848764e8cd/src/java.desktop/windows/native/libawt/windows/awt_Component.cpp#L2807 </a>
     */
    @SuppressWarnings("RedundantTypeArguments") // IntelliJ says it slows down analysys
    private static final Map<Integer, Integer> KEY_MAP_TABLE = Map.<Integer, Integer>ofEntries(
            // Modifier keys
            Map.entry(VK_CAPITAL, KeyEvent.VK_CAPS_LOCK),
            Map.entry(VK_SHIFT, KeyEvent.VK_SHIFT),
            Map.entry(VK_CONTROL, KeyEvent.VK_CONTROL),
            Map.entry(VK_MENU, KeyEvent.VK_ALT),
            Map.entry(VK_RMENU, KeyEvent.VK_ALT_GRAPH),
            Map.entry(VK_NUMLOCK, KeyEvent.VK_NUM_LOCK),

            // Miscellaneous Windows keys
            Map.entry(VK_LWIN, KeyEvent.VK_WINDOWS),
            Map.entry(VK_RWIN, KeyEvent.VK_WINDOWS),
            Map.entry(VK_APPS, KeyEvent.VK_CONTEXT_MENU),

            // Alphabet
            Map.entry(VK_A, KeyEvent.VK_A),
            Map.entry(VK_B, KeyEvent.VK_B),
            Map.entry(VK_C, KeyEvent.VK_C),
            Map.entry(VK_D, KeyEvent.VK_D),
            Map.entry(VK_E, KeyEvent.VK_E),
            Map.entry(VK_F, KeyEvent.VK_F),
            Map.entry(VK_G, KeyEvent.VK_G),
            Map.entry(VK_H, KeyEvent.VK_H),
            Map.entry(VK_I, KeyEvent.VK_I),
            Map.entry(VK_J, KeyEvent.VK_J),
            Map.entry(VK_K, KeyEvent.VK_K),
            Map.entry(VK_L, KeyEvent.VK_L),
            Map.entry(VK_M, KeyEvent.VK_M),
            Map.entry(VK_N, KeyEvent.VK_N),
            Map.entry(VK_O, KeyEvent.VK_O),
            Map.entry(VK_P, KeyEvent.VK_P),
            Map.entry(VK_Q, KeyEvent.VK_Q),
            Map.entry(VK_R, KeyEvent.VK_R),
            Map.entry(VK_S, KeyEvent.VK_S),
            Map.entry(VK_T, KeyEvent.VK_T),
            Map.entry(VK_U, KeyEvent.VK_U),
            Map.entry(VK_V, KeyEvent.VK_V),
            Map.entry(VK_W, KeyEvent.VK_W),
            Map.entry(VK_X, KeyEvent.VK_X),
            Map.entry(VK_Y, KeyEvent.VK_Y),
            Map.entry(VK_Z, KeyEvent.VK_Z),

            // Standard numeric row
            Map.entry(VK_0, KeyEvent.VK_0),
            Map.entry(VK_1, KeyEvent.VK_1),
            Map.entry(VK_2, KeyEvent.VK_2),
            Map.entry(VK_3, KeyEvent.VK_3),
            Map.entry(VK_4, KeyEvent.VK_4),
            Map.entry(VK_5, KeyEvent.VK_5),
            Map.entry(VK_6, KeyEvent.VK_6),
            Map.entry(VK_7, KeyEvent.VK_7),
            Map.entry(VK_8, KeyEvent.VK_8),
            Map.entry(VK_9, KeyEvent.VK_9),

            // Misc key from main block
            Map.entry(VK_RETURN, KeyEvent.VK_ENTER),
            Map.entry(VK_SPACE, KeyEvent.VK_SPACE),
            Map.entry(VK_BACK, KeyEvent.VK_BACK_SPACE),
            Map.entry(VK_TAB, KeyEvent.VK_TAB),
            Map.entry(VK_ESCAPE, KeyEvent.VK_ESCAPE),

            // NumPad with NumLock off & extended block (rectangular)
            Map.entry(VK_INSERT, KeyEvent.VK_INSERT),
            Map.entry(VK_DELETE, KeyEvent.VK_DELETE),
            Map.entry(VK_HOME, KeyEvent.VK_HOME),
            Map.entry(VK_END, KeyEvent.VK_END),
            Map.entry(VK_PRIOR, KeyEvent.VK_PAGE_UP),
            Map.entry(VK_NEXT, KeyEvent.VK_PAGE_DOWN),
            Map.entry(VK_CLEAR, KeyEvent.VK_CLEAR),     // NumPad 5

            // NumPad with NumLock off & extended arrows block (triangular)
            Map.entry(VK_LEFT, KeyEvent.VK_LEFT),
            Map.entry(VK_RIGHT, KeyEvent.VK_RIGHT),
            Map.entry(VK_UP, KeyEvent.VK_UP),
            Map.entry(VK_DOWN, KeyEvent.VK_DOWN),

            // NumPad with NumLock on: numbers
            Map.entry(VK_NUMPAD0, KeyEvent.VK_NUMPAD0),
            Map.entry(VK_NUMPAD1, KeyEvent.VK_NUMPAD1),
            Map.entry(VK_NUMPAD2, KeyEvent.VK_NUMPAD2),
            Map.entry(VK_NUMPAD3, KeyEvent.VK_NUMPAD3),
            Map.entry(VK_NUMPAD4, KeyEvent.VK_NUMPAD4),
            Map.entry(VK_NUMPAD5, KeyEvent.VK_NUMPAD5),
            Map.entry(VK_NUMPAD6, KeyEvent.VK_NUMPAD6),
            Map.entry(VK_NUMPAD7, KeyEvent.VK_NUMPAD7),
            Map.entry(VK_NUMPAD8, KeyEvent.VK_NUMPAD8),
            Map.entry(VK_NUMPAD9, KeyEvent.VK_NUMPAD9),

            // NumPad with NumLock on
            Map.entry(VK_MULTIPLY, KeyEvent.VK_MULTIPLY),
            Map.entry(VK_ADD, KeyEvent.VK_ADD),
            Map.entry(VK_SEPARATOR, KeyEvent.VK_SEPARATOR),
            Map.entry(VK_SUBTRACT, KeyEvent.VK_SUBTRACT),
            Map.entry(VK_DECIMAL, KeyEvent.VK_DECIMAL),
            Map.entry(VK_DIVIDE, KeyEvent.VK_DIVIDE),

            // Functional keys
            Map.entry(VK_F1, KeyEvent.VK_F1),
            Map.entry(VK_F2, KeyEvent.VK_F2),
            Map.entry(VK_F3, KeyEvent.VK_F3),
            Map.entry(VK_F4, KeyEvent.VK_F4),
            Map.entry(VK_F5, KeyEvent.VK_F5),
            Map.entry(VK_F6, KeyEvent.VK_F6),
            Map.entry(VK_F7, KeyEvent.VK_F7),
            Map.entry(VK_F8, KeyEvent.VK_F8),
            Map.entry(VK_F9, KeyEvent.VK_F9),
            Map.entry(VK_F10, KeyEvent.VK_F10),
            Map.entry(VK_F11, KeyEvent.VK_F11),
            Map.entry(VK_F12, KeyEvent.VK_F12),
            Map.entry(VK_F13, KeyEvent.VK_F13),
            Map.entry(VK_F14, KeyEvent.VK_F14),
            Map.entry(VK_F15, KeyEvent.VK_F15),
            Map.entry(VK_F16, KeyEvent.VK_F16),
            Map.entry(VK_F17, KeyEvent.VK_F17),
            Map.entry(VK_F18, KeyEvent.VK_F18),
            Map.entry(VK_F19, KeyEvent.VK_F19),
            Map.entry(VK_F20, KeyEvent.VK_F20),
            Map.entry(VK_F21, KeyEvent.VK_F21),
            Map.entry(VK_F22, KeyEvent.VK_F22),
            Map.entry(VK_F23, KeyEvent.VK_F23),
            Map.entry(VK_F24, KeyEvent.VK_F24),

            Map.entry(VK_SNAPSHOT, KeyEvent.VK_PRINTSCREEN),
            Map.entry(VK_SCROLL, KeyEvent.VK_SCROLL_LOCK),
            Map.entry(VK_PAUSE, KeyEvent.VK_PAUSE),
            Map.entry(VK_CANCEL, KeyEvent.VK_CANCEL),
            Map.entry(VK_HELP, KeyEvent.VK_HELP),

            // Japanese
            Map.entry(VK_CONVERT, KeyEvent.VK_CONVERT),
            Map.entry(VK_NONCONVERT, KeyEvent.VK_NONCONVERT),
            Map.entry(VK_KANJI, KeyEvent.VK_INPUT_METHOD_ON_OFF),
            Map.entry(VK_DBE_ALPHANUMERIC, KeyEvent.VK_ALPHANUMERIC),
            Map.entry(VK_DBE_KATAKANA, KeyEvent.VK_KATAKANA),
            Map.entry(VK_DBE_HIRAGANA, KeyEvent.VK_HIRAGANA),
            Map.entry(VK_DBE_DBCSCHAR, KeyEvent.VK_FULL_WIDTH),
            Map.entry(VK_DBE_SBCSCHAR, KeyEvent.VK_HALF_WIDTH),
            Map.entry(VK_DBE_ROMAN, KeyEvent.VK_ROMAN_CHARACTERS),

            Map.entry(0, KeyEvent.VK_UNDEFINED)
    );

    // https://github.com/openjdk/jdk/blob/9bc023220fbbb0b6ea1ed1a0ca2aa3848764e8cd/src/java.desktop/windows/native/libawt/windows/awt_Component.cpp#L2958
    private static Integer DYNAMIC_TABLE_LANG_ID;
    private static final Map<Integer, Integer> DYNAMIC_KEY_MAP_TABLE = new HashMap<>(Map.ofEntries(
            Map.entry(0x00BA,  KeyEvent.VK_UNDEFINED), // VK_OEM_1
            Map.entry(0x00BB,  KeyEvent.VK_UNDEFINED), // VK_OEM_PLUS
            Map.entry(0x00BC,  KeyEvent.VK_UNDEFINED), // VK_OEM_COMMA
            Map.entry(0x00BD,  KeyEvent.VK_UNDEFINED), // VK_OEM_MINUS
            Map.entry(0x00BE,  KeyEvent.VK_UNDEFINED), // VK_OEM_PERIOD
            Map.entry(0x00BF,  KeyEvent.VK_UNDEFINED), // VK_OEM_2
            Map.entry(0x00C0,  KeyEvent.VK_UNDEFINED), // VK_OEM_3
            Map.entry(0x00DB,  KeyEvent.VK_UNDEFINED), // VK_OEM_4
            Map.entry(0x00DC,  KeyEvent.VK_UNDEFINED), // VK_OEM_5
            Map.entry(0x00DD,  KeyEvent.VK_UNDEFINED), // VK_OEM_6
            Map.entry(0x00DE,  KeyEvent.VK_UNDEFINED), // VK_OEM_7
            Map.entry(0x00DF,  KeyEvent.VK_UNDEFINED), // VK_OEM_8
            Map.entry(0x00E2,  KeyEvent.VK_UNDEFINED), // VK_OEM_102
            Map.entry(0, 0)
    ));

    // Auxiliary tables used to fill the above dynamic table.  We first
    // find the character for the OEM VK code using ::MapVirtualKey and
    // then go through these auxiliary tables to map it to Java VK code.

    private static final Map<Character, Integer> CHAR_TO_VK_TABLE = Map.ofEntries(
            Map.entry('!',   KeyEvent.VK_EXCLAMATION_MARK),
            Map.entry('"',   KeyEvent.VK_QUOTEDBL),
            Map.entry('#',   KeyEvent.VK_NUMBER_SIGN),
            Map.entry('$',   KeyEvent.VK_DOLLAR),
            Map.entry('&',   KeyEvent.VK_AMPERSAND),
            Map.entry('\'',  KeyEvent.VK_QUOTE),
            Map.entry('(',   KeyEvent.VK_LEFT_PARENTHESIS),
            Map.entry(')',   KeyEvent.VK_RIGHT_PARENTHESIS),
            Map.entry('*',   KeyEvent.VK_ASTERISK),
            Map.entry('+',   KeyEvent.VK_PLUS),
            Map.entry(',',   KeyEvent.VK_COMMA),
            Map.entry('-',   KeyEvent.VK_MINUS),
            Map.entry('.',   KeyEvent.VK_PERIOD),
            Map.entry('/',   KeyEvent.VK_SLASH),
            Map.entry(':',   KeyEvent.VK_COLON),
            Map.entry(';',   KeyEvent.VK_SEMICOLON),
            Map.entry('<',   KeyEvent.VK_LESS),
            Map.entry('=',   KeyEvent.VK_EQUALS),
            Map.entry('>',   KeyEvent.VK_GREATER),
            Map.entry('@',   KeyEvent.VK_AT),
            Map.entry('[',   KeyEvent.VK_OPEN_BRACKET),
            Map.entry('\\',  KeyEvent.VK_BACK_SLASH),
            Map.entry(']',   KeyEvent.VK_CLOSE_BRACKET),
            Map.entry('^',   KeyEvent.VK_CIRCUMFLEX),
            Map.entry('_',   KeyEvent.VK_UNDERSCORE),
            Map.entry('`',   KeyEvent.VK_BACK_QUOTE),
            Map.entry('{',   KeyEvent.VK_BRACELEFT),
            Map.entry('}',   KeyEvent.VK_BRACERIGHT),
            Map.entry((char) 0x00A1, KeyEvent.VK_INVERTED_EXCLAMATION_MARK),
            Map.entry((char) 0x20A0, KeyEvent.VK_EURO_SIGN), // ????
            Map.entry((char) 0,0)
    );

    // For dead accents some layouts return ASCII punctuation, while some
    // return spacing accent chars, so both should be listed.  NB: MS docs
    // say that conversion routings return spacing accent character, not
    // combining.

    private static final Map<Character, Integer> CHAR_TO_DEAD_VK_TABLE = Map.ofEntries(
            Map.entry('`',   KeyEvent.VK_DEAD_GRAVE),
            Map.entry('\'',  KeyEvent.VK_DEAD_ACUTE),
            Map.entry((char) 0x00B4, KeyEvent.VK_DEAD_ACUTE),
            Map.entry('^',   KeyEvent.VK_DEAD_CIRCUMFLEX),
            Map.entry('~',   KeyEvent.VK_DEAD_TILDE),
            Map.entry((char) 0x02DC, KeyEvent.VK_DEAD_TILDE),
            Map.entry((char) 0x00AF, KeyEvent.VK_DEAD_MACRON),
            Map.entry((char) 0x02D8, KeyEvent.VK_DEAD_BREVE),
            Map.entry((char) 0x02D9, KeyEvent.VK_DEAD_ABOVEDOT),
            Map.entry('"',   KeyEvent.VK_DEAD_DIAERESIS),
            Map.entry((char) 0x00A8, KeyEvent.VK_DEAD_DIAERESIS),
            Map.entry((char) 0x02DA, KeyEvent.VK_DEAD_ABOVERING),
            Map.entry((char) 0x02DD, KeyEvent.VK_DEAD_DOUBLEACUTE),
            Map.entry((char) 0x02C7, KeyEvent.VK_DEAD_CARON),            // aka hacek
            Map.entry(',',   KeyEvent.VK_DEAD_CEDILLA),
            Map.entry((char) 0x00B8, KeyEvent.VK_DEAD_CEDILLA),
            Map.entry((char) 0x02DB, KeyEvent.VK_DEAD_OGONEK),
            Map.entry((char) 0x037A, KeyEvent.VK_DEAD_IOTA),             // ASCII ???
            Map.entry((char) 0x309B, KeyEvent.VK_DEAD_VOICED_SOUND),
            Map.entry((char) 0x309C, KeyEvent.VK_DEAD_SEMIVOICED_SOUND),
            Map.entry((char) 0x0004, KeyEvent.VK_COMPOSE),
            Map.entry((char) 0, 0)
    );

    /**
     * @implNote See <a>See https://github.com/openjdk/jdk/blob/9bc023220fbbb0b6ea1ed1a0ca2aa3848764e8cd/src/java.desktop/windows/native/libawt/windows/awt_Component.cpp#L3261</a>
     */
    public static int winVKeyToAwtKey(int vKey, int modifiers) {
        // Handle the few cases where we need to take the modifier into
        // consideration for the Java VK code or where we have to take the keyboard
        // layout into consideration so that function keys can get
        // recognized in a platform-independent way.
        switch (vKey) {
            case VK_CONVERT:
                if ((modifiers & KeyboardHookEvent.ALT_MASK) != 0)
                    return KeyEvent.VK_ALL_CANDIDATES;
                if ((modifiers & KeyboardHookEvent.SHIFT_MASK) != 0)
                    return KeyEvent.VK_PREVIOUS_CANDIDATE;
                break;
            case VK_DBE_ALPHANUMERIC:
                if ((modifiers & KeyboardHookEvent.ALT_MASK) != 0)
                    return KeyEvent.VK_CODE_INPUT;
                break;
            case VK_KANA:
                // This method is to determine whether the Kana Lock feature is
                // available on the attached keyboard.  Kana Lock feature does not
                // necessarily require that the real KANA keytop is available on
                // keyboard, so using MapVirtualKey(VK_KANA) is not sufficient for testing.
                // Instead of that we regard it as Japanese keyboard (w/ Kana Lock) if :-
                //
                // - the keyboard layout is Japanese (VK_KANA has the same value as VK_HANGUL)
                // - the keyboard is Japanese keyboard (keyboard type == 7).
                final var keyboardLayout = getForegroundKeyboardLayout();
                final boolean isKanaLockAvailable =
                        (keyboardLayout.getLanguageIdentifier() == LocaleMacros.MAKELANGID(LANG_JAPANESE, SUBLANG_DEFAULT)) &&
                                (User32.INSTANCE.GetKeyboardType(0) == 7);
                if (isKanaLockAvailable)
                    return KeyEvent.VK_KANA_LOCK;
                break;
        }

        // check dead key
//        if (isDeadKey) {
//            for (int i = 0; charToDeadVKTable[i].c != 0; i++) {
//                if (charToDeadVKTable[i].c == character) {
//                    return charToDeadVKTable[i].javaKey;
//                }
//            }
//        }

        Integer k = KEY_MAP_TABLE.get(vKey);
        if (k != null)
            return k;

        // Detect if the keyboard layout changed, if it did rebuild the table
        final var currLangId = getForegroundKeyboardLayout().getLanguageIdentifier();
        if (DYNAMIC_TABLE_LANG_ID == null || DYNAMIC_TABLE_LANG_ID != currLangId) {
            buildDynamicKeyMapTable();
            DYNAMIC_TABLE_LANG_ID = currLangId;
        }

        k = DYNAMIC_KEY_MAP_TABLE.get(vKey);
        if (k != null && k != KeyEvent.VK_UNDEFINED)
            return k;

        return KeyEvent.VK_UNDEFINED;
    }

    /**
     * @implNote See <a>https://github.com/openjdk/jdk/blob/9bc023220fbbb0b6ea1ed1a0ca2aa3848764e8cd/src/java.desktop/windows/native/libawt/windows/awt_Component.cpp#L3076</a>
     */
    private static void buildDynamicKeyMapTable() {
        HKL hkl = getForegroundKeyboardLayout();

        // Will need this to reset layout after dead keys.
        int spaceScanCode = User32.INSTANCE.MapVirtualKeyEx(VK_SPACE, 0, hkl);

        // Entries in dynamic table that maps between Java VK and Windows
        // VK are built in three steps:
        //   1. Map windows VK to ANSI character (cannot map to unicode
        //      directly, since ::ToUnicode is not implemented on win9x)
        //   2. Convert ANSI char to Unicode char
        //   3. Map Unicode char to Java VK via two auxilary tables.

        DYNAMIC_KEY_MAP_TABLE.forEach((windowsKey, javaKey) -> { // for each VK_OEM_*
            // Defaults to VK_UNDEFINED
            DYNAMIC_KEY_MAP_TABLE.put(windowsKey, KeyEvent.VK_UNDEFINED);

            byte[] kbdState = new byte[KB_STATE_SIZE];
            User32.INSTANCE.GetKeyboardState(kbdState);

            kbdState[windowsKey] |= 0x80; // Press the key.

            // Unpress modifiers, since they are most likely pressed as
            // part of the keyboard switching shortcut.
            kbdState[VK_CONTROL] &= ~0x80;
            kbdState[VK_SHIFT] &= ~0x80;
            kbdState[VK_MENU] &= ~0x80;

            byte[] cbuf = new byte[]{0, 0};
            int scancode = User32.INSTANCE.MapVirtualKeyEx(windowsKey, 0, hkl);
            int nchars = User32.INSTANCE.ToAsciiEx(windowsKey, scancode, kbdState, cbuf, 0, hkl);

            // Auxiliary table used to map Unicode character to Java VK.
            // Will assign a different table for dead keys (below).
            Map<Character, Integer> charMap = CHAR_TO_VK_TABLE;

            if (nchars < 0) { // Dead key
                // Use a different table for dead chars since different layouts
                // return different characters for the same dead key.
                charMap = CHAR_TO_DEAD_VK_TABLE;

                // We also need to reset layout so that next translation
                // is unaffected by the dead status.  We do this by
                // translating <SPACE> key.
                kbdState[windowsKey] &= ~0x80;
                kbdState[VK_SPACE] |= 0x80;

                byte[] junkBuf = new byte[]{0, 0};
                User32.INSTANCE.ToAsciiEx(VK_SPACE, spaceScanCode, kbdState, junkBuf, 0, hkl);
            }
            // cannot convert to ANSI char
            if (nchars == 0) {
                LOGGER.atError().log(() -> String.format("VK 0x%02X -> cannot convert to ANSI char", windowsKey));
                return;
            }
            // can't happen, see reset code below
            if (nchars > 1) {
                LOGGER.atError().log(() -> String.format("VK 0x%02X -> converted to <0x%02X,0x%02X>", windowsKey, cbuf[0], cbuf[1]));
                return;
            }

            final LPWSTR str = new LPWSTR(new Memory(2L * Native.WCHAR_SIZE));
            int codePage = langToCodePage(hkl.getLanguageIdentifier());
            int nConverted = Kernel32.INSTANCE.MultiByteToWideChar(codePage, 0, cbuf, 1, str, 2);
            if (nConverted < 0 || str.getValue().length() <= 0) {
                if(LOGGER.isErrorEnabled())
                    LOGGER.error(
                            String.format("VK 0x%02X -> ANSI 0x%02X -> MultiByteToWideChar failed\n", windowsKey, cbuf[0]),
                            new Win32Exception(Kernel32.INSTANCE.GetLastError()));
                return;
            }

            char uc = str.getValue().charAt(0);
            charMap.entrySet().stream()
                    .sequential()
                    .filter(e -> e.getKey() == uc)
                    .findFirst()
                    .map(Map.Entry::getValue)
                    .ifPresent(newJavaKey -> DYNAMIC_KEY_MAP_TABLE.put(windowsKey, newJavaKey));
        });
    }

    private static int langToCodePage(int idLang) {
        final var strCodePage = new Memory((long) Kernel32.MAX_ACP_STR_LEN * Native.WCHAR_SIZE);
        // use the LANGID to create a LCID
        final LCID idLocale = LocaleMacros.MAKELCID(idLang, SORT_DEFAULT);
        // get the ANSI code page associated with this locale
        if (Kernel32.INSTANCE.GetLocaleInfo(idLocale, Kernel32.LOCALE_IDEFAULTANSICODEPAGE, strCodePage, Kernel32.MAX_ACP_STR_LEN) <= 0)
            return Kernel32.INSTANCE.GetACP();

        try {
            return Integer.parseInt(W32APITypeMapper.DEFAULT == W32APITypeMapper.UNICODE ?
                    strCodePage.getWideString(0) :
                    strCodePage.getString(0));
        } catch (NumberFormatException ex) {
            return 0; // TODO: does _ttoi do this?
        }
    }

    /**
     * Get the keyboard layout of the window in the foreground
     * <p>
     * Since our window is never the focused one (or visible for that matter), we never get WM_INPUTLANGCHANGEREQUEST
     * and WM_INPUTLANGCHANGE events.
     * That means our thread's keyboard layout is never changed, so we need to get one that we are sure changed
     *
     * @return keyboard layout of the window in the foreground
     */
    private static HKL getForegroundKeyboardLayout() {
        final HWND foregroundWindow = User32.INSTANCE.GetForegroundWindow();
        return foregroundWindow != null ?
                User32.INSTANCE.GetKeyboardLayout(User32.INSTANCE.GetWindowThreadProcessId(foregroundWindow, null)) :
                User32.INSTANCE.GetKeyboardLayout(0); // fall back to the current thread layout
    }

    /**
     * @implNote See <a>https://github.com/openjdk/jdk/blob/9bc023220fbbb0b6ea1ed1a0ca2aa3848764e8cd/src/java.desktop/windows/native/libawt/windows/awt_Component.cpp#L2607</a>
     */
    public static int getAwtKeyLocation(int vKey, int scancode, boolean extended) {
        // Rector+Newcomer page 413
        // The extended keys are the Alt and Control on the right of
        // the space bar, the non-Numpad arrow keys, the non-Numpad
        // Insert, PageUp, etc. keys, and the Numpad Divide and Enter keys.
        // Note that neither Shift key is extended.
        // Although not listed in Rector+Newcomer, both Windows keys
        // (91 and 92) are extended keys, the Context Menu key
        // (property key or application key - 93) is extended,
        // and so is the NumLock key.

        final boolean isNumpadKey = switch (vKey) {
            // numpad ,  not on US kbds
            case VK_CLEAR, // numpad 5 with numlock off
                    VK_NUMPAD0, VK_NUMPAD1, VK_NUMPAD2, VK_NUMPAD3, VK_NUMPAD4, VK_NUMPAD5, VK_NUMPAD6, VK_NUMPAD7, VK_NUMPAD8, VK_NUMPAD9,
                    VK_MULTIPLY, VK_ADD,
                    VK_SEPARATOR,  // numpad ,  not on US kbds
                    VK_SUBTRACT, VK_DECIMAL, VK_DIVIDE, VK_NUMLOCK -> true;
            case VK_END,
                    VK_PRIOR /* PageUp */, VK_NEXT /* PageDown */,
                    VK_HOME,
                    VK_LEFT, VK_UP, VK_RIGHT, VK_DOWN,
                    VK_INSERT, VK_DELETE -> (!extended); // extended if non-numpad
            case VK_RETURN -> (extended); // extended if on numpad
            default -> false;
        };
        if (isNumpadKey)
            return KeyEvent.KEY_LOCATION_NUMPAD;

        return switch (vKey) {
            case VK_SHIFT -> {
                int leftShiftScancode = User32.INSTANCE.MapVirtualKey(VK_LSHIFT, MAPVK_VK_TO_VSC);
                int rightShiftScancode = User32.INSTANCE.MapVirtualKey(VK_RSHIFT, MAPVK_VK_TO_VSC);

                if (scancode == leftShiftScancode)
                    yield KeyEvent.KEY_LOCATION_LEFT;
                if (scancode == rightShiftScancode)
                    yield KeyEvent.KEY_LOCATION_RIGHT;
                yield KeyEvent.KEY_LOCATION_LEFT;
            }
            case VK_CONTROL, VK_MENU -> extended ? KeyEvent.KEY_LOCATION_RIGHT : KeyEvent.KEY_LOCATION_LEFT;
            case VK_LSHIFT, VK_LCONTROL, VK_LMENU, VK_LWIN -> KeyEvent.KEY_LOCATION_LEFT;
            case VK_RSHIFT, VK_RCONTROL, VK_RMENU, VK_RWIN -> KeyEvent.KEY_LOCATION_RIGHT;
            // REMIND: if we add keycodes for the windows keys, we'll have to
            // include left/right discrimination code for them.
            default -> KeyEvent.KEY_LOCATION_STANDARD;
        };
    }

    static int getLocatedVKey(int vKey, int scancode, boolean extended) {
        return switch (vKey) {
            case VK_SHIFT -> {
                int leftShiftScancode = User32.INSTANCE.MapVirtualKey(VK_LSHIFT, MAPVK_VK_TO_VSC);
                int rightShiftScancode = User32.INSTANCE.MapVirtualKey(VK_RSHIFT, MAPVK_VK_TO_VSC);

                if (scancode == leftShiftScancode)
                    yield VK_LSHIFT;
                if (scancode == rightShiftScancode)
                    yield VK_RSHIFT;
                yield VK_LSHIFT;
            }
            case VK_CONTROL -> extended ? VK_RCONTROL : VK_LCONTROL;
            case VK_MENU -> extended ? VK_RMENU : VK_LMENU;
            // REMIND: if we add keycodes for the windows keys, we'll have to
            // include left/right discrimination code for them.
            default -> vKey;
        };
    }

    private interface User32 extends com.sun.jna.platform.win32.User32 {

        User32 INSTANCE = Native.load("user32", User32.class, DEFAULT_OPTIONS);

        /**
         * Translates (maps) a virtual-key code into a scan code or character value, or
         * translates a scan code into a virtual-key code. The function translates the
         * codes using the input language and an input locale identifier.
         * <p>
         * See <a>https://docs.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-mapvirtualkeya</a>
         *
         * @param uCode    The virtual-key code or scan code for a key. How this value
         *                 is interpreted depends on the value of the uMapType
         *                 parameter. Starting with Windows Vista, the high byte of the
         *                 uCode value can contain either 0xe0 or 0xe1 to specify the
         *                 extended scan code.
         * @param uMapType The translation to perform. The value of this parameter
         *                 depends on the value of the uCode parameter. One of
         *                 {@link WinUser#MAPVK_VK_TO_CHAR},
         *                 {@link WinUser#MAPVK_VK_TO_VSC},
         *                 {@link WinUser#MAPVK_VK_TO_VSC_EX},
         *                 {@link WinUser#MAPVK_VSC_TO_VK},
         *                 {@link WinUser#MAPVK_VSC_TO_VK_EX}
         * @return The return value is either a scan code, a virtual-key code, or a
         * character value, depending on the value of uCode and uMapType. If
         * there is no translation, the return value is zero.
         */
        int MapVirtualKey(int uCode, int uMapType);

        /**
         * Retrieves information about the current keyboard.
         * <p>
         * See <a>https://docs.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getkeyboardtype</a>
         *
         * @param nTypeFlag The type of keyboard information to be retrieved.
         *                  This parameter can be one of the following values.
         *                  - 0 	Keyboard type
         *                  - 1 	Keyboard subtype
         *                  - 2 	The number of function keys on the keyboard
         * @return If the function succeeds, the return value specifies the requested information.
         * <p>
         * If the function fails and nTypeFlag is not 1, the return value is 0;
         * 0 is a valid return value when nTypeFlag is 1 (keyboard subtype).
         * To get extended error information, call GetLastError.
         */
        int GetKeyboardType(int nTypeFlag);

        /**
         * Translates the specified virtual-key code and keyboard state to the corresponding character or characters.
         * The function translates the code using the input language and physical keyboard layout identified
         * by the input locale identifier.
         *
         * @param uVirtKey   The virtual-key code to be translated. See Virtual-Key Codes.
         * @param uScanCode  The hardware scan code of the key to be translated.
         *                   The high-order bit of this value is set if the key is up (not pressed).
         * @param lpKeyState A pointer to a 256-byte array that contains the current keyboard state.
         *                   Each element (byte) in the array contains the state of one key.
         *                   If the high-order bit of a byte is set, the key is down (pressed).
         *                   <p>
         *                   The low bit, if set, indicates that the key is toggled on.
         *                   In this function, only the toggle bit of the CAPS LOCK key is relevant.
         *                   The toggle state of the NUM LOCK and SCOLL LOCK keys is ignored.
         * @param lpChar     A pointer to the buffer that receives the translated character or characters.
         * @param uFlags     This parameter must be 1 if a menu is active, zero otherwise.
         * @param dwhkl      Input locale identifier to use to translate the code.
         *                   This parameter can be any input locale identifier previously returned by the
         *                   LoadKeyboardLayout function.
         * @return If the specified key is a dead key, the return value is negative.
         * Otherwise, it is one of the following values.
         * - 0: The specified virtual key has no translation for the current state of the keyboard.
         * - 1: One character was copied to the buffer.
         * - 2: Two characters were copied to the buffer.
         *      This usually happens when a dead-key character (accent or diacritic) stored in the keyboard layout
         *      cannot be composed with the specified virtual key to form a single character.
         */
        int ToAsciiEx(int uVirtKey, int uScanCode, byte[] lpKeyState, byte[] lpChar, int uFlags, HKL dwhkl);
    }

    private interface Kernel32 extends com.sun.jna.platform.win32.Kernel32 {

        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class, DEFAULT_OPTIONS);

        int MAX_ACP_STR_LEN = 7; // ANSI CP identifiers are no longer than this
        int LOCALE_IDEFAULTANSICODEPAGE = 0;

        /**
         * Maps a character string to a UTF-16 (wide character) string.
         * The character string is not necessarily from a multibyte character set.
         *
         * See <a>https://docs.microsoft.com/en-us/windows/win32/api/stringapiset/nf-stringapiset-multibytetowidechar</a>
         *
         * @param CodePage       Code page to use in performing the conversion.
         *                       This parameter can be set to the value of any code page that is installed or available
         *                       in the operating system.
         *                       For a list of code pages, see Code Page Identifiers.
         *                       Your application can also specify one of the values shown in the following table.
         *                       - CP_ACP: The system default Windows ANSI code page.
         *                                 Note: This value can be different on different computers, even on the same network.
         *                                       It can be changed on the same computer, leading to stored data becoming
         *                                       irrecoverably corrupted.
         *                                       This value is only intended for temporary use and permanent storage should
         *                                       use UTF-16 or UTF-8 if possible.
         *                       - CP_MACCP: The current system Macintosh code page.
         *                                   Note: This value can be different on different computers, even on the same network.
         *                                         It can be changed on the same computer, leading to stored data becoming
         *                                         irrecoverably corrupted.
         *                                         This value is only intended for temporary use and permanent storage should
         *                                         use UTF-16 or UTF-8 if possible.
         *                                   Note: This value is used primarily in legacy code and should not generally
         *                                         be needed since modern Macintosh computers use Unicode for encoding.
         *                       - CP_OEMCP: The current system OEM code page.
         *                                   Note: This value can be different on different computers, even on the same network.
         *                                         It can be changed on the same computer, leading to stored data becoming
         *                                         irrecoverably corrupted.
         *                                         This value is only intended for temporary use and permanent storage should
         *                                         use UTF-16 or UTF-8 if possible.
         *                       - CP_SYMBOL: Symbol code page (42).
         *                       - CP_THREAD_ACP: The Windows ANSI code page for the current thread.
         *                                        Note: This value can be different on different computers, even on the same network.
         *                                              It can be changed on the same computer, leading to stored data
         *                                              becoming irrecoverably corrupted.
         *                                              This value is only intended for temporary use and permanent storage
         *                                              should use UTF-16 or UTF-8 if possible.
         *                       - CP_UTF7: UTF-7. Use this value only when forced by a 7-bit transport mechanism.
         *                                  Use of UTF-8 is preferred.
         *                       - CP_UTF8: UTF-8.
         * @param dwFlags        Flags indicating the conversion type.
         *                       The application can specify a combination of the following values, with MB_PRECOMPOSED being the default.
         *                       MB_PRECOMPOSED and MB_COMPOSITE are mutually exclusive.
         *                       MB_USEGLYPHCHARS and MB_ERR_INVALID_CHARS can be set regardless of the state of the other flags.
         *                       - MB_COMPOSITE: Always use decomposed characters, that is, characters in which a base
         *                                       character and one or more nonspacing characters each have distinct
         *                                       code point values. For example, Ä is represented by A + ¨:
         *                                       LATIN CAPITAL LETTER A (U+0041) + COMBINING DIAERESIS (U+0308).
         *                                       Note that this flag cannot be used with MB_PRECOMPOSED.
         *                       - MB_ERR_INVALID_CHARS: Fail if an invalid input character is encountered.
         *                                               Starting with Windows Vista, the function does not drop illegal
         *                                               code points if the application does not set this flag,
         *                                               but instead replaces illegal sequences with U+FFFD (encoded
         *                                               as appropriate for the specified codepage).
         *                                               <p>
         *                                               Windows 2000 with SP4 and later, Windows XP:
         *                                               If this flag is not set, the function silently drops illegal
         *                                               code points.
         *                                               A call to GetLastError returns ERROR_NO_UNICODE_TRANSLATION.
         *                       - MB_PRECOMPOSED: Default; do not use with MB_COMPOSITE.
         *                                         Always use precomposed characters, that is, characters having a
         *                                         single character value for a base or nonspacing character combination.
         *                                         For example, in the character è, the e is the base character
         *                                         and the accent grave mark is the nonspacing character.
         *                                         If a single Unicode code point is defined for a character,
         *                                         the application should use it instead of a separate base character and
         *                                         a nonspacing character. For example, Ä is represented by the single
         *                                         Unicode code point LATIN CAPITAL LETTER A WITH DIAERESIS (U+00C4).
         *                       - MB_USEGLYPHCHARS: Use glyph characters instead of control characters.
         *                       For the code pages listed below, dwFlags must be set to 0. Otherwise, the function fails with ERROR_INVALID_FLAGS.
         *                       - 50220
         *                       - 50221
         *                       - 50222
         *                       - 50225
         *                       - 50227
         *                       - 50229
         *                       - 57002 through 57011
         *                       - 65000 (UTF-7)
         *                       - 42 (Symbol)
         * @param lpMultiByteStr Pointer to the character string to convert.
         * @param cbMultiByte    Size, in bytes, of the string indicated by the lpMultiByteStr parameter.
         *                       Alternatively, this parameter can be set to -1 if the string is null-terminated.
         *                       Note that, if cbMultiByte is 0, the function fails.
         *                       <p>
         *                       If this parameter is -1, the function processes the entire input string, including the
         *                       terminating null character. Therefore, the resulting Unicode string has a terminating
         *                       null character, and the length returned by the function includes this character.
         *                       <p>
         *                       If this parameter is set to a positive integer, the function processes exactly
         *                       the specified number of bytes. If the provided size does not include a terminating
         *                       null character, the resulting Unicode string is not null-terminated, and the returned
         *                       length does not include this character.
         * @param lpWideCharStr  Pointer to a buffer that receives the converted string.
         * @param cchWideChar    Size, in characters, of the buffer indicated by lpWideCharStr.
         *                       If this value is 0, the function returns the required buffer size, in characters,
         *                       including any terminating null character, and makes no use of the lpWideCharStr buffer.
         * @return Returns the number of characters written to the buffer indicated by lpWideCharStr if successful.
         * If the function succeeds and cchWideChar is 0, the return value is the required size, in characters,
         * or the buffer indicated by lpWideCharStr. Also see dwFlags for info about how the MB_ERR_INVALID_CHARS
         * flag affects the return value when invalid sequences are input.
         * </p>
         * The function returns 0 if it does not succeed. To get extended error information,
         * the application can call GetLastError, which can return one of the following error codes:
         * - ERROR_INSUFFICIENT_BUFFER. A supplied buffer size was not large enough, or it was incorrectly set to NULL.
         * - ERROR_INVALID_FLAGS. The values supplied for flags were not valid.
         * - ERROR_INVALID_PARAMETER. Any of the parameter values was invalid.
         * - ERROR_NO_UNICODE_TRANSLATION. Invalid Unicode was found in a string.
         */
        int MultiByteToWideChar(int CodePage, int dwFlags, byte[] lpMultiByteStr, int cbMultiByte, LPWSTR lpWideCharStr, int cchWideChar);

        /**
         * Retrieves information about a locale specified by identifier.
         * <p>
         * See <a>https://docs.microsoft.com/en-us/windows/win32/api/winnls/nf-winnls-getlocaleinfow</a>
         *
         * @param Locale   Locale identifier for which to retrieve information.
         *                 You can use the {@link LocaleMacros#MAKELCID(int, int)} macro to create a locale identifier
         *                 or use one of the following predefined values.
         *                 - LOCALE_CUSTOM_DEFAULT
         *                 - LOCALE_CUSTOM_UI_DEFAULT
         *                 - LOCALE_CUSTOM_UNSPECIFIED
         *                 - LOCALE_INVARIANT
         *                 - LOCALE_SYSTEM_DEFAULT
         *                 - LOCALE_USER_DEFAULT
         * @param LCType   The locale information to retrieve.
         *                 For detailed definitions, see the LCType parameter of GetLocaleInfoEx.
         *                 Note: For GetLocaleInfo, the value LOCALE_USE_CP_ACP is relevant only for the ANSI version.
         * @param lpLCData Pointer to a buffer in which this function retrieves the requested locale information.
         *                 This pointer is not used if cchData is set to 0. For more information, see the Remarks section.
         * @param cchData  Size, in TCHAR values, of the data buffer indicated by lpLCData.
         *                 Alternatively, the application can set this parameter to 0.
         *                 In this case, the function does not use the lpLCData parameter and returns the required buffer
         *                 size, including the terminating null character.
         * @return Returns the number of characters retrieved in the locale data buffer if successful and cchData
         * is a nonzero value. If the function succeeds, cchData is nonzero, and LOCALE_RETURN_NUMBER is specified,
         * the return value is the size of the integer retrieved in the data buffer; that is, 2 for the Unicode
         * version of the function or 4 for the ANSI version.
         * If the function succeeds and the value of cchData is 0, the return value is the required size,
         * in characters including a null character, for the locale data buffer.
         * <p>
         * The function returns 0 if it does not succeed. To get extended error information, the application
         * can call GetLastError, which can return one of the following error codes:
         * - ERROR_INSUFFICIENT_BUFFER. A supplied buffer size was not large enough, or it was incorrectly set to NULL.
         * - ERROR_INVALID_FLAGS. The values supplied for flags were not valid.
         * - ERROR_INVALID_PARAMETER. Any of the parameter values was invalid.
         */
        int GetLocaleInfo(LCID Locale, int LCType, Pointer lpLCData, int cchData);

        /**
         * Retrieves the current Windows ANSI code page identifier for the operating system.
         * <p>
         * See <a>https://docs.microsoft.com/en-us/windows/win32/api/winnls/nf-winnls-getacp</a>
         *
         * @return Returns the current Windows ANSI code page (ACP) identifier for the operating system.
         * See Code Page Identifiers for a list of identifiers for Windows ANSI code pages and other code pages.
         */
        int GetACP();
    }
}
