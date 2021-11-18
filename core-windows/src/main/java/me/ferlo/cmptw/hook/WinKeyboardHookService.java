package me.ferlo.cmptw.hook;

import com.sun.jna.platform.win32.User32;
import me.ferlo.cmptw.global.GlobalKeyEvent;
import me.ferlo.cmptw.global.GlobalKeyboardHookListener;
import me.ferlo.cmptw.global.GlobalKeyboardHookService;
import me.ferlo.cmptw.raw.RawInputDevice;
import me.ferlo.cmptw.raw.RawInputKeyListener;
import me.ferlo.cmptw.raw.RawKeyEvent;
import me.ferlo.cmptw.raw.RawKeyboardInputService;
import me.ferlo.cmptw.window.WindowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class WinKeyboardHookService implements KeyboardHookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WinKeyboardHookService.class);

    private final boolean isServicesOwner;
    private final WindowService windowService;
    private final RawKeyboardInputService rawKeyboardInputService;
    private final GlobalKeyboardHookService globalKeyboardHookService;

    private final Object lock = new Object();
    private volatile boolean registered;

    private final Collection<KeyboardHookListener> listeners = ConcurrentHashMap.newKeySet();
    private final GlobalKeyboardHookListener globalKeyListener = this::globalKeyEvent;
    private final RawInputKeyListener rawKeyListener = new RawInputKeyListener() {
        @Override
        public void onRawKeyEvent(RawKeyEvent rawEvent, Supplier<List<RawKeyEvent>> peek) {
            rawKeyEvent(rawEvent, peek);
        }

        @Override
        public void onDevicesChange(Collection<RawInputDevice> currentDevices,
                                    Collection<RawInputDevice> added,
                                    Collection<RawInputDevice> removed) {
            WinKeyboardHookService.this.onDevicesChange(currentDevices, added, removed);
        }
    };

    private final Queue<SavedRawEvent> rawEventQueue = new ConcurrentLinkedQueue<>();
    private final Queue<SavedNativeEvent> nativeEventQueue = new ConcurrentLinkedQueue<>();

    private final Map<RawInputDevice, WinRawKeyboardHookDevice> devices = new ConcurrentHashMap<>();
    private final Collection<KeyboardHookDevice> unmodifiableDevices = Collections.unmodifiableCollection(devices.values());

    public WinKeyboardHookService(WindowService windowService,
                                  RawKeyboardInputService rawKeyboardInputService,
                                  GlobalKeyboardHookService globalKeyboardHookService) {
        this.isServicesOwner = false;
        this.windowService = windowService;
        this.rawKeyboardInputService = rawKeyboardInputService;
        this.globalKeyboardHookService = globalKeyboardHookService;
    }

    public WinKeyboardHookService() {
        this.isServicesOwner = true;
        this.windowService = WindowService.create();
        this.rawKeyboardInputService = RawKeyboardInputService.create(windowService);
        this.globalKeyboardHookService = GlobalKeyboardHookService.create(windowService);
    }

    @Override
    public void register() throws Exception {
        if(!registered)
            synchronized (lock) {
                if(!registered) {
                    registered = true;

                    rawKeyboardInputService.addListener(rawKeyListener);
                    globalKeyboardHookService.addListener(globalKeyListener);

                    if(isServicesOwner) {
                        windowService.register();
                        rawKeyboardInputService.register();
                        globalKeyboardHookService.register();
                    }

                    // Init devices
                    rawKeyboardInputService.getDevices().forEach(device -> devices.put(device, new WinRawKeyboardHookDevice(device)));

                    // Init modifiers
                    devices.forEach((rawDevice, device) -> {
                        // TODO: this should be per device
                        final var keyboardState = new byte[WinVK.KB_STATE_SIZE];
                        User32.INSTANCE.GetKeyboardState(keyboardState);
                        // Check the high order bit
                        int currentModifiers = 0;
                        if ((keyboardState[WinVK.VK_LSHIFT]   & 0x80) != 0) currentModifiers |= KeyboardHookEvent.LSHIFT_MASK;
                        if ((keyboardState[WinVK.VK_RSHIFT]   & 0x80) != 0) currentModifiers |= KeyboardHookEvent.RSHIFT_MASK;
                        if ((keyboardState[WinVK.VK_LCONTROL] & 0x80) != 0) currentModifiers |= KeyboardHookEvent.LCTRL_MASK;
                        if ((keyboardState[WinVK.VK_RCONTROL] & 0x80) != 0) currentModifiers |= KeyboardHookEvent.RCTRL_MASK;
                        if ((keyboardState[WinVK.VK_LMENU]    & 0x80) != 0) currentModifiers |= KeyboardHookEvent.LALT_MASK;
                        if ((keyboardState[WinVK.VK_RMENU]    & 0x80) != 0) currentModifiers |= KeyboardHookEvent.RALT_MASK;
                        if ((keyboardState[WinVK.VK_LWIN]     & 0x80) != 0) currentModifiers |= KeyboardHookEvent.LMETA_MASK;
                        if ((keyboardState[WinVK.VK_RWIN]     & 0x80) != 0) currentModifiers |= KeyboardHookEvent.RMETA_MASK;

                        // For toggle keys, check the low order bit
                        if ((keyboardState[WinVK.VK_CAPITAL] & 0x01) != 0) currentModifiers |= KeyboardHookEvent.CAPS_LOCK_MASK;
                        if ((keyboardState[WinVK.VK_NUMLOCK] & 0x01) != 0) currentModifiers |= KeyboardHookEvent.NUM_LOCK_MASK;
                        if ((keyboardState[WinVK.VK_SCROLL] & 0x01) != 0) currentModifiers |= KeyboardHookEvent.SCROLL_LOCK_MASK;

                        device.setModifiersMask(currentModifiers);
                    });
                }
            }
    }

    @Override
    public void unregister() throws Exception {
        if (registered)
            synchronized (lock) {
                if (registered) {
                    registered = false;

                    globalKeyboardHookService.removeListener(globalKeyListener);
                    rawKeyboardInputService.removeListener(rawKeyListener);

                    if(isServicesOwner) {
                        globalKeyboardHookService.unregister();
                        rawKeyboardInputService.unregister();
                        windowService.unregister();
                    }
                }
            }
    }

    @Override
    public void addListener(KeyboardHookListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(KeyboardHookListener listener) {
        listeners.remove(listener);
    }

    @Override
    public Collection<KeyboardHookDevice> getDevices() {
        return unmodifiableDevices;
    }

    private void onDevicesChange(@SuppressWarnings("unused") Collection<RawInputDevice> currentDevices,
                                 Collection<RawInputDevice> added,
                                 Collection<RawInputDevice> removed) {
        devices.keySet().removeAll(removed);
        added.forEach(device -> devices.putIfAbsent(device, new WinRawKeyboardHookDevice(device)));
    }

    private boolean globalKeyEvent(GlobalKeyEvent globalEvent) {
        try {
            return globalKeyEvent0(globalEvent);
        } catch (Throwable ex) {
            LOGGER.error("Uncaught exception in GlobalHook listener", ex);
            return false;
        }
    }

    private boolean globalKeyEvent0(GlobalKeyEvent globalEvent) {
        // First search in already arrived raw messages
        final var maybeStoredRawEvent = globalKeyEvent0(globalEvent, rawEventQueue);
        if(maybeStoredRawEvent.isPresent())
            return dispatchEvent(maybeStoredRawEvent.get(), globalEvent);

        // If not found, wait for it to arrive
        final long timestamp = System.currentTimeMillis();
        final var future = new CompletableFuture<RawKeyEvent>();
        final var queuedEvent = new SavedNativeEvent(globalEvent, future, timestamp);
        nativeEventQueue.add(queuedEvent);

        // Can't return control to the OS, need to busy wait
        RawKeyEvent rawEvent;
        try {
            rawEvent = future.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException ex) {
            LOGGER.error("Failed to wait for future", ex);
            rawEvent = null;
        } catch (TimeoutException e) {
            rawEvent = null;
        }

        if(rawEvent == null) {
            nativeEventQueue.remove(queuedEvent);
            return false;
        }

        return dispatchEvent(rawEvent, globalEvent);
    }

    private Optional<RawKeyEvent> globalKeyEvent0(GlobalKeyEvent globalEvent, Collection<SavedRawEvent> rawEventQueue) {
        final long timestamp = System.currentTimeMillis();
        final var iter = rawEventQueue.iterator();

        while(iter.hasNext()) {
            final SavedRawEvent savedRawEvent = iter.next();
            final RawKeyEvent rawEvent = savedRawEvent.rawEvent();
            if(timestamp - savedRawEvent.timestamp() > 5000) {
                iter.remove();
                continue;
            }

            final boolean rawEventPressed = rawEvent.keyState() == RawKeyEvent.State.DOWN;
            final boolean globalEventPressed = globalEvent.isKeyDown();
            if(rawEventPressed == globalEventPressed && rawEvent.vKeyCode() == globalEvent.vKeyCode()) {
                iter.remove();
                return Optional.of(rawEvent);
            }
        }

        return Optional.empty();
    }

    private void rawKeyEvent(RawKeyEvent rawEvent, Supplier<List<RawKeyEvent>> peek) {
        try {
            rawKeyEvent0(rawEvent, peek);
        } catch (Throwable ex) {
            LOGGER.error("Uncaught exception in RawInput listener");
        }
    }

    private void rawKeyEvent0(RawKeyEvent rawEvent, Supplier<List<RawKeyEvent>> peek) {
        final long timestamp = System.currentTimeMillis();
        final var iter = nativeEventQueue.iterator();

        while(iter.hasNext()) {
            final SavedNativeEvent savedNativeEvent = iter.next();
            final GlobalKeyEvent globalEvent = savedNativeEvent.globalEvent();
            if(timestamp - savedNativeEvent.timestamp() > 5000) {
                savedNativeEvent.future.complete(null);
                iter.remove();
                continue;
            }

            final boolean globalEventPressed = globalEvent.isKeyDown();
            final var maybeRawEvent = Stream.<Supplier<Stream<RawKeyEvent>>>of(
                            () -> Stream.of(rawEvent),
                            () -> peek.get().stream())
                    .flatMap(Supplier::get)
                    .filter(rawEvent0 -> {
                        final boolean rawEventPressed = rawEvent0.keyState() == RawKeyEvent.State.DOWN;
                        return rawEventPressed == globalEventPressed && rawEvent0.vKeyCode() == globalEvent.vKeyCode();
                    })
                    .findFirst();
            if(maybeRawEvent.isPresent()) {
                savedNativeEvent.future.complete(maybeRawEvent.get());
                iter.remove();
                return;
            }
        }
        // Not matched, add it to the queue
        rawEventQueue.add(new SavedRawEvent(rawEvent, timestamp));
    }

    private boolean dispatchEvent(RawKeyEvent rawEvent, GlobalKeyEvent globalEvent) {
        final WinRawKeyboardHookDevice device = devices.computeIfAbsent(rawEvent.device(), WinRawKeyboardHookDevice::new);

        final int vKey = WinToAwtHelper.getLocatedVKey(globalEvent.vKeyCode(), globalEvent.scanCode(), globalEvent.isExtendedKey());
        final int modifier = switch (vKey) {
            case WinVK.VK_SHIFT, WinVK.VK_LSHIFT -> KeyboardHookEvent.LSHIFT_MASK;
            case WinVK.VK_RSHIFT -> KeyboardHookEvent.RSHIFT_MASK;
            case WinVK.VK_CONTROL, WinVK.VK_LCONTROL -> KeyboardHookEvent.LCTRL_MASK;
            case WinVK.VK_RCONTROL -> KeyboardHookEvent.RCTRL_MASK;
            case WinVK.VK_MENU, WinVK.VK_LMENU -> KeyboardHookEvent.LALT_MASK;
            case WinVK.VK_RMENU -> KeyboardHookEvent.RALT_MASK;
            case WinVK.VK_LWIN -> KeyboardHookEvent.LMETA_MASK;
            case WinVK.VK_RWIN -> KeyboardHookEvent.RMETA_MASK;
            case WinVK.VK_NUMLOCK -> KeyboardHookEvent.NUM_LOCK_MASK;
            case WinVK.VK_CAPITAL -> KeyboardHookEvent.CAPS_LOCK_MASK;
            case WinVK.VK_SCROLL -> KeyboardHookEvent.SCROLL_LOCK_MASK;
            default -> 0;
        };

        if(modifier != 0) {
            final boolean isLockModifier = switch (vKey) {
                case WinVK.VK_NUMLOCK, WinVK.VK_CAPITAL, WinVK.VK_SCROLL -> true;
                default -> false;
            };

            int currentModifiers = device.getModifiersMask();
            if(!isLockModifier) {
                if(rawEvent.keyState() == RawKeyEvent.State.DOWN)
                    currentModifiers |= modifier;
                else
                    currentModifiers &= ~modifier;
            } else {
                if(rawEvent.keyState() == RawKeyEvent.State.DOWN)
                    currentModifiers ^= modifier;
            }
            device.setModifiersMask(currentModifiers);
        }

        // If alt is not in the modifiers, but the global event says it should, force it to be there
        final int modifiers = (globalEvent.isAltPressed() && (device.getModifiersMask() & KeyboardHookEvent.ALT_MASK) == 0) ?
                device.getModifiersMask() | KeyboardHookEvent.LALT_MASK :
                device.getModifiersMask();

        final KeyboardHookEvent evt = new KeyboardHookEvent(
                device,
                vKey,
                globalEvent.scanCode(),
                globalEvent.isExtendedKey(),
                modifiers,
                WinToAwtHelper.winVKeyToAwtKey(globalEvent.vKeyCode(), modifiers),
                WinToAwtHelper.getAwtKeyLocation(globalEvent.vKeyCode(), globalEvent.scanCode(), globalEvent.isExtendedKey()),
                globalEvent.wasKeyDown(),
                globalEvent.isKeyDown(),
                globalEvent.repeatCount()
        );
        return listeners.stream().sequential().anyMatch(l -> l.onKeyHook(evt));
    }

    private record SavedRawEvent(RawKeyEvent rawEvent, long timestamp) {
    }

    private record SavedNativeEvent(GlobalKeyEvent globalEvent, CompletableFuture<RawKeyEvent> future, long timestamp) {
    }
}
