package me.ferlo.cmptw.raw;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.WinUser.RAWINPUTDEVICELIST;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APITypeMapper;
import me.ferlo.cmptw.window.WindowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static com.sun.jna.platform.win32.WinDef.*;
import static me.ferlo.cmptw.raw.RawInput.*;

class RawKeyboardInputServiceImpl implements RawKeyboardInputService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawKeyboardInputServiceImpl.class);
    private static final RawInput RAW_INPUT = RawInput.INSTANCE;

    private final WindowService windowService;
    private final Object lock = new Object();
    private volatile boolean registered;

    private final Collection<RawInputKeyListener> listeners = ConcurrentHashMap.newKeySet();
    private final ConcurrentMap<HANDLE, RawInputDevice> devices = new ConcurrentHashMap<>();

    private RAWINPUT cachedRawInputObj;
    private Memory cachedRawInputMemory;

    private List<RawKeyEvent> polledEvents;

    RawKeyboardInputServiceImpl(WindowService windowService) {
        this.windowService = windowService;
    }

    @Override
    public void register() throws RawInputException {
        if(registered)
            return;

        synchronized (lock) {
            if(registered)
                return;

            registered = true;
            // Register handler
            final RAWINPUTDEVICE keyboardDevice = new RAWINPUTDEVICE();
            keyboardDevice.usUsagePage = new USHORT(HID_USAGE_PAGE_GENERIC);
            keyboardDevice.usUsage = new USHORT(HID_USAGE_GENERIC_KEYBOARD);
            keyboardDevice.dwFlags = RIDEV_INPUTSINK;
            keyboardDevice.hwndTarget = windowService.getHwnd();

            if (!RAW_INPUT.RegisterRawInputDevices(new RAWINPUTDEVICE[]{keyboardDevice}, 1, keyboardDevice.sizeof()))
                throw new RawInputException();

            windowService.addListener(WM_INPUT, this::wndProc);
            windowService.addListener(WM_USB_DEVICECHANGE, this::wndProc);

            // TODO: register change notifs

            // Get all current devices
            devices.putAll(getCurrentDeviceList());
        }
    }

    @Override
    public void unregister() throws RawInputException {
        if(!registered)
            return;

        synchronized (lock) {
            if (!registered)
                return;

            registered = false;
            windowService.removeListener(WM_INPUT);
            windowService.removeListener(WM_USB_DEVICECHANGE);
//            final List<Throwable> exceptions = new ArrayList<>();

            // TODO: remove change notifs
            // TODO: remove RegisterRawInputDevices

//            if(!exceptions.isEmpty()) {
//                final RawInputException ex = new RawInputException("Failed to unregister RawKeyboardInputServiceImpl");
//                exceptions.forEach(ex::addSuppressed);
//                throw ex;
//            }
        }
    }

    @Override
    public void addListener(RawInputKeyListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(RawInputKeyListener listener) {
        listeners.remove(listener);
    }

    @Override
    public List<RawKeyEvent> peek() {
        try {
            polledEvents = new ArrayList<>();
            windowService.peekMessages(WM_INPUT, WM_INPUT);
            return polledEvents;
        } finally {
            polledEvents = null;
        }
    }

    private LRESULT wndProc(HWND hwnd, int uMsg, WPARAM wParam, LPARAM lParam) {
        if (uMsg == WM_USB_DEVICECHANGE) {
            getCurrentDeviceList().forEach(devices::putIfAbsent);
            return new LRESULT(0);
        }

        if (uMsg == WM_INPUT)
            return handleRawInput(lParam.toPointer());

        throw new AssertionError("Unsupported message type " + uMsg);
    }

    private LRESULT handleRawInput(Pointer hRawInput) {
        final var sizeOfHeader = new RAWINPUTHEADER().sizeof();

        final var inputDataSizePtr = new IntByReference();
        if (RAW_INPUT.GetRawInputData(new LPVOID(hRawInput), RID_INPUT, null, inputDataSizePtr, sizeOfHeader) < 0)
            throw new RawInputException();

        final int inputDataSize = inputDataSizePtr.getValue();
        final var inputData = cachedRawInputMemory != null && cachedRawInputMemory.size() >= inputDataSize ?
                cachedRawInputMemory :
                new Memory(inputDataSize);

        int read;
        if ((read = RAW_INPUT.GetRawInputData(new LPVOID(hRawInput), RID_INPUT, inputData, inputDataSizePtr, sizeOfHeader)) < inputDataSize)
            throw new RawInputException("GetRawInputData did not return correct size (expected: " + inputDataSize + ", got: " + read + ')');

        final RAWINPUT input;
        if(inputData == cachedRawInputMemory) {
            input = cachedRawInputObj;
            input.read();
        } else {
            cachedRawInputMemory = inputData;
            input = cachedRawInputObj = new RAWINPUT(inputData);
        }

        // Only care about keyboards
        if (input.header.dwType != RIM_TYPEKEYBOARD)
            return new LRESULT(0);

        final RawKeyEvent evt = new RawKeyEvent(
                devices.get(input.header.hDevice), // TODO: add default
                ((input.data.keyboard.Flags.intValue() & RI_KEY_BREAK) != 0) ?
                        RawKeyEvent.State.UP :
                        RawKeyEvent.State.DOWN,
                input.data.keyboard.MakeCode.intValue(),
                input.data.keyboard.Flags.intValue(),
                input.data.keyboard.Reserved.intValue(),
                input.data.keyboard.VKey.intValue(),
                input.data.keyboard.Message,
                input.data.keyboard.ExtraInformation
        );

        // If we are polling the events, don't fire to the event listeners
        if(polledEvents != null)
            polledEvents.add(evt);
        else
            listeners.forEach(l -> l.onRawKeyEvent(evt));

        return new LRESULT(0);
    }

    private Map<HANDLE, RawInputDevice> getCurrentDeviceList() {
        final IntByReference numDevices = new IntByReference();
        final int sizeOfRAWINPUTDEVICELIST = new RAWINPUTDEVICELIST().sizeof();
        if(RAW_INPUT.GetRawInputDeviceList(null, numDevices, sizeOfRAWINPUTDEVICELIST) < 0)
            throw new RawInputException();

        final RAWINPUTDEVICELIST[] rawDeviceList = new RAWINPUTDEVICELIST[numDevices.getValue()];
        if(RAW_INPUT.GetRawInputDeviceList(rawDeviceList, numDevices, sizeOfRAWINPUTDEVICELIST) < 0)
            throw new RawInputException();

        return Arrays.stream(rawDeviceList)
                .filter(rawDevice -> rawDevice.dwType == RawInput.RIM_TYPEKEYBOARD || rawDevice.dwType == RawInput.RIM_TYPEHID)
                .collect(Collectors.toMap(
                        rawDevice -> new HANDLE(rawDevice.hDevice.getPointer()),
                        rawDevice -> {
                            String hwid;
                            try {
                                hwid = getDeviceHwid(rawDevice.hDevice);
                                if(hwid == null) hwid = "";
                            } catch (RawInputException ex) {
                                LOGGER.error("Failed to get device HWID for handle {}", rawDevice.hDevice, ex);
                                hwid = "";
                            }

                            String name;
                            try {
                                name = hwid.isEmpty() ? "" : getDeviceNameByHwid(hwid);
                            } catch (IllegalArgumentException ex) {
                                LOGGER.error("Failed to get device name for HWID {}", hwid, ex);
                                name = hwid;
                            }

                            return new RawInputDevice(hwid, name, rawDevice.dwType);
                        }));
    }

    private static String getDeviceHwid(HANDLE rawDevice) throws RawInputException {
        final IntByReference hwidSize = new IntByReference();
        if(RAW_INPUT.GetRawInputDeviceInfo(rawDevice, RawInput.RIDI_DEVICENAME, null, hwidSize) < 0)
            throw new RawInputException();

        if (hwidSize.getValue() == 0)
            return "";

        final var hwidPtr = new Memory((hwidSize.getValue() + 1L) * Native.WCHAR_SIZE);
        hwidPtr.clear();

        if(RAW_INPUT.GetRawInputDeviceInfo(rawDevice, RawInput.RIDI_DEVICENAME, new LPVOID(hwidPtr), hwidSize) < 0)
            throw new RawInputException();

        if (W32APITypeMapper.DEFAULT == W32APITypeMapper.UNICODE) {
            return hwidPtr.getWideString(0);
        } else {
            return hwidPtr.getString(0);
        }
    }

    private static String getDeviceNameByHwid(String hwid) {
        if(!hwid.startsWith("\\\\?\\"))
            throw new IllegalArgumentException("HWID doesn't start with \\\\?\\" + hwid);

        var parts = hwid.substring("\\\\?\\".length()).split("#");
        if(parts.length < 3)
            throw new IllegalArgumentException("HWID is missing some parts: " + hwid);

        final String desc = Advapi32Util.registryGetStringValue(
                WinReg.HKEY_LOCAL_MACHINE,
                String.format("System\\CurrentControlSet\\Enum\\%s\\%s\\%s", parts[0], parts[1], parts[2]),
                "DeviceDesc"
        );

        final int separatorIdx = desc.indexOf(";");
        if(separatorIdx < 0)
            throw new IllegalArgumentException("Device desc is missing ; separator: " + desc);
        return desc.substring(separatorIdx + 1);
    }
}
