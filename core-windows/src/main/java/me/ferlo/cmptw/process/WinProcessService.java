package me.ferlo.cmptw.process;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HICON;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFOHEADER;
import com.sun.jna.platform.win32.WinGDI.ICONINFO;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APITypeMapper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class WinProcessService implements ProcessService {

    private static final int[] WIN32_ICO_SIZES = { 16, 24, 32, 48, 64, 72, 96, 128, 180, 256 };

    private List<BufferedImage> fallbackIcons;

    @Override
    public Optional<Process> getProcessForPid(long pid) {
        return Optional.ofNullable(createWinProcess((int) pid));
    }

    @Override
    public Collection<Process> enumerateProcesses() {

        int[] processes = new int[0];
        int returnedProcesses = 0;

        int size = 1024;
        while (size < 1024 * 10) {
            processes = new int[size];
            final IntByReference returnedProcessesInBytes = new IntByReference();
            if(!Psapi.INSTANCE.EnumProcesses(processes, processes.length * WinDef.DWORD.SIZE, returnedProcessesInBytes))
                throw new Win32Exception(Kernel32.INSTANCE.GetLastError());

            returnedProcesses = returnedProcessesInBytes.getValue() / WinDef.DWORD.SIZE;
            if(returnedProcesses > processes.length)
                break;

            size = size * 2;
        }

        final int[] processes0 = processes;
        return Arrays.stream(processes0, 0, returnedProcesses)
                .mapToObj(this::createWinProcess)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public List<BufferedImage> extractProcessIcons(Path processFile) {
        final List<BufferedImage> icons = Arrays.stream(WIN32_ICO_SIZES)
                .sequential()
                .mapToObj(size -> {
                    HICON[] hIcon = new HICON[1];
                    int res = Shell32.INSTANCE.SHDefExtractIcon(
                            processFile.toAbsolutePath().toString(),
                            0, 0,
                            hIcon, null, size);

                    if(res != WinError.S_OK.intValue())
                        return null;

                    try {
                        return convertToJavaImage(hIcon[0]);
                    } finally {
                        User32.INSTANCE.DestroyIcon(hIcon[0]);
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        return !icons.isEmpty() ? icons : getFallbackIcons();
    }

    @Override
    public List<BufferedImage> getFallbackIcons() {
        if(fallbackIcons == null)
            // TODO: default win icon fallback?
            fallbackIcons = List.of(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE));
        return fallbackIcons;
    }

    /**
     * @see WindowUtils.NativeWindowUtils#getWindowIcon(WinDef.HWND)
     * See <a>https://github.com/openjdk/jdk/blob/7f52c50ba32eecf5f379f8db30ac6a5cc50b3b66/src/java.desktop/windows/native/libawt/windows/ShellFolder2.cpp#L1013</a>
     */
    @SuppressWarnings("JavadocReference")
    private static BufferedImage convertToJavaImage(HICON hIcon) {
        // draw native icon into Java image
        final Dimension iconSize = WindowUtils.getIconSize(hIcon);
        if (iconSize.width == 0 || iconSize.height == 0)
            return null;

        final int width = iconSize.width;
        final int height = iconSize.height;
        final short depth = 32;

        final byte[] lpBitsColor = new byte[width * height * depth / 8];
        final Pointer lpBitsColorPtr = new Memory(lpBitsColor.length);
        final byte[] lpBitsMask = new byte[width * height * depth / 8];
        final Pointer lpBitsMaskPtr = new Memory(lpBitsMask.length);
        final BITMAPINFO bitmapInfo = new BITMAPINFO();
        final BITMAPINFOHEADER hdr = new BITMAPINFOHEADER();

        bitmapInfo.bmiHeader = hdr;
        hdr.biWidth = width;
        hdr.biHeight = height;
        hdr.biPlanes = 1;
        hdr.biBitCount = depth;
        hdr.biCompression = 0;
        hdr.write();
        bitmapInfo.write();

        final HDC hDC = User32.INSTANCE.GetDC(null);
        final ICONINFO iconInfo = new ICONINFO();
        User32.INSTANCE.GetIconInfo(hIcon, iconInfo);
        iconInfo.read();
        GDI32.INSTANCE.GetDIBits(hDC, iconInfo.hbmColor, 0, height,
                lpBitsColorPtr, bitmapInfo, 0);
        lpBitsColorPtr.read(0, lpBitsColor, 0, lpBitsColor.length);
        GDI32.INSTANCE.GetDIBits(hDC, iconInfo.hbmMask, 0, height,
                lpBitsMaskPtr, bitmapInfo, 0);
        lpBitsMaskPtr.read(0, lpBitsMask, 0, lpBitsMask.length);
        final BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);

        boolean hasAlpha = false;
//        if (IS_WINXP)
            for (int i = 0; i < lpBitsColor.length; i = i + 4) {
                if ((lpBitsColor[i + 3] & 0xff) != 0) {
                    hasAlpha = true;
                    break;
                }
            }

        int r, g, b, a, argb;
        int x = 0, y = height - 1;
        for (int i = 0; i < lpBitsColor.length; i = i + 4) {
            b = lpBitsColor[i] & 0xFF;
            g = lpBitsColor[i + 1] & 0xFF;
            r = lpBitsColor[i + 2] & 0xFF;
            a = hasAlpha ? lpBitsColor[i + 3] : 0xFF - lpBitsMask[i] & 0xFF;
            argb = (a << 24) | (r << 16) | (g << 8) | b;
            image.setRGB(x, y, argb);
            x = (x + 1) % width;
            if (x == 0)
                y--;
        }

        User32.INSTANCE.ReleaseDC(null, hDC);
        return image;
    }

    private Process createWinProcess(int pid) {
        HANDLE hProcess = Kernel32.INSTANCE.OpenProcess(Kernel32.PROCESS_QUERY_INFORMATION | Kernel32.PROCESS_VM_READ, false, pid);
        if (hProcess == null)
            return null;

        try {
            HMODULE[] hMod = new HMODULE[1];
            final var cbNeeded = new IntByReference();
            if (!Psapi.INSTANCE.EnumProcessModulesEx(hProcess, hMod, Native.POINTER_SIZE, cbNeeded, Psapi.LIST_MODULES_ALL))
                throw new Win32Exception(Kernel32.INSTANCE.GetLastError());

            return new WinProcess(pid, getProcessName(hProcess, hMod[0]), Path.of(getProcessFileName(hProcess, hMod[0])));
        } finally {
            Kernel32.INSTANCE.CloseHandle(hProcess);
        }
    }

    private String getProcessName(HANDLE hProcess, HMODULE hMod) {
        final var szProcessName = new Memory((long) (Kernel32.MAX_PATH + 1) * Native.WCHAR_SIZE);
        if (Psapi.INSTANCE.GetModuleBaseName(hProcess, hMod, szProcessName, (int) (szProcessName.size() / Native.WCHAR_SIZE)) == 0)
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());

        return W32APITypeMapper.DEFAULT == W32APITypeMapper.UNICODE ?
                szProcessName.getWideString(0) :
                szProcessName.getString(0);
    }

    private String getProcessFileName(HANDLE hProcess, HMODULE hMod) {
        final var szProcessName = new Memory((long) (Kernel32.MAX_PATH + 1) * Native.WCHAR_SIZE);
        if (Psapi.INSTANCE.GetModuleFileNameEx(hProcess, hMod, szProcessName, (int) (szProcessName.size() / Native.WCHAR_SIZE)) == 0)
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());

        return W32APITypeMapper.DEFAULT == W32APITypeMapper.UNICODE ?
                szProcessName.getWideString(0) :
                szProcessName.getString(0);
    }
}
