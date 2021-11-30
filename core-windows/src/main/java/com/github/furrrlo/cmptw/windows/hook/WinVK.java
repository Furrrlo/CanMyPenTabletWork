package com.github.furrrlo.cmptw.windows.hook;

/**
 * See <a>https://docs.microsoft.com/en-us/windows/win32/inputdev/virtual-key-codes</a>
 */
@SuppressWarnings("unused")
public interface WinVK {
    /** Left mouse button */
    int VK_LBUTTON = 0x01;
    /** Right mouse button */
    int VK_RBUTTON = 0x02;
    /** Control-break processing */
    int VK_CANCEL = 0x03;
    /** Middle mouse button(three-button mouse) */
    int VK_MBUTTON = 0x04;
    /** X1 mouse button */
    int VK_XBUTTON1 = 0x05;
    /** X2 mouse button */
    int VK_XBUTTON2 = 0x06;
    /** BACKSPACE key */
    int VK_BACK = 0x08;
    /** TAB key */
    int VK_TAB = 0x09;
    /** CLEAR key */
    int VK_CLEAR = 0x0C;
    /** ENTER key */
    int VK_RETURN = 0x0D;
    /** SHIFT key */
    int VK_SHIFT = 0x10;
    /** CTRL key */
    int VK_CONTROL = 0x11;
    /** ALT key */
    int VK_MENU = 0x12;
    /** PAUSE key */
    int VK_PAUSE = 0x13;
    /** CAPS LOCK key */
    int VK_CAPITAL = 0x14;
    /** IME Kana mode */
    int VK_KANA = 0x15;
    /** IME Hanguel  mode(maintained for compatibility; use VK_HANGUL) */
    int VK_HANGUEL = 0x15;
    /** IME Hangul mode */
    int VK_HANGUL = 0x15;
    /** IME On */
    int VK_IME_ON = 0x16;
    /** IME Junja mode */
    int VK_JUNJA = 0x17;
    /** IME final mode */
    int VK_FINAL = 0x18;
    /** IME Hanja mode */
    int VK_HANJA = 0x19;
    /** IME Kanji mode */
    int VK_KANJI = 0x19;
    /** IME Off */
    int VK_IME_OFF = 0x1A;
    /** ESC key */
    int VK_ESCAPE = 0x1B;
    /** IME convert */
    int VK_CONVERT = 0x1C;
    /** IME nonconvert */
    int VK_NONCONVERT = 0x1D;
    /** IME accept */
    int VK_ACCEPT = 0x1E;
    /** IME mode change request */
    int VK_MODECHANGE = 0x1F;
    /** SPACEBAR */
    int VK_SPACE = 0x20;
    /** PAGE UP key */
    int VK_PRIOR = 0x21;
    /** PAGE DOWN key */
    int VK_NEXT = 0x22;
    /** END key */
    int VK_END = 0x23;
    /** HOME key */
    int VK_HOME = 0x24;
    /** LEFT ARROW key */
    int VK_LEFT = 0x25;
    /** UP ARROW key */
    int VK_UP = 0x26;
    /** RIGHT ARROW key */
    int VK_RIGHT = 0x27;
    /** DOWN ARROW key */
    int VK_DOWN = 0x28;
    /** SELECT key */
    int VK_SELECT = 0x29;
    /** PRINT key */
    int VK_PRINT = 0x2A;
    /** EXECUTE key */
    int VK_EXECUTE = 0x2B;
    /** PRINT SCREEN key */
    int VK_SNAPSHOT = 0x2C;
    /** INS key */
    int VK_INSERT = 0x2D;
    /** DEL key */
    int VK_DELETE = 0x2E;
    /** HELP key */
    int VK_HELP = 0x2F;
    /** 0 key */
    int VK_0 = 0x30;
    /** 1 key */
    int VK_1  = 0x31;
    /** 2 key */
    int VK_2  = 0x32;
    /** 3 key */
    int VK_3  =0x33;
    /** 4 key */
    int VK_4  =0x34;
    /** 5 key */
    int VK_5 = 0x35;
    /** 6 key */
    int VK_6 = 0x36;
    /** 7 key */
    int VK_7 = 0x37;
    /** 8 key */
    int VK_8 = 0x38;
    /** 9 key */
    int VK_9 = 0x39;
    /** A key */
    int VK_A = 0x41;
    /** B key */
    int VK_B = 0x42;
    /** C key */
    int VK_C = 0x43;
    /** D key */
    int VK_D = 0x44;
    /** E key */
    int VK_E = 0x45;
    /** F key */
    int VK_F = 0x46;
    /** G key */
    int VK_G = 0x47;
    /** H key */
    int VK_H = 0x48;
    /** I key */
    int VK_I = 0x49;
    /** J key */
    int VK_J = 0x4A;
    /** K key */
    int VK_K = 0x4B;
    /** L key */
    int VK_L = 0x4C;
    /** M key */
    int VK_M = 0x4D;
    /** N key */
    int VK_N = 0x4E;
    /** O key */
    int VK_O = 0x4F;
    /** P key */
    int VK_P = 0x50;
    /** Q key */
    int VK_Q = 0x51;
    /** R key */
    int VK_R = 0x52;
    /** S key */
    int VK_S = 0x53;
    /** T key */
    int VK_T = 0x54;
    /** U key */
    int VK_U = 0x55;
    /** V key */
    int VK_V = 0x56;
    /** W key */
    int VK_W = 0x57;
    /** X key */
    int VK_X = 0x58;
    /** Y key */
    int VK_Y = 0x59;
    /** Z key */
    int VK_Z = 0x5A;
    /** Left Windows key(Natural keyboard) */
    int VK_LWIN = 0x5B;
    /** Right Windows key(Natural keyboard) */
    int VK_RWIN = 0x5C;
    /** Applications key(Natural keyboard) */
    int VK_APPS = 0x5D;
    /** Computer Sleep key */
    int VK_SLEEP = 0x5F;
    /** Numeric keypad 0key */
    int VK_NUMPAD0 = 0x60;
    /** Numeric keypad 1key */
    int VK_NUMPAD1 = 0x61;
    /** Numeric keypad 2key */
    int VK_NUMPAD2 = 0x62;
    /** Numeric keypad 3key */
    int VK_NUMPAD3 = 0x63;
    /** Numeric keypad 4key */
    int VK_NUMPAD4 = 0x64;
    /** Numeric keypad 5key */
    int VK_NUMPAD5 = 0x65;
    /** Numeric keypad 6key */
    int VK_NUMPAD6 = 0x66;
    /** Numeric keypad 7key */
    int VK_NUMPAD7 = 0x67;
    /** Numeric keypad 8key */
    int VK_NUMPAD8 = 0x68;
    /** Numeric keypad 9key */
    int VK_NUMPAD9 = 0x69;
    /** Multiply key */
    int VK_MULTIPLY = 0x6A;
    /** Add key */
    int VK_ADD = 0x6B;
    /** Separator key */
    int VK_SEPARATOR = 0x6C;
    /** Subtract key */
    int VK_SUBTRACT = 0x6D;
    /** Decimal key */
    int VK_DECIMAL = 0x6E;
    /** Divide key */
    int VK_DIVIDE = 0x6F;
    /** F1 key */
    int VK_F1 = 0x70;
    /** F2 key */
    int VK_F2 = 0x71;
    /** F3 key */
    int VK_F3 = 0x72;
    /** F4 key */
    int VK_F4 = 0x73;
    /** F5 key */
    int VK_F5 = 0x74;
    /** F6 key */
    int VK_F6 = 0x75;
    /** F7 key */
    int VK_F7 = 0x76;
    /** F8 key */
    int VK_F8 = 0x77;
    /** F9 key */
    int VK_F9 = 0x78;
    /** F10 key */
    int VK_F10 = 0x79;
    /** F11 key */
    int VK_F11 = 0x7A;
    /** F12 key */
    int VK_F12 = 0x7B;
    /** F13 key */
    int VK_F13 = 0x7C;
    /** F14 key */
    int VK_F14 = 0x7D;
    /** F15 key */
    int VK_F15 = 0x7E;
    /** F16 key */
    int VK_F16 = 0x7F;
    /** F17 key */
    int VK_F17 = 0x80;
    /** F18 key */
    int VK_F18 = 0x81;
    /** F19 key */
    int VK_F19 = 0x82;
    /** F20 key */
    int VK_F20 = 0x83;
    /** F21 key */
    int VK_F21 = 0x84;
    /** F22 key */
    int VK_F22 = 0x85;
    /** F23 key */
    int VK_F23 = 0x86;
    /** F24 key */
    int VK_F24 = 0x87;
    /** NUM LOCK key */
    int VK_NUMLOCK = 0x90;
    /** SCROLL LOCK key */
    int VK_SCROLL = 0x91;
    /** Left SHIFT key */
    int VK_LSHIFT = 0xA0;
    /** Right SHIFT key */
    int VK_RSHIFT = 0xA1;
    /** Left CONTROL key */
    int VK_LCONTROL = 0xA2;
    /** Right CONTROL key */
    int VK_RCONTROL = 0xA3;
    /** Left MENU key */
    int VK_LMENU = 0xA4;
    /** Right MENU key */
    int VK_RMENU = 0xA5;
    /** Browser Back key */
    int VK_BROWSER_BACK = 0xA6;
    /** Browser Forward key */
    int VK_BROWSER_FORWARD = 0xA7;
    /** Browser Refresh key */
    int VK_BROWSER_REFRESH = 0xA8;
    /** Browser Stop key */
    int VK_BROWSER_STOP = 0xA9;
    /** Browser Search key */
    int VK_BROWSER_SEARCH = 0xAA;
    /** Browser Favorites key */
    int VK_BROWSER_FAVORITES = 0xAB;
    /** Browser Start and Home key */
    int VK_BROWSER_HOME = 0xAC;
    /** Volume Mute key */
    int VK_VOLUME_MUTE = 0xAD;
    /** Volume Down key */
    int VK_VOLUME_DOWN = 0xAE;
    /** Volume Up key */
    int VK_VOLUME_UP = 0xAF;
    /** Next Track key */
    int VK_MEDIA_NEXT_TRACK = 0xB0;
    /** Previous Track key */
    int VK_MEDIA_PREV_TRACK = 0xB0;
    /** Stop Media key */
    int VK_MEDIA_STOP = 0xB2;
    /** Play/Pause Media key */
    int VK_MEDIA_PLAY_PAUSE = 0xB3;
    /** Start Mail key */
    int VK_LAUNCH_MAIL = 0xB4;
    /** Select Media key */
    int VK_LAUNCH_MEDIA_SELECT = 0xB5;
    /** Start Application 1key */
    int VK_LAUNCH_APP1 = 0xB6;
    /** Start Application 2key */
    int VK_LAUNCH_APP2 = 0xB7;
    /** Used for miscellaneous characters; it can vary by keyboard. For the US standard keyboard,the ';:'key */
    int VK_OEM_1 = 0xBA;
    /** For any country/region,the '+'key */
    int VK_OEM_PLUS = 0xBB;
    /** For any country/region,the ','key */
    int VK_OEM_COMMA = 0xBC;
    /** For any country/region,the '-'key */
    int VK_OEM_MINUS = 0xBD;
    /** For any country/region,the '.'key */
    int VK_OEM_PERIOD = 0xBE;
    /** Used for miscellaneous characters; it can vary by keyboard . For the US standard keyboard,the '/?'key */
    int VK_OEM_2 = 0xBF;
    /** Used for miscellaneous characters; it can vary by keyboard. For the US standard keyboard,the '`~'key */
    int VK_OEM_3 = 0xC0;
    /** Used for miscellaneous characters; it can vary by keyboard. For the US standard keyboard,the '[{'key */
    int VK_OEM_4 = 0xDB;
    /** Used for miscellaneous characters; it can vary by keyboard. For the US standard keyboard,the '\|'key */
    int VK_OEM_5 = 0xDC;
    /** Used for miscellaneous characters; it can vary by keyboard. For the US standard keyboard,the ']}'key */
    int VK_OEM_6 = 0xDD;
    /** Used for miscellaneous characters; it can vary by keyboard. For the US standard keyboard,the 'single-quote/double-quote'key */
    int VK_OEM_7 = 0xDE;
    /** Used for miscellaneous characters; it can vary by keyboard. */
    int VK_OEM_8 = 0xDF;
    /** Either the angle bracket key or the backslash key on the RT 102-key keyboard */
    int VK_OEM_102 = 0xE2;
    /** IME PROCESS key */
    int VK_PROCESSKEY = 0xE5;
    /** Used to pass Unicode characters as if they were keystrokes. The VK_PACKET key is the low word of a 32-bit Virtual Key value used for non-keyboard input methods. For more information, see Remark in KEYBDINPUT, SendInput, WM_KEYDOWN, and WM_KEYUP */
    int VK_PACKET = 0xE7;
    /** Attn key */
    int VK_ATTN = 0xF6;
    /** CrSel key */
    int VK_CRSEL = 0xF7;
    /** ExSel key */
    int VK_EXSEL = 0xF8;
    /** Erase EOF key */
    int VK_EREOF = 0xF9;
    /** Play key */
    int VK_PLAY = 0xFA;
    /** Zoom key */
    int VK_ZOOM = 0xFB;
    /** Reserved */
    int VK_NONAME = 0xFC;
    /** PA1 key */
    int VK_PA1 = 0xFD;
    /** Clear key */
    int VK_OEM_CLEAR = 0xFE;

    int VK_DBE_ALPHANUMERIC = 0x0f0;
    int VK_DBE_KATAKANA = 0x0f1;
    int VK_DBE_HIRAGANA = 0x0f2;
    int VK_DBE_SBCSCHAR = 0x0f3;
    int VK_DBE_DBCSCHAR = 0x0f4;
    int VK_DBE_ROMAN = 0x0f5;
    int VK_DBE_NOROMAN = 0x0f6;
    int VK_DBE_ENTERWORDREGISTERMODE = 0x0f7;
    int VK_DBE_ENTERIMECONFIGMODE = 0x0f8;
    int VK_DBE_FLUSHSTRING = 0x0f9;
    int VK_DBE_CODEINPUT = 0x0fa;
    int VK_DBE_NOCODEINPUT = 0x0fb;
    int VK_DBE_DETERMINESTRING = 0x0fc;
    int VK_DBE_ENTERDLGCONVERSIONMODE = 0x0fd;

    int KB_STATE_SIZE = 256;
}
