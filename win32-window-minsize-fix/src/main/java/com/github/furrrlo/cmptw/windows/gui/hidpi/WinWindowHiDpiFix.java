package com.github.furrrlo.cmptw.windows.gui.hidpi;

import com.sun.jna.*;
import com.sun.jna.platform.win32.BaseTSD.LONG_PTR;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.win32.W32APIOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class WinWindowHiDpiFix {

    private static final Logger LOGGER = LoggerFactory.getLogger(WinWindowHiDpiFix.class);
    private static final Map<HWND, WindowWrapper> HANDLE_TO_WINDOW_WRAPPER = new ConcurrentHashMap<>();

    private static final int GWLP_WNDPROC = -4;
    private static final int WM_DPICHANGED = 0x02E0;
    private static final int WM_GETMINMAXINFO = 0x0024;

    private WinWindowHiDpiFix() {
    }

    public static void install() {
        if(Platform.isWindows())
            Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
                if(!(event.getSource() instanceof Window window))
                    return;

                if(event.getID() == ComponentEvent.COMPONENT_SHOWN)
                    doInstall(window);
                else if(event.getID() == ComponentEvent.COMPONENT_HIDDEN)
                    doUninstall(window);
            }, AWTEvent.COMPONENT_EVENT_MASK);
    }

    private static void doInstall(Window window) {
        HANDLE_TO_WINDOW_WRAPPER.computeIfAbsent(new HWND(Native.getWindowPointer(window)), hWnd -> {
            int lastError;

            final var wrappedWndProc = User32.INSTANCE.GetWindowLongPtr(hWnd, GWLP_WNDPROC);
            if((wrappedWndProc == null || wrappedWndProc.intValue() == 0) && (lastError = Kernel32.INSTANCE.GetLastError()) != 0)
                throw new Win32Exception(lastError);

            Kernel32.INSTANCE.SetLastError(0);
            final Pointer res = User32.INSTANCE.SetWindowLongPtr(hWnd, GWLP_WNDPROC, WinWindowHiDpiFix::wndProc);
            if((res == null || res.equals(Pointer.createConstant(0))) && (lastError = Kernel32.INSTANCE.GetLastError()) != 0)
                throw new Win32Exception(lastError);

            final var myWndProc = User32.INSTANCE.GetWindowLongPtr(hWnd, GWLP_WNDPROC);
            if((myWndProc == null || myWndProc.intValue() == 0) && (lastError = Kernel32.INSTANCE.GetLastError()) != 0)
                throw new Win32Exception(lastError);

            return new WindowWrapper(hWnd, window, wrappedWndProc, myWndProc);
        });
    }

    private static void doUninstall(Window window) {
        final HWND hWnd = new HWND(Native.getWindowPointer(window));
        final WindowWrapper wrapper = HANDLE_TO_WINDOW_WRAPPER.get(hWnd);
        if(wrapper == null)
            return;

        int lastError;
        final var currWndProc = User32.INSTANCE.GetWindowLongPtr(hWnd, GWLP_WNDPROC);
        if((currWndProc == null || currWndProc.intValue() == 0) && (lastError = Kernel32.INSTANCE.GetLastError()) != 0)
            throw new Win32Exception(lastError);

        if(Objects.equals(currWndProc, wrapper.myWndProc)) {
            Kernel32.INSTANCE.SetLastError(0);
            final Pointer res = User32.INSTANCE.SetWindowLongPtr(hWnd, GWLP_WNDPROC, wrapper.wrappedWndProc.toPointer());
            if ((res == null || res.equals(Pointer.createConstant(0))) && (lastError = Kernel32.INSTANCE.GetLastError()) != 0)
                throw new Win32Exception(lastError);
        }
    }

    private static LRESULT wndProc(HWND hwnd, int uMsg, WPARAM wParam, LPARAM lParam) {
        final var wrapper = HANDLE_TO_WINDOW_WRAPPER.get(hwnd);
        if(wrapper == null) {
            LOGGER.error("Missing window wrapper for HWND {}, cannot handle msg {} with params {}, {}", hwnd, uMsg, wParam, lParam);
            return new LRESULT(-1);
        }

        try {
            return wrapper.wndProc(uMsg, wParam, lParam);
        } catch (Throwable t) {
            LOGGER.error("Failed to execute wndProc function for HWND {} and msg {} with params {}, {}", hwnd, uMsg, wParam, lParam, t);
            return new LRESULT(-1);
        }
    }

    private static class WindowWrapper {

        private final HWND hWnd;
        private final Window wrapped;
        final LONG_PTR wrappedWndProc;
        final LONG_PTR myWndProc;

        private float scaleX = -1, scaleY = -1;

        public WindowWrapper(HWND hWnd, Window wrapped, LONG_PTR wrappedWndProc, LONG_PTR myWndProc) {
            this.hWnd = hWnd;
            this.wrapped = wrapped;
            this.wrappedWndProc = wrappedWndProc;
            this.myWndProc = myWndProc;
        }

        LRESULT wndProc(int uMsg, WPARAM wParam, LPARAM lParam) {
            if(uMsg == WM_DPICHANGED) {
                final float dpiX = new DWORD(wParam.longValue()).getLow().longValue() / 96F;
                final float dpiY = new DWORD(wParam.longValue()).getHigh().longValue() / 96F;
                if (dpiX > 0 && dpiY > 0) {
                    this.scaleX = dpiX;
                    this.scaleY = dpiY;
                }
            }

            if(uMsg == WM_GETMINMAXINFO && wrapped.isMinimumSizeSet()) {
                // The transform is the same one used for rendering scaled icons
                // (see SunGraphics2D#getResolutionVariant(...)), so it has to be correct
                if(scaleX == -1 || scaleY == -1) {
                    final AffineTransform transform = wrapped.getGraphicsConfiguration().getDefaultTransform();
                    this.scaleX = (float) transform.getScaleX();
                    this.scaleY = (float) transform.getScaleY();
                }

                final LRESULT res = User32.INSTANCE.CallWindowProc(wrappedWndProc.toPointer(), hWnd, uMsg, wParam, lParam);
                final User32.MINMAXINFO info = new User32.MINMAXINFO(lParam);
                final Dimension minimumSize = wrapped.getMinimumSize();
                info.ptMinTrackSize.x = clipRound(minimumSize.width * scaleX);
                info.ptMinTrackSize.y = clipRound(minimumSize.height * scaleY);
                info.write();
                return res;
            }

            return User32.INSTANCE.CallWindowProc(wrappedWndProc.toPointer(), hWnd, uMsg, wParam, lParam);
        }

        private static int clipRound(double value) {
            // Copied off of https://github.com/openjdk/jdk/blob/739769c8fc4b496f08a92225a12d07414537b6c0/src/java.desktop/windows/native/libawt/windows/awt_Win32GraphicsDevice.cpp#L676-L690
            value -= 0.5;
            if (value < Integer.MIN_VALUE)
                return Integer.MIN_VALUE;
            if (value > Integer.MAX_VALUE)
                return Integer.MAX_VALUE;
            return (int) Math.ceil(value);
        }
    }

    private interface User32 extends com.sun.jna.platform.win32.User32 {

        User32 INSTANCE = Native.load("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);

        Pointer SetWindowLongPtr(HWND hWnd, int nIndex, WndProc dwNewLongPtr);

        interface WndProc extends Callback {

            @SuppressWarnings("unused")
            LRESULT wndProc(HWND hwnd, int uMsg, WPARAM wParam, LPARAM lParam);
        }

        @SuppressWarnings("unused")
        @Structure.FieldOrder({ "ptReserved", "ptMaxSize", "ptMaxPosition", "ptMinTrackSize", "ptMaxTrackSize" })
        class MINMAXINFO extends Structure implements Structure.ByReference {
            public POINT ptReserved;
            public POINT ptMaxSize;
            public POINT ptMaxPosition;
            public POINT ptMinTrackSize;
            public POINT ptMaxTrackSize;

            public MINMAXINFO(LPARAM lParam) {
                // See https://stackoverflow.com/a/31706927
                super(new Pointer(lParam.longValue()));
                read();
            }
        }
    }
}
