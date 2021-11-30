package com.github.furrrlo.cmptw.windows.process;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

public interface Psapi extends com.sun.jna.platform.win32.Psapi {

    Psapi INSTANCE = Native.load("psapi", Psapi.class, W32APIOptions.DEFAULT_OPTIONS);

    /** https://docs.microsoft.com/en-us/windows/win32/fileio/maximum-file-path-limitation?tabs=cmd */
    int MAX_PATH = 260;

    int LIST_MODULES_32BIT = 0x01;
    int LIST_MODULES_64BIT = 0x02;
    int LIST_MODULES_ALL = 0x03;
    int LIST_MODULES_DEFAULT = 0x0;

    /**
     * Retrieves a handle for each module in the specified process that meets the specified filter criteria.
     * <p>
     * See <a>https://docs.microsoft.com/en-us/windows/win32/api/psapi/nf-psapi-enumprocessmodulesex</a>
     *
     * @param hProcess     A handle to the process.
     * @param lphModule    An array that receives the list of module handles.
     * @param cb           The size of the lphModule array, in bytes.
     * @param lpcbNeeded   The number of bytes required to store all module handles in the lphModule array.
     * @param dwFilterFlag The filter criteria. This parameter can be one of the following values.
     *                     - LIST_MODULES_32BIT: List the 32-bit modules.
     *                     - LIST_MODULES_64BIT: List the 64-bit modules.
     *                     - LIST_MODULES_ALL: List all modules.
     *                     - LIST_MODULES_DEFAULT: Use the default behavior.
     * @return If the function succeeds, the return value is nonzero.
     * <p>
     * If the function fails, the return value is zero. To get extended error information, call GetLastError.
     */
    boolean EnumProcessModulesEx(HANDLE hProcess, HMODULE[] lphModule, int cb, IntByReference lpcbNeeded, int dwFilterFlag);

    /**
     * Retrieves the base name of the specified module.
     * <p>
     * See <a>https://docs.microsoft.com/en-us/windows/win32/api/psapi/nf-psapi-getmodulebasenamea</a>
     *
     * @param hProcess   A handle to the process that contains the module.
     *                   The handle must have the PROCESS_QUERY_INFORMATION and PROCESS_VM_READ access rights.
     *                   For more information, see Process Security and Access Rights.
     * @param hModule    A handle to the module.
     *                   If this parameter is NULL, this function returns the name of the file used to create the calling process.
     * @param lpBaseName A pointer to the buffer that receives the base name of the module.
     *                   If the base name is longer than maximum number of characters specified by the nSize parameter,
     *                   the base name is truncated.
     * @param nSize      The size of the lpBaseName buffer, in characters.
     * @return If the function succeeds, the return value specifies the length of the string copied to the buffer, in characters.
     * <p>
     * If the function fails, the return value is zero. To get extended error information, call GetLastError.
     */
    int GetModuleBaseName(HANDLE hProcess, HMODULE hModule, Pointer lpBaseName, int nSize);
}
