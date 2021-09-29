package me.ferlo.cmptw.raw;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.platform.win32.WinUser.RAWINPUTDEVICELIST;
import com.sun.jna.platform.win32.WinUser.WNDCLASSEX;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APITypeMapper;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sun.jna.platform.win32.User32.HWND_MESSAGE;
import static com.sun.jna.platform.win32.WinDef.*;
import static me.ferlo.cmptw.raw.User32.*;

class RawKeyboardInputServiceImpl implements RawKeyboardInputService {

    private static final User32 USER32 = User32.INSTANCE;

    private final Object lock = new Object();
    private volatile boolean registered;

    private final Collection<RawInputKeyListener> listeners = ConcurrentHashMap.newKeySet();
    private final ConcurrentMap<HANDLE, RawInputDevice> devices = new ConcurrentHashMap<>();

    private HMODULE hInst;
    private WNDCLASSEX wndCls;
    private HWND hWnd;
    private Future<?> pumpEventFuture;

    private RAWINPUT cachedRawInputObj;
    private Memory cachedRawInputMemory;

    private final ExecutorService pumpExecutor = Executors.newSingleThreadExecutor(r -> {
        final var th = Executors.defaultThreadFactory().newThread(r);
        th.setName("RawKeyboardInputServiceImpl-message-pump");
        th.setDaemon(true);
        th.setUncaughtExceptionHandler((t, e) -> {
            // TODO: better logging
            System.err.println("Thread '" + t.getName() + "' crashed: ");
            e.printStackTrace();
        });
        return th;
    });

    @Override
    public void register() throws RawInputException {
        if(registered)
            return;

        synchronized (lock) {
            if(registered)
                return;

            registered = true;
            final CompletableFuture<Void> startupFuture = new CompletableFuture<>();
            pumpEventFuture = pumpExecutor.submit(() -> {
                try {
                    // register window
                    if (hInst == null) {
                        hInst = Kernel32.INSTANCE.GetModuleHandle(null);
                        if (hInst == null)
                            throw new RawInputException();
                    }

                    wndCls = new WNDCLASSEX();
                    wndCls.hInstance = hInst;
                    wndCls.lpszClassName = "RawKeyboardInputServiceImpl";
                    wndCls.lpfnWndProc = (WindowProc) this::wndProc;

                    if (USER32.RegisterClassEx(wndCls).intValue() == 0)
                        throw new RawInputException();

                    // Create an invisible Message-Only Window (https://msdn.microsoft.com/library/windows/desktop/ms632599.aspx#message_only)
                    final var windowHandle = USER32.CreateWindowEx(
                            0, wndCls.lpszClassName, null, 0,
                            0, 0, 0, 0, HWND_MESSAGE, null, hInst, null);
                    if (windowHandle == null)
                        throw new RawInputException();
                    this.hWnd = new HWND(windowHandle.getPointer());

                    // Register handler
                    final RAWINPUTDEVICE keyboardDevice = new RAWINPUTDEVICE();
                    keyboardDevice.usUsagePage = new USHORT(HID_USAGE_PAGE_GENERIC);
                    keyboardDevice.usUsage = new USHORT(HID_USAGE_GENERIC_KEYBOARD);
                    keyboardDevice.dwFlags = RIDEV_INPUTSINK;
                    keyboardDevice.hwndTarget = hWnd;

                    if (!USER32.RegisterRawInputDevices(new RAWINPUTDEVICE[]{keyboardDevice}, 1, keyboardDevice.sizeof()))
                        throw new RawInputException();

                    // TODO: register change notifs

                    // Get all current devices
                    devices.putAll(getCurrentDeviceList());

                    // Startup done
                    startupFuture.complete(null);
                } catch (Throwable t) {
                    startupFuture.completeExceptionally(t);
                    return;
                }

                // Start pumping events
                pumpEvents();
            });

            try {
                startupFuture.get();
            } catch (InterruptedException e) {
                throw new RawInputException("Failed to wait for startup", e);
            } catch (ExecutionException e) {
                final Throwable cause = e.getCause();
                if(cause instanceof Error || cause instanceof RuntimeException) {
                    // Add this thread stacktrace to the exception
                    cause.addSuppressed(new Exception("Called from here"));

                    if(cause instanceof Error)
                        throw (Error) cause;
                    // if(cause instanceof RuntimeException)
                    throw (RuntimeException) cause;
                }
                throw new RawInputException("Failed to wait for startup", cause != null ? cause : e);
            }
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
            final List<Throwable> exceptions = new ArrayList<>();

            pumpEventFuture.cancel(true);
            devices.clear();

            if(!USER32.DestroyWindow(hWnd))
                exceptions.add(new RawInputException());
            hWnd = null;

            if(!USER32.UnregisterClass(wndCls.lpszClassName, hInst))
                exceptions.add(new RawInputException());
            wndCls = null;

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

    private void pumpEvents() {
        MSG msg = new MSG();
        while (USER32.GetMessage(msg, hWnd, 0, 0) > 0) {
            USER32.TranslateMessage(msg);
            USER32.DispatchMessage(msg);
        }
    }

    private LRESULT wndProc(HWND hwnd, int uMsg, WPARAM wParam, LPARAM lParam) {
        try {
            if (uMsg == WM_USB_DEVICECHANGE) {
                getCurrentDeviceList().forEach(devices::putIfAbsent);
                return new LRESULT(0);
            }

            if (uMsg == WM_INPUT)
                return handleRawInput(lParam.toPointer());

        } catch (Throwable t) {
            // TODO: better logging
            System.err.println("Uncaught exception in wndProc function:");
            t.printStackTrace();
        }

        return USER32.DefWindowProc(hwnd, uMsg, wParam, lParam);
    }

    private LRESULT handleRawInput(Pointer hRawInput) {
        final var sizeOfHeader = new RAWINPUTHEADER().sizeof();

        final var inputDataSizePtr = new IntByReference();
        if (USER32.GetRawInputData(new LPVOID(hRawInput), RID_INPUT, null, inputDataSizePtr, sizeOfHeader) < 0)
            throw new RawInputException();

        final int inputDataSize = inputDataSizePtr.getValue();
        final var inputData = cachedRawInputMemory != null && cachedRawInputMemory.size() >= inputDataSize ?
                cachedRawInputMemory :
                new Memory(inputDataSize);

        int read;
        if ((read = USER32.GetRawInputData(new LPVOID(hRawInput), RID_INPUT, inputData, inputDataSizePtr, sizeOfHeader)) < inputDataSize)
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

        final RawKeyboardInputEvent evt = new RawKeyboardInputEvent(
                devices.get(input.header.hDevice), // TODO: add default
                ((input.data.keyboard.Flags.intValue() & RI_KEY_BREAK) != 0) ?
                        RawKeyboardInputEvent.State.UP :
                        RawKeyboardInputEvent.State.DOWN,
                input.data.keyboard.MakeCode.intValue(),
                input.data.keyboard.Flags.intValue(),
                input.data.keyboard.Reserved.intValue(),
                input.data.keyboard.VKey.intValue(),
                input.data.keyboard.Message,
                input.data.keyboard.ExtraInformation
        );
        listeners.forEach(l -> l.rawKeyEvent(evt));
        return new LRESULT(0);
    }

    private Map<HANDLE, RawInputDevice> getCurrentDeviceList() {
        final IntByReference numDevices = new IntByReference();
        final int sizeOfRAWINPUTDEVICELIST = new RAWINPUTDEVICELIST().sizeof();
        if(USER32.GetRawInputDeviceList(null, numDevices, sizeOfRAWINPUTDEVICELIST) < 0)
            throw new RawInputException();

        final RAWINPUTDEVICELIST[] rawDeviceList = new RAWINPUTDEVICELIST[numDevices.getValue()];
        if(USER32.GetRawInputDeviceList(rawDeviceList, numDevices, sizeOfRAWINPUTDEVICELIST) < 0)
            throw new RawInputException();

        return Arrays.stream(rawDeviceList)
                .filter(rawDevice -> rawDevice.dwType == User32.RIM_TYPEKEYBOARD || rawDevice.dwType == User32.RIM_TYPEHID)
                .map(rawDevice -> {
                    final String hwid;
                    try {
                        hwid = getDeviceHwid(rawDevice.hDevice);
                    } catch (RawInputException ex) {
                        // TODO: proper logging
                        ex.printStackTrace();
                        return null; // Skip
                    }

                    String name;
                    try {
                        name = getDeviceNameByHwid(hwid);
                    } catch (IllegalArgumentException ex) {
                        // TODO: proper logging
                        ex.printStackTrace();
                        name = hwid;
                    }

                    return new RawInputDevice(
                            hwid,
                            name,
                            rawDevice.dwType,
                            new RAWINPUTDEVICE(rawDevice.hDevice.getPointer())
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        d -> new HANDLE(d.handle().getPointer()),
                        Function.identity()));
    }

    private static String getDeviceHwid(HANDLE rawDevice) throws RawInputException {
        final IntByReference hwidSize = new IntByReference();
        if(USER32.GetRawInputDeviceInfo(rawDevice, User32.RIDI_DEVICENAME, null, hwidSize) < 0)
            throw new RawInputException();

        if (hwidSize.getValue() == 0)
            return "";

        final var hwidPtr = new Memory((hwidSize.getValue() + 1L) * Native.WCHAR_SIZE);
        hwidPtr.clear();

        if(USER32.GetRawInputDeviceInfo(rawDevice, User32.RIDI_DEVICENAME, new LPVOID(hwidPtr), hwidSize) < 0)
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
