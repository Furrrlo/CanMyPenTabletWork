package me.ferlo.cmptw.hook;

import me.ferlo.cmptw.global.GlobalKeyEvent;
import me.ferlo.cmptw.global.GlobalKeyboardHookListener;
import me.ferlo.cmptw.global.GlobalKeyboardHookService;
import me.ferlo.cmptw.raw.RawInputKeyListener;
import me.ferlo.cmptw.raw.RawKeyEvent;
import me.ferlo.cmptw.raw.RawKeyboardInputService;

import java.util.Collection;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class KeyboardHookServiceImpl implements KeyboardHookService {

    private final Object lock = new Object();
    private volatile boolean registered;

    private final Collection<KeyboardHookListener> listeners = ConcurrentHashMap.newKeySet();
    private final GlobalKeyboardHookListener globalKeyListener = this::globalKeyEvent;
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

                    GlobalKeyboardHookService.INSTANCE.register();
                    GlobalKeyboardHookService.INSTANCE.addListener(globalKeyListener);
                }
            }
    }

    @Override
    public void unregister() throws Exception {
        if (registered)
            synchronized (lock) {
                if (registered) {
                    registered = false;

                    GlobalKeyboardHookService.INSTANCE.removeListener(globalKeyListener);
                    GlobalKeyboardHookService.INSTANCE.unregister();

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

    private boolean globalKeyEvent(GlobalKeyEvent globalEvent) {
        try {
            return globalKeyEvent0(globalEvent);
        } catch (Throwable ex) {
            // TODO: logging
            System.err.println("Uncaught exception in JNativeHook listener: ");
            ex.printStackTrace();
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
        //noinspection StatementWithEmptyBody
        while((rawEvent = future.getNow(null)) == null && System.currentTimeMillis() - timestamp < 1000);

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

    private void rawKeyEvent(RawKeyEvent rawEvent) {
        try {
            rawKeyEvent0(rawEvent);
        } catch (Throwable ex) {
            // TODO: logging
            System.err.println("Uncaught exception in RawInput listener: ");
            ex.printStackTrace();
        }
    }

    private void rawKeyEvent0(RawKeyEvent rawEvent) {
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
                            () -> RawKeyboardInputService.INSTANCE.peek().stream())
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
        final KeyboardHookEvent evt = new KeyboardHookEvent(rawEvent, globalEvent);
        return listeners.stream().sequential().anyMatch(l -> l.onKeyHook(evt));
    }

    private record SavedRawEvent(RawKeyEvent rawEvent, long timestamp) {
    }

    private record SavedNativeEvent(GlobalKeyEvent globalEvent, CompletableFuture<RawKeyEvent> future, long timestamp) {
    }
}
