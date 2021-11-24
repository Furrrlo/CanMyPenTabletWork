package me.ferlo.cmptw.window;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import me.ferlo.cmptw.raw.RawInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

import static com.sun.jna.platform.win32.User32.*;

class WindowServiceImpl implements WindowService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WindowServiceImpl.class);

    private static final User32 USER32 = User32.INSTANCE;

    private static final int PM_NOREMOVE = 0x0000;
    private static final int PM_REMOVE = 0x0001;
    private static final int PM_NOYIELD = 0x0002;

    private static final int WM_STOP_EVENT_PUMP = WM_USER + 1;
    private static final int WM_EXECUTE_ON_PUMP_THREAD = WM_USER + 2;

    private final ExecutorService pumpExecutor = Executors.newSingleThreadExecutor(r -> {
        final var th = Executors.defaultThreadFactory().newThread(r);
        th.setName("RawKeyboardInputServiceImpl-message-pump");
        th.setDaemon(true);
        th.setUncaughtExceptionHandler((t, e) -> LOGGER.error("Thread '{}' crashed: ", t.getName(), e));
        return th;
    });
    private final Object lock = new Object();

    private volatile boolean registered;
    private final ConcurrentMap<Integer, WindowMessageListener> listeners = new ConcurrentHashMap<>();
    private final Queue<Callable<?>> toRunOnPumpThread = new ConcurrentLinkedQueue<>();
    private final ConcurrentMap<Callable<?>, CompletableFuture<?>> toRunOnPumpThreadToFuture = new ConcurrentHashMap<>();

    private HMODULE hInst;
    private WNDCLASSEX wndCls;
    private HWND hWnd;
    private MSG msg;
    private Thread wndThread;
    private Future<?> pumpEventFuture;

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
                            throw new WindowException();
                    }

                    wndCls = new WNDCLASSEX();
                    wndCls.hInstance = hInst;
                    wndCls.lpszClassName = "RawKeyboardInputServiceImpl";
                    wndCls.lpfnWndProc = (WindowProc) this::wndProc;

                    if (USER32.RegisterClassEx(wndCls).intValue() == 0)
                        throw new WindowException();

                    // Create an invisible Message-Only Window (https://msdn.microsoft.com/library/windows/desktop/ms632599.aspx#message_only)
                    final var windowHandle = USER32.CreateWindowEx(
                            0, wndCls.lpszClassName, null, 0,
                            0, 0, 0, 0, HWND_MESSAGE, null, hInst, null);
                    if (windowHandle == null)
                        throw new WindowException();
                    this.hWnd = new HWND(windowHandle.getPointer());
                    this.msg = new MSG();
                    this.wndThread = Thread.currentThread();

                    // Startup done
                    startupFuture.complete(null);
                } catch (Throwable t) {
                    startupFuture.completeExceptionally(t);
                    return;
                }

                // Start pumping events
                pumpEvents();
            });

            waitFutureAndPropagateException(startupFuture, "Failed to wait for startup");
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

            USER32.PostMessage(hWnd, WM_STOP_EVENT_PUMP, null, null);
            waitFutureAndPropagateException(pumpExecutor.submit(() -> {
                final List<Throwable> exceptions = new ArrayList<>();

                msg = null;

                if(Thread.currentThread() != wndThread)
                    exceptions.add(new IllegalStateException("Cannot destroy window if not on the window thread"));
                wndThread = null;

                if(!USER32.DestroyWindow(hWnd))
                    exceptions.add(new WindowException());
                hWnd = null;

                if(!USER32.UnregisterClass(wndCls.lpszClassName, hInst))
                    exceptions.add(new WindowException());
                wndCls = null;

                if(!exceptions.isEmpty()) {
                    final WindowException ex = new WindowException("Failed to unregister WindowServiceImpl");
                    exceptions.forEach(ex::addSuppressed);
                    throw ex;
                }
            }), "Failed to wait for shutdown");
        }
    }

    @Override
    public HWND getHwnd() {
        return hWnd;
    }

    @Override
    public void addListener(int uMsg, WindowMessageListener listener) {
        listeners.compute(uMsg, (k, v) -> {
            if(v != null)
                throw new IllegalArgumentException("Message " + k + " has already been hooked");
            return listener;
        });
    }

    @Override
    public void removeListener(int uMsg) {
        listeners.remove(uMsg);
    }

    @Override
    public void removeListener(WindowMessageListener listener) {
        listeners.values().remove(listener);
    }

    @Override
    public void removeListener(int uMsg, WindowMessageListener listener) {
        listeners.remove(uMsg, listener);
    }

    @Override
    public CompletableFuture<Void> runOnPumpThread(Runnable runnable) {
        return callOnPumpThread(() -> {
            runnable.run();
            return null;
        });
    }

    @Override
    public <T> CompletableFuture<T> callOnPumpThread(Callable<T> callable) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        toRunOnPumpThread.add(callable);
        toRunOnPumpThreadToFuture.put(callable, future);
        USER32.PostMessage(hWnd, WM_STOP_EVENT_PUMP, null, null);
        return future;
    }

    private void pumpEvents() {
        while (USER32.GetMessage(msg, hWnd, 0, 0) > 0) {
            USER32.TranslateMessage(msg);
            USER32.DispatchMessage(msg);
        }
    }

    @Override
    public void peekMessages(int wMsgFilterMin, int wMsgFilterMax) {
        if(Thread.currentThread() != wndThread)
            throw new IllegalStateException("Cannot peek event if not on the window thread");
        if(!registered || msg == null)
            return;

        while (USER32.PeekMessage(msg, hWnd, wMsgFilterMin, wMsgFilterMax, PM_NOREMOVE)) {
            USER32.TranslateMessage(msg);
            USER32.DispatchMessage(msg);
        }
    }

    private LRESULT wndProc(HWND hwnd, int uMsg, WPARAM wParam, LPARAM lParam) {
        try {
            if(uMsg == WM_STOP_EVENT_PUMP) {
                USER32.PostQuitMessage(0);
                return new LRESULT(0);
            }

            if(uMsg == WM_EXECUTE_ON_PUMP_THREAD) {
                final Callable<?> callable = toRunOnPumpThread.remove();
                final CompletableFuture<?> future = Objects.requireNonNull(toRunOnPumpThreadToFuture.remove(callable));

                try {
                    // Type is guaranteed to be correct
                    //noinspection ALL
                    ((CompletableFuture) future).complete(callable.call());
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }

                return new LRESULT(0);
            }

            final var listener = listeners.get(uMsg);
            if(listener != null)
                return listener.wndProc(hwnd, uMsg, wParam, lParam);
        } catch (Throwable t) {
            LOGGER.error("Uncaught exception in wndProc function", t);
        }

        return USER32.DefWindowProc(hwnd, uMsg, wParam, lParam);
    }

    private static <T> T waitFutureAndPropagateException(Future<T> future, String exception) throws RawInputException {
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new WindowException(exception, e);
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof Error || cause instanceof RuntimeException) {
                // Add this thread stacktrace to the exception
                cause.addSuppressed(new Exception("Called from here"));

                if (cause instanceof Error)
                    throw (Error) cause;
                // if(cause instanceof RuntimeException)
                throw (RuntimeException) cause;
            }
            throw new WindowException(exception, cause != null ? cause : e);
        }
    }
}
