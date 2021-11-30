package com.github.furrrlo.cmptw.windows.raw;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Union;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

import static com.sun.jna.win32.W32APIOptions.DEFAULT_OPTIONS;

@SuppressWarnings("unused")
interface RawInput extends StdCallLibrary, WinUser, WinNT {

    RawInput INSTANCE = Native.load("user32", RawInput.class, DEFAULT_OPTIONS);

    int RID_HEADER = 0x10000005;
    int RID_INPUT = 0x10000003;

    int RIDI_PREPARSEDDATA = 0x20000005;
    int RIDI_DEVICENAME = 0x20000007;
    int RIDI_DEVICEINFO = 0x2000000b;

    int RIDEV_REMOVE = 0x00000001;
    int RIDEV_EXCLUDE = 0x00000010;
    int RIDEV_PAGEONLY = 0x00000020;
    int RIDEV_NOLEGACY = 0x00000030;
    int RIDEV_INPUTSINK = 0x00000100;
    int RIDEV_CAPTUREMOUSE = 0x00000200;
    int RIDEV_NOHOTKEYS = 0x00000200;
    int RIDEV_APPKEYS = 0x00000400;
    int RIDEV_EXINPUTSINK = 0x00001000;
    int RIDEV_DEVNOTIFY = 0x00002000;

    // See https://docs.microsoft.com/en-us/windows-hardware/drivers/hid/hid-usages#usage-page

    int HID_USAGE_PAGE_GENERIC = 0x01;
    int HID_USAGE_PAGE_GAME = 0x05;
    int HID_USAGE_PAGE_LED = 0x08;
    int HID_USAGE_PAGE_BUTTON = 0x09;

    // See https://docs.microsoft.com/en-us/windows-hardware/drivers/hid/hid-usages#usage-id

    int HID_USAGE_GENERIC_POINTER = 0x01;
    int HID_USAGE_GENERIC_MOUSE = 0x02;
    int HID_USAGE_GENERIC_JOYSTICK = 0x04;
    int HID_USAGE_GENERIC_GAMEPAD = 0x05;
    int HID_USAGE_GENERIC_KEYBOARD = 0x06;
    int HID_USAGE_GENERIC_KEYPAD = 0x07;
    int HID_USAGE_GENERIC_SYSTEM_CTL = 0x80;

    /**
     * Sent to the window that is getting raw input.
     *
     * A window receives this message through its WindowProc function.
     *
     * See <a>https://docs.microsoft.com/en-us/windows/win32/inputdev/wm-input</a>
     */
    int WM_INPUT = 0x00FF;
    /**
     * See <a>https://docs.microsoft.com/en-us/windows/win32/devio/wm-devicechange</a>
     */
    int WM_USB_DEVICECHANGE = 0x219;

    int RI_KEY_MAKE = 0;
    int RI_KEY_BREAK = 1;
    int RI_KEY_E0 = 2;
    int RI_KEY_E1 = 4;

    /** The device is a mouse. */
    int RIM_TYPEMOUSE = WinUser.RIM_TYPEMOUSE;
    /** The device is a keyboard. */
    int RIM_TYPEKEYBOARD = WinUser.RIM_TYPEKEYBOARD;
    /** The device is an HID that is not a keyboard and not a mouse. **/
    int RIM_TYPEHID = WinUser.RIM_TYPEHID;

    /**
     * @param pRawInputDeviceList
     *            An array of {@link RAWINPUTDEVICELIST} structures for the devices
     *            attached to the system. If (@code null}, the number of devices is
     *            returned in <tt>puiNumDevices</tt>
     * @param puiNumDevices
     *            If <tt>pRawInputDeviceList</tt> is {@code null}, the function populates
     *            this variable with the number of devices attached to the system;
     *            otherwise, this variable specifies the number of {@link RAWINPUTDEVICELIST}
     *            structures that can be contained in the buffer to which <tt>pRawInputDeviceList</tt>
     *            points. If this value is less than the number of devices attached to
     *            the system, the function returns the actual number of devices in this
     *            variable and fails with ERROR_INSUFFICIENT_BUFFER.
     * @param cbSize
     *            The size of a {@link RAWINPUTDEVICELIST} structure, in bytes.
     * @return If the function is successful, the return value is the number of devices
     *             stored in the buffer pointed to by <tt>pRawInputDeviceList</tt>. On
     *             any other error, the function returns -1 and {@code GetLastError}
     *             returns the error indication.
     * @see RAWINPUTDEVICELIST#sizeof()
     * @see <A HREF="https://msdn.microsoft.com/en-us/library/windows/desktop/ms645598(v=vs.85).aspx">GetRawInputDeviceList</A>
     */
    int GetRawInputDeviceList(RAWINPUTDEVICELIST[] pRawInputDeviceList, IntByReference puiNumDevices, int cbSize);

    /**
     * Defines information for the raw input devices.
     * <p>
     * Remarks
     * If RIDEV_NOLEGACY is set for a mouse or a keyboard,
     * the system does not generate any legacy message for that device for the application.
     * For example, if the mouse TLC is set with RIDEV_NOLEGACY, WM_LBUTTONDOWN and related legacy mouse messages
     * are not generated. Likewise, if the keyboard TLC is set with RIDEV_NOLEGACY, WM_KEYDOWN and related legacy
     * keyboard messages are not generated.
     * <p>
     * If RIDEV_REMOVE is set and the hwndTarget member is not set to NULL, then RegisterRawInputDevices function will fail.
     * <p>
     * See <a>https://docs.microsoft.com/en-us/windows/win32/api/winuser/ns-winuser-rawinputdevice</a>
     */
    @FieldOrder({"usUsagePage", "usUsage", "dwFlags", "hwndTarget"})
    class RAWINPUTDEVICE extends Structure {
        /**
         * Top level collection Usage page for the raw input device.
         * See HID Clients Supported in Windows for details on possible values.
         */
        public USHORT usUsagePage;
        /**
         * Top level collection Usage ID for the raw input device.
         * See HID Clients Supported in Windows for details on possible values.
         */
        public USHORT usUsage;
        /**
         * Mode flag that specifies how to interpret the information provided by usUsagePage and usUsage.
         * It can be zero (the default) or one of the following values.
         * By default, the operating system sends raw input from devices with the specified top level collection (TLC)
         * to the registered application as long as it has the window focus.
         * - RIDEV_REMOVE: If set, this removes the top level collection from the inclusion list.
         * This tells the operating system to stop reading from a device which matches the top level collection.
         * - RIDEV_EXCLUDE: If set, this specifies the top level collections to exclude when reading a complete usage page.
         * This flag only affects a TLC whose usage page is already specified with RIDEV_PAGEONLY.
         * - RIDEV_PAGEONLY: If set, this specifies all devices whose top level collection is from the specified usUsagePage.
         * Note that usUsage must be zero. To exclude a particular top level collection, use RIDEV_EXCLUDE.
         * - RIDEV_NOLEGACY: If set, this prevents any devices specified by usUsagePage or usUsage from generating legacy messages.
         * This is only for the mouse and keyboard. See Remarks.
         * - RIDEV_INPUTSINK: If set, this enables the caller to receive the input even when the caller is not in the  foreground.
         * Note that hwndTarget must be specified.
         * - RIDEV_CAPTUREMOUSE: If set, the mouse button click does not activate the other window.
         * RIDEV_CAPTUREMOUSE can be specified only if RIDEV_NOLEGACY is specified for a mouse device.
         * - RIDEV_NOHOTKEYS: If set, the application-defined keyboard device hotkeys are not handled.
         * However, the system hotkeys; for example, ALT+TAB and CTRL+ALT+DEL, are still handled.
         * By default, all keyboard hotkeys are handled. RIDEV_NOHOTKEYS can be specified even if
         * RIDEV_NOLEGACY is not specified and hwndTarget is NULL.
         * - RIDEV_APPKEYS:  If set, the application command keys are handled. RIDEV_APPKEYS can be specified only if
         * RIDEV_NOLEGACY is specified for a keyboard device.
         * - RIDEV_EXINPUTSINK: If set, this enables the caller to receive input in the background only if the foreground
         * application does not process it.
         * In other words, if the foreground application is not registered for raw input,
         * then the background application that is registered will receive the input.
         * Windows XP: This flag is not supported until Windows Vista
         * - RIDEV_DEVNOTIFY: If set, this enables the caller to receive WM_INPUT_DEVICE_CHANGE notifications for device
         * arrival and device removal.
         * Windows XP: This flag is not supported until Windows Vista
         */
        public int dwFlags;
        /**
         * A handle to the target window. If NULL it follows the keyboard focus.
         */
        public HWND hwndTarget;

        public RAWINPUTDEVICE() {}

        public RAWINPUTDEVICE(Pointer p) {
            super(p);
        }

        public int sizeof() {
            return calculateSize(false);
        }
    }

    /**
     * Retrieves information about the raw input device.
     *
     * @param hDevice   A handle to the raw input device.
     *                  This comes from the hDevice member of RAWINPUTHEADER or from GetRawInputDeviceList.
     * @param uiCommand Specifies what data will be returned in pData. This parameter can be one of the following values:
     *                  - RIDI_PREPARSEDDATA: pData is a PHIDP_PREPARSED_DATA pointer
     *                  to a buffer for a top-level collection's preparsed data.
     *                  - RIDI_DEVICENAME: pData points to a string that contains the device interface name.
     *                  <p>
     *                  If this device is opened with Shared Access Mode then you can call CreateFile
     *                  with this name to open a HID collection and use returned handle for calling
     *                  ReadFile to read input reports and WriteFile to send output reports.
     *                  <p>
     *                  For more information, see Opening HID Collections and Handling HID Reports.
     *                  <p>
     *                  For this uiCommand only, the value in pcbSize is the character count (not the byte count).
     *                  - RIDI_DEVICEINFO: pData points to an RID_DEVICE_INFO structure.
     * @param pData     A pointer to a buffer that contains the information specified by uiCommand.
     *                  If uiCommand is RIDI_DEVICEINFO, set the cbSize member of RID_DEVICE_INFO to sizeof(RID_DEVICE_INFO)
     *                  before calling GetRawInputDeviceInfo.
     * @param pcbSize   The size, in bytes, of the data in pData.
     * @return If successful, this function returns a non-negative number indicating the number of bytes copied to pData.
     * <p>
     * If pData is not large enough for the data, the function returns -1. If pData is NULL, the function returns a
     * value of zero. In both of these cases, pcbSize is set to the minimum size required for the pData buffer.
     * <p>
     * Call GetLastError to identify any other errors.
     */
    int GetRawInputDeviceInfo(HANDLE hDevice, int uiCommand, LPVOID pData, IntByReference pcbSize);

    /**
     * Retrieves the raw input from the specified device.
     * <p>
     * GetRawInputData gets the raw input one RAWINPUT structure at a time.
     * In contrast, GetRawInputBuffer gets an array of RAWINPUT structures.
     * <p>
     * See <a>https://docs.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getrawinputdata</a>
     *
     * @param hRawInput    A handle to the RAWINPUT structure. This comes from the lParam in WM_INPUT.
     * @param uiCommand    The command flag. This parameter can be one of the following values.
     *                     - RID_HEADER: Get the header information from the RAWINPUT structure.
     *                     - RID_INPUT: Get the raw data from the RAWINPUT structure.
     * @param pData        A pointer to the data that comes from the RAWINPUT structure. This depends on the value of uiCommand.
     *                     If pData is NULL, the required size of the buffer is returned in *pcbSize.
     * @param pcbSize      The size, in bytes, of the data in pData.
     * @param cbSizeHeader The size, in bytes, of the RAWINPUTHEADER structure.
     * @return If pData is NULL and the function is successful, the return value is 0.
     * If pData is not NULL and the function is successful, the return value is the number of bytes copied into pData.
     * <p>
     * If there is an error, the return value is (UINT)-1.
     */
    int GetRawInputData(LPVOID hRawInput, int uiCommand, Pointer pData, IntByReference pcbSize, int cbSizeHeader);

    /**
     * Performs a buffered read of the raw input messages data found in the calling thread's message queue.
     * <p>
     * Remarks
     * When an application receives raw input, its message queue gets a WM_INPUT message and the queue status flag QS_RAWINPUT is set.
     * <p>
     * Using GetRawInputBuffer, the raw input data is read in the array of variable size RAWINPUT structures and
     * corresponding WM_INPUT messages are removed from the calling thread's message queue.
     * You can call this method several times with buffer that cannot fit all message's data until all raw input messages
     * have been read.
     * <p>
     * The NEXTRAWINPUTBLOCK macro allows an application to traverse an array of RAWINPUT structures.
     * <p>
     * If all raw input messages have been successfully read from message queue then QS_RAWINPUT flag is cleared from the
     * calling thread's message queue status.
     * <p>
     * See <a>https://docs.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getrawinputbuffer</a>
     *
     * @param pData        A pointer to a buffer of RAWINPUT structures that contain the raw input data.
     *                     If NULL, size of the first raw input message data (minimum required buffer),
     *                     in bytes, is returned in *pcbSize.
     * @param pcbSize      The size, in bytes, of the provided RAWINPUT buffer.
     * @param cbSizeHeader The size, in bytes, of the RAWINPUTHEADER structure.
     * @return If pData is NULL and the function is successful, the return value is zero.
     * If pData is not NULL and the function is successful, the return value is the number of RAWINPUT structures written to pData.
     * <p>
     * If an error occurs, the return value is (UINT)-1. Call GetLastError for the error code.
     */
    int GetRawInputBuffer(RAWINPUT pData, IntByReference pcbSize, int cbSizeHeader);

    /**
     * Contains the raw input from a device.
     * <p>
     * The handle to this structure is passed in the lParam parameter of WM_INPUT.
     * <p>
     * To get detailed information -- such as the header and the content of the raw input -- call GetRawInputData.
     * <p>
     * To read the RAWINPUT in the message loop as a buffered read, call GetRawInputBuffer.
     * <p>
     * To get device specific information, call GetRawInputDeviceInfo with the hDevice from RAWINPUTHEADER.
     * <p>
     * Raw input is available only when the application calls RegisterRawInputDevices with valid device specifications.
     * <p>
     * See <a>https://docs.microsoft.com/en-us/windows/win32/api/winuser/ns-winuser-rawinput</a>
     */
    @FieldOrder({"header", "data"})
    class RAWINPUT extends Structure {
        /**
         * The raw input data.
         */
        public RAWINPUTHEADER header;
        public RAWINPUTDATA data;

        public RAWINPUT(Pointer p) {
            super(p);
            read();
        }

        @Override
        public Object readField(StructField structField) {
            if(structField.name.equals("data") && header != null)
                switch (header.dwType) {
                    case RIM_TYPEMOUSE -> data.setType(Pointer.class);
                    case RIM_TYPEKEYBOARD -> data.setType(RAWKEYBOARD.class);
                    case RIM_TYPEHID -> data.setType(RAWHID.class);
                }
            return super.readField(structField);
        }

        public int sizeof() {
            return calculateSize(false);
        }
    }

    /**
     * Contains the header information that is part of the raw input data.
     * <p>
     * Remarks
     * To get more information on the device, use hDevice in a call to GetRawInputDeviceInfo.
     * <p>
     * See <a>https://docs.microsoft.com/en-us/windows/win32/api/winuser/ns-winuser-rawinputheader</a>
     */
    @FieldOrder({"dwType", "dwSize", "hDevice", "wParam"})
    class RAWINPUTHEADER extends Structure {
        /**
         * The type of raw input. It can be one of the following values:
         * - RIM_TYPEMOUSE 0: Raw input comes from the mouse.
         * - RIM_TYPEKEYBOARD 1: Raw input comes from the keyboard.
         * - RIM_TYPEHID 2: Raw input comes from some device that is not a keyboard or a mouse.
         */
        public int dwType;
        /**
         * The size, in bytes, of the entire input packet of data.
         * This includes RAWINPUT plus possible extra input reports in the RAWHID variable length array.
         */
        public int dwSize;
        /**
         * A handle to the device generating the raw input data.
         */
        public HANDLE hDevice;
        /**
         * The value passed in the wParam parameter of the WM_INPUT message.
         */
        public WPARAM wParam;

        public int sizeof() {
            return calculateSize(false);
        }
    }

    /**
     * Contains the raw input from a device.
     * <p>
     * Remarks:
     * The handle to this structure is passed in the lParam parameter of WM_INPUT.
     * <p>
     * To get detailed information -- such as the header and the content of the raw input -- call GetRawInputData.
     * <p>
     * To read the RAWINPUT in the message loop as a buffered read, call GetRawInputBuffer.
     * <p>
     * To get device specific information, call GetRawInputDeviceInfo with the hDevice from RAWINPUTHEADER.
     * <p>
     * Raw input is available only when the application calls RegisterRawInputDevices with valid device specifications.
     * <p>
     * See <a>https://docs.microsoft.com/en-us/windows/win32/api/winuser/ns-winuser-rawinput</a>
     */
    @FieldOrder({"mouse", "keyboard", "hid"})
    class RAWINPUTDATA extends Union {
        /**
         * If the data comes from a mouse, this is the raw input data.
         */
        public Pointer mouse;
        /**
         * If the data comes from a keyboard, this is the raw input data.
         */
        public RAWKEYBOARD keyboard;
        /**
         * If the data comes from an HID, this is the raw input data.
         */
        public RAWHID hid;
    }

    /**
     * Contains information about the state of the keyboard.
     * <p>
     * Remarks
     * For a MakeCode value HID client mapper driver converts HID usages into scan codes according to USB HID to PS/2
     * Scan Code Translation Table (see PS/2 Set 1 Make column).
     * <p>
     * Older PS/2 keyboards actually transmit Scan Code Set 2 values down the wire from the keyboard to the keyboard port.
     * These values are translated to Scan Code Set 1 by the i8042 port chip. Possible values are listed in Keyboard
     * Scan Code Specification (see Scan Code Table).
     * <p>
     * KEYBOARD_OVERRUN_MAKE_CODE is a special MakeCode value sent when an invalid or unrecognizable combination of keys
     * is pressed or the number of keys pressed exceeds the limit for this keyboard.
     * <p>
     * See <a>https://docs.microsoft.com/en-us/windows/win32/api/winuser/ns-winuser-rawkeyboard</a>
     */
    @FieldOrder({"MakeCode", "Flags", "Reserved", "VKey", "Message", "ExtraInformation"})
    class RAWKEYBOARD extends Structure {
        /**
         * Specifies the scan code (from Scan Code Set 1) associated with a key press. See Remarks.
         */
        public USHORT MakeCode;
        /**
         * Flags for scan code information. It can be one or more of the following:
         * - RI_KEY_MAKE 0: The key is down.
         * - RI_KEY_BREAK 1: The key is up.
         * - RI_KEY_E0 2: The scan code has the E0 prefix.
         * - RI_KEY_E1 4: The scan code has the E1 prefix.
         */
        public USHORT Flags;
        /** Reserved; must be zero. */
        public USHORT Reserved;
        /** The corresponding legacy virtual-key code. */
        public USHORT VKey;
        /** The corresponding legacy keyboard window message, for example WM_KEYDOWN, WM_SYSKEYDOWN, and so forth. */
        public int Message;
        /** The device-specific additional information for the event. */
        public ULONG ExtraInformation;
    }

    /**
     * Describes the format of the raw input from a Human Interface Device (HID).
     * <p>
     * Remarks
     * <p>
     * Each WM_INPUT can indicate several inputs, but all of the inputs come from the same HID.
     * The size of the bRawData array is dwSizeHid * dwCount.
     * <p>
     * For more information, see Interpreting HID Reports.
     * <p>
     * See <a>https://docs.microsoft.com/en-us/windows/win32/api/winuser/ns-winuser-rawhid</a>
     */
    @FieldOrder({"dwSizeHid", "dwCount", "bRawData"})
    class RAWHID extends Structure {
        /**
         * The size, in bytes, of each HID input in bRawData.
         */
        public int dwSizeHid;
        /**
         * The number of HID inputs in bRawData.
         */
        public int dwCount;
        /**
         * The raw input data, as an array of bytes.
         */
        public byte[] bRawData = new byte[1];
    }

    /**
     * Registers the devices that supply the raw input data.
     * <p>
     * Remarks
     * To receive WM_INPUT messages, an application must first register the raw input devices using RegisterRawInputDevices.
     * By default, an application does not receive raw input.
     * <p>
     * To receive WM_INPUT_DEVICE_CHANGE messages, an application must specify the RIDEV_DEVNOTIFY flag for each device
     * class that is specified by the usUsagePage and usUsage fields of the RAWINPUTDEVICE structure . By default, an application does not receive WM_INPUT_DEVICE_CHANGE notifications for raw input device arrival and removal.
     * <p>
     * If a RAWINPUTDEVICE structure has the RIDEV_REMOVE flag set and the hwndTarget parameter is not set to NULL,
     * then parameter validation will fail.
     * <p>
     * Only one window per raw input device class may be registered to receive raw input within a process
     * (the window passed in the last call to RegisterRawInputDevices).
     * Because of this, RegisterRawInputDevices should not be used from a library, as it may interfere with any
     * raw input processing logic already present in applications that load it.
     *
     * @param pRawInputDevices An array of RAWINPUTDEVICE structures that represent the devices that supply the raw input.
     * @param uiNumDevices     The number of RAWINPUTDEVICE structures pointed to by pRawInputDevices.
     * @param cbSize           The size, in bytes, of a RAWINPUTDEVICE structure.
     * @return TRUE if the function succeeds; otherwise, FALSE. If the function fails, call GetLastError for more information.
     */
    boolean RegisterRawInputDevices(RAWINPUTDEVICE[] pRawInputDevices, int uiNumDevices, int cbSize);
}
