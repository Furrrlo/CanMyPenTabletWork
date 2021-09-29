package me.ferlo.cmptw.hook;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeInputEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import me.ferlo.cmptw.raw.RawInputKeyListener;
import me.ferlo.cmptw.raw.RawKeyboardInputEvent;
import me.ferlo.cmptw.raw.RawKeyboardInputService;
import me.ferlo.cmptw.util.SameThreadExecutorService;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.*;

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
    private final RawInputKeyListener rawKeyListener = this::rawKeyEvent;

    private final Queue<SavedRawEvent> rawEventQueue = new ConcurrentLinkedQueue<>();

    @Override
    public void register() throws Exception {
        if(!registered)
            synchronized (lock) {
                if(!registered) {
                    registered = true;

                    GlobalScreen.setEventDispatcher(new SameThreadExecutorService());
                    GlobalScreen.registerNativeHook();
                    GlobalScreen.addNativeKeyListener(nativeKeyListener);

                    RawKeyboardInputService.INSTANCE.register();
                    RawKeyboardInputService.INSTANCE.addListener(rawKeyListener);
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

                    RawKeyboardInputService.INSTANCE.removeListener(rawKeyListener);
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

    private void rawKeyEvent(RawKeyboardInputEvent rawEvent) {
        rawEventQueue.add(new SavedRawEvent(rawEvent, System.currentTimeMillis()));
    }

    private void nativeKeyEvent(NativeKeyEvent nativeEvent) {
        nativeKeyEvent0(nativeEvent);
    }

    private boolean nativeKeyEvent0(NativeKeyEvent nativeEvent) {
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
