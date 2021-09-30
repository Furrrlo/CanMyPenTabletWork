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
import java.util.function.Supplier;
import java.util.stream.Stream;

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
    };
    private final RawInputKeyListener rawKeyListener = this::rawKeyEvent;

    private final Queue<SavedRawEvent> rawEventQueue = new ConcurrentLinkedQueue<>();
    private final Queue<SavedNativeEvent> nativeEventQueue = new ConcurrentLinkedQueue<>();

    @Override
    public void register() throws Exception {
        if(!registered)
            synchronized (lock) {
                if(!registered) {
                    registered = true;

                    RawKeyboardInputService.INSTANCE.register();
                    RawKeyboardInputService.INSTANCE.addListener(rawKeyListener);

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
        // First search in already arrived raw messages
        if(nativeKeyEvent0(nativeEvent, rawEventQueue))
            return;
        // If not found, wait for it to arrive
        final long timestamp = System.currentTimeMillis();
        final var future = new CompletableFuture<RawKeyboardInputEvent>();
        final var queuedEvent = new SavedNativeEvent(nativeEvent, future, timestamp);
        nativeEventQueue.add(queuedEvent);

        // Can't return control to the OS, need to busy wait
        RawKeyboardInputEvent rawEvent;
        //noinspection StatementWithEmptyBody
        while((rawEvent = future.getNow(null)) == null && System.currentTimeMillis() - timestamp < 1000);

        if(rawEvent != null) {
            System.out.println("Matched in " + (System.currentTimeMillis() - timestamp));
            dispatchEvent(rawEvent, nativeEvent);
        } else {
            System.out.println("Timeout expired, not matched");
            nativeEventQueue.remove(queuedEvent);
        }
    }

    private boolean nativeKeyEvent0(NativeKeyEvent nativeEvent, Collection<SavedRawEvent> rawEventQueue) {
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
                dispatchEvent(rawEvent, nativeEvent);
                iter.remove();
                return true;
            }
        }

        return false;
    }

    private void rawKeyEvent(RawKeyboardInputEvent rawEvent) {
        try {
            rawKeyEvent0(rawEvent);
        } catch (Throwable ex) {
            // TODO: logging
            System.err.println("Uncaught exception in RawInput listener: ");
            ex.printStackTrace();
        }
    }

    private void rawKeyEvent0(RawKeyboardInputEvent rawEvent) {
        final long timestamp = System.currentTimeMillis();
        final var iter = nativeEventQueue.iterator();

        while(iter.hasNext()) {
            final SavedNativeEvent savedNativeEvent = iter.next();
            final NativeKeyEvent nativeEvent = savedNativeEvent.nativeEvent();
            System.out.println("Arrived after " + (timestamp - savedNativeEvent.timestamp()));
            if(timestamp - savedNativeEvent.timestamp() > 5000) {
                savedNativeEvent.future.complete(null);
                iter.remove();
                continue;
            }

            final boolean nativeEventPressed = nativeEvent.getID() == NativeKeyEvent.NATIVE_KEY_PRESSED;
            final var maybeRawEvent = Stream.<Supplier<Stream<RawKeyboardInputEvent>>>of(
                            () -> Stream.of(rawEvent),
                            () -> RawKeyboardInputService.INSTANCE.peek().stream())
                    .flatMap(Supplier::get)
                    .filter(rawEvent0 -> {
                        final boolean rawEventPressed = rawEvent0.keyState() == RawKeyboardInputEvent.State.DOWN;
                        return rawEventPressed == nativeEventPressed && rawEvent0.vKey() == nativeEvent.getRawCode();
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

    private void dispatchEvent(RawKeyboardInputEvent rawEvent, NativeKeyEvent nativeEvent) {
        final KeyboardHookEvent evt = new KeyboardHookEvent(rawEvent, nativeEvent);
        for(KeyboardHookListener listener : listeners) {
            listener.keyPressed(evt);
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

    private record SavedNativeEvent(NativeKeyEvent nativeEvent, CompletableFuture<RawKeyboardInputEvent> future, long timestamp) {
    }
}
