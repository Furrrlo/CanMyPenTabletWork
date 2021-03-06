package com.github.furrrlo.cmptw.windows.raw;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.DBT.DEV_BROADCAST_DEVICEINTERFACE;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinUser.RAWINPUTDEVICELIST;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APITypeMapper;
import com.github.furrrlo.cmptw.windows.window.WindowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static com.sun.jna.platform.win32.WinDef.*;
import static com.github.furrrlo.cmptw.windows.raw.RawInput.*;

class RawKeyboardInputServiceImpl implements RawKeyboardInputService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawKeyboardInputServiceImpl.class);
    private static final RawInput RAW_INPUT = RawInput.INSTANCE;
    private static final User32 USER32 = User32.INSTANCE;
    private static final RawInputDevice UNKNOWN_DEVICE = new RawInputDevice(
            "Unknwon",
            "Unknwon",
            "Unknwon",
            RawInput.RIM_TYPEHID
    );

    private final WindowService windowService;
    private final Object lock = new Object();
    private volatile boolean registered;
    private HDEVNOTIFY notifyHandle;

    private final Collection<RawInputKeyListener> listeners = ConcurrentHashMap.newKeySet();
    private final ConcurrentMap<HANDLE, RawInputDevice> devices = new ConcurrentHashMap<>();
    private final Collection<RawInputDevice> unmodifiableDevices = Collections.unmodifiableCollection(devices.values());

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

            // Register change notifications
            final var filter = new DEV_BROADCAST_DEVICEINTERFACE();
            filter.dbcc_size = filter.size();
            filter.dbcc_devicetype = DBT.DBT_DEVTYP_DEVICEINTERFACE;
            notifyHandle = USER32.RegisterDeviceNotification(
                    windowService.getHwnd(),
                    filter,
                    User32.DEVICE_NOTIFY_WINDOW_HANDLE | User32.DEVICE_NOTIFY_ALL_INTERFACE_CLASSES);
            if(notifyHandle == null)
                throw new RawInputException();

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
            final List<Throwable> exceptions = new ArrayList<>();

            // Remove change notifications
            if(notifyHandle != null && !USER32.UnregisterDeviceNotification(notifyHandle))
                exceptions.add(new RawInputException());

            // TODO: remove RegisterRawInputDevices

            if(!exceptions.isEmpty()) {
                final RawInputException ex = new RawInputException("Failed to unregister RawKeyboardInputServiceImpl");
                exceptions.forEach(ex::addSuppressed);
                throw ex;
            }
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

    private LRESULT wndProc(HWND hwnd, int uMsg, WPARAM wParam, LPARAM lParam) {
        if (uMsg == WM_USB_DEVICECHANGE)
            return handleDeviceChange();

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
                input.header.hDevice == null ?
                        UNKNOWN_DEVICE :
                        devices.getOrDefault(input.header.hDevice, UNKNOWN_DEVICE),
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
        else {
            listeners.forEach(l -> l.onRawKeyEvent(evt, () -> {
                try {
                    polledEvents = new ArrayList<>();
                    windowService.peekMessages(WM_INPUT, WM_INPUT);
                    return polledEvents;
                } finally {
                    polledEvents = null;
                }
            }));
        }

        return new LRESULT(0);
    }

    @Override
    public Collection<RawInputDevice> getDevices() {
        return unmodifiableDevices;
    }

    private LRESULT handleDeviceChange() {
        final Map<HANDLE, RawInputDevice> prevDevices = new HashMap<>(getCurrentDeviceList());
        final Map<HANDLE, RawInputDevice> newDevices = getCurrentDeviceList();

        final Set<RawInputDevice> added = new HashSet<>();
        final Set<RawInputDevice> removed = new HashSet<>();

        newDevices.forEach((handle, device) -> {
            final boolean wasAdded = devices.putIfAbsent(handle, device) == null;
            if(wasAdded)
                added.add(device);
        });
        prevDevices.forEach((handle, device) -> {
            if(newDevices.containsKey(handle))
                return;

            devices.remove(handle);
            removed.add(device);
        });

        listeners.forEach(l -> l.onDevicesChange(
                unmodifiableDevices,
                Collections.unmodifiableSet(added),
                Collections.unmodifiableSet(removed)));
        return new LRESULT(0);
    }

    private Map<HANDLE, RawInputDevice> getCurrentDeviceList() {
        final IntByReference numDevices = new IntByReference();
        final int sizeOfRAWINPUTDEVICELIST = new RAWINPUTDEVICELIST().sizeof();

        final RAWINPUTDEVICELIST[] rawDeviceList;
        for(int tries = 0; ; tries++) {
            if (RAW_INPUT.GetRawInputDeviceList(null, numDevices, sizeOfRAWINPUTDEVICELIST) < 0)
                throw new RawInputException();

            final RAWINPUTDEVICELIST[] rawDeviceList0 = new RAWINPUTDEVICELIST[numDevices.getValue()];
            if (RAW_INPUT.GetRawInputDeviceList(rawDeviceList0, numDevices, sizeOfRAWINPUTDEVICELIST) < 0) {
                final int err = Kernel32.INSTANCE.GetLastError();
                // If it was a 'not big enough' kind of error, and we have tried only a few times, retry.
                // This is because concurrency (probably), so the size value we got above was changed before we could use it.
                if(err == WinError.ERROR_INSUFFICIENT_BUFFER && tries <= 5)
                    continue;

                throw new RawInputException(err);
            }

            rawDeviceList = rawDeviceList0;
            break;
        }

        return Arrays.stream(rawDeviceList)
                .filter(rawDevice -> rawDevice.dwType == RawInput.RIM_TYPEKEYBOARD || rawDevice.dwType == RawInput.RIM_TYPEHID)
                .collect(Collectors.toMap(
                        rawDevice -> new HANDLE(rawDevice.hDevice.getPointer()),
                        rawDevice -> {
                            String deviceId;
                            try {
                                deviceId = getDeviceId(rawDevice.hDevice);
                                if(deviceId == null) deviceId = "";
                            } catch (RawInputException ex) {
                                LOGGER.error("Failed to get device HWID for handle {}", rawDevice.hDevice, ex);
                                deviceId = "";
                            }

                            String hwid;
                            try {
                                hwid = deviceId.isEmpty() ? "" : getDeviceHwidById(deviceId);
                            } catch (IllegalArgumentException ex) {
                                LOGGER.error("Failed to get device HWID for ID {}", deviceId, ex);
                                hwid = deviceId;
                            }

                            String name;
                            try {
                                name = deviceId.isEmpty() ? "" : getDeviceNameById(deviceId);
                            } catch (IllegalArgumentException ex) {
                                LOGGER.error("Failed to get device name for ID {}", deviceId, ex);
                                name = deviceId;
                            }

                            return new RawInputDevice(deviceId, hwid, name, rawDevice.dwType);
                        }));
    }

    private static String getDeviceId(HANDLE rawDevice) throws RawInputException {
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

    private static String getDeviceRegistryPath(String deviceId) {
        if(!deviceId.startsWith("\\\\?\\"))
            throw new IllegalArgumentException("HWID doesn't start with \\\\?\\" + deviceId);

        var parts = deviceId.substring("\\\\?\\".length()).split("#");
        if(parts.length < 3)
            throw new IllegalArgumentException("HWID is missing some parts: " + deviceId);

        return String.format("System\\CurrentControlSet\\Enum\\%s\\%s\\%s", parts[0], parts[1], parts[2]);
    }

    private static String getDeviceHwidById(String deviceId) {
        final String[] arr = Advapi32Util.registryGetStringArray(
                WinReg.HKEY_LOCAL_MACHINE,
                getDeviceRegistryPath(deviceId),
                "HardwareID"
        );

        if(arr.length <= 0)
            throw new IllegalArgumentException("Device hwid is missing");
        return arr[0];
    }

    private static String getDeviceNameById(String deviceId) {
        final String desc = Advapi32Util.registryGetStringValue(
                WinReg.HKEY_LOCAL_MACHINE,
                getDeviceRegistryPath(deviceId),
                "DeviceDesc"
        );

        final int separatorIdx = desc.indexOf(";");
        if(separatorIdx < 0)
            throw new IllegalArgumentException("Device desc is missing ; separator: " + desc);
        return desc.substring(separatorIdx + 1);
    }
}
