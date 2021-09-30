package me.ferlo.cmptw.hook;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeInputEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import me.ferlo.cmptw.raw.RawKeyboardInputEvent;
import me.ferlo.cmptw.raw.RawKeyboardInputService;
import me.ferlo.cmptw.util.SameThreadExecutorService;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class KeyboardHookServiceImpl implements KeyboardHookService {

    private static final MethodHandle NATIVE_INPUT_EVENT_SET_RESERVED;
    static {
        try {
            Method m = NativeInputEvent.class.getDeclaredMethod("setReserved", short.class);
            m.setAccessible(true);
            NATIVE_INPUT_EVENT_SET_RESERVED = MethodHandles.lookup().unreflect(m);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new AssertionError("Failed to ");
        }
    }

    private final Object lock = new Object();
    private volatile boolean registered;

    private final Collection<KeyboardHookListener> listeners = ConcurrentHashMap.newKeySet();
    private final NativeKeyListener nativeKeyListener = new NativeKeyListener() {
        @Override
        public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
            nativeKeyEvent(nativeEvent);
        }
        @Override
        public void nativeKeyReleased(NativeKeyEvent nativeEvent) {
            nativeKeyEvent(nativeEvent);
        }
    };

    private final List<SavedRawEvent> rawEventQueue = new ArrayList<>();

    @Override
    public void register() throws Exception {
        if(!registered)
            synchronized (lock) {
                if(!registered) {
                    registered = true;

                    RawKeyboardInputService.INSTANCE.register();

                    GlobalScreen.registerNativeHook();
                    // We need our hooks to be executed on the JNativeHook event generation thread
                    // so that we can properly delete events.
                    // Also, add it after registerNativeHook(), as it looks like otherwise it shuts it down (?)
                    GlobalScreen.setEventDispatcher(new SameThreadExecutorService());
                    GlobalScreen.addNativeKeyListener(nativeKeyListener);
                }
            }
    }

    @Override
    public void unregister() throws Exception {
        if (registered)
            synchronized (lock) {
                if (registered) {
                    registered = false;

                    GlobalScreen.removeNativeKeyListener(nativeKeyListener);
                    GlobalScreen.unregisterNativeHook();

                    RawKeyboardInputService.INSTANCE.unregister();
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

    private void nativeKeyEvent(NativeKeyEvent nativeEvent) {
        try {
            nativeKeyEvent0(nativeEvent);
        } catch (Throwable ex) {
            // TODO: logging
            System.err.println("Uncaught exception in JNativeHook listener: ");
            ex.printStackTrace();
        }
    }

    private void nativeKeyEvent0(NativeKeyEvent nativeEvent) {
        // See https://github.com/me2d13/luamacros/blob/a0bda6c4c7b38c1bceb2217a9e38bf402eaead87/src/ukeylogservice.pas#L74
        // First search in already arrived raw messages
        if(nativeKeyEvent0(nativeEvent, rawEventQueue))
            return;
        rawEventQueue.addAll(RawKeyboardInputService.INSTANCE.poll().stream()
                .map(e -> new SavedRawEvent(e, System.currentTimeMillis()))
                .toList());
        nativeKeyEvent0(nativeEvent, rawEventQueue);
    }

    private boolean nativeKeyEvent0(NativeKeyEvent nativeEvent, Collection<SavedRawEvent> rawEventQueue) {
        // See https://github.com/me2d13/luamacros/blob/a0bda6c4c7b38c1bceb2217a9e38bf402eaead87/src/ukeylogservice.pas#L101
        final long timestamp = System.currentTimeMillis();
        final var iter = rawEventQueue.iterator();

        while(iter.hasNext()) {
            final SavedRawEvent savedRawEvent = iter.next();
            final RawKeyboardInputEvent rawEvent = savedRawEvent.rawEvent();
            if(timestamp - savedRawEvent.timestamp() > 5000) {
                iter.remove();
                continue;
            }

            final boolean rawEventPressed = rawEvent.keyState() == RawKeyboardInputEvent.State.DOWN;
            final boolean nativeEventPressed = nativeEvent.getID() == NativeKeyEvent.NATIVE_KEY_PRESSED;
            if(rawEventPressed == nativeEventPressed && rawEvent.vKey() == nativeEvent.getRawCode()) {
                dispatchEvent(rawEvent, nativeEvent, rawEventPressed);
                iter.remove();
                return true;
            }
        }

        return false;
    }

    private void dispatchEvent(RawKeyboardInputEvent rawEvent, NativeKeyEvent nativeEvent, boolean isKeyPress) {
        final KeyboardHookEvent evt = new KeyboardHookEvent(rawEvent, nativeEvent, isKeyPress);
        for(KeyboardHookListener listener : listeners) {
            if(isKeyPress)
                listener.keyPressed(evt);
            else
                listener.keyReleased(evt);

            if(evt.isCancelled())
                break;
        }

        if(evt.isCancelled()) {
            try {
                NATIVE_INPUT_EVENT_SET_RESERVED.invoke(nativeEvent, (short) 0x01);
            } catch (Throwable t) {
                // TODO: better logging
                System.err.println("Failed to invoke NativeInputEvent#setReserved(short)");
                t.printStackTrace();
                System.exit(-1);
            }
        }
    }

    private record SavedRawEvent(RawKeyboardInputEvent rawEvent, long timestamp) {
    }
}
