package me.ferlo.cmptw.process;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HICON;
import com.sun.jna.win32.W32APIOptions;

interface Shell32 extends com.sun.jna.platform.win32.Shell32 {

    Shell32 INSTANCE = Native.load("shell32", Shell32.class, W32APIOptions.DEFAULT_OPTIONS);

    /**
     * Provides a default handler to extract an icon from a file.
     *
     * See <a>https://docs.microsoft.com/en-us/windows/win32/api/shlobj_core/nf-shlobj_core-shdefextracticona</a>
     *
     * @param pszIconFile A pointer to a null-terminated buffer that contains the path and name of the file from which the icon is extracted.
     * @param iIndex      The location of the icon within the file named in pszIconFile.
     *                    If this is a positive number, it refers to the zero-based position of the icon in the file.
     *                    For instance, 0 refers to the 1st icon in the resource file and 2 refers to the 3rd.
     *                    If this is a negative number, it refers to the icon's resource ID.
     * @param uFlags      A flag that controls the icon extraction.
     *                    GIL_SIMULATEDOC: Overlays the extracted icon on the default document icon to create the final icon.
     *                    This icon can be used when no more appropriate icon can be found or retrieved.
     * @param phiconLarge A pointer to an HICON that, when this function returns successfully,
     *                    receives the handle of the large version of the icon specified in the
     *                    LOWORD of nIconSize. This value can be NULL.
     * @param phiconSmall A pointer to an HICON that, when this function returns successfully, receives the handle of
     *                   the small version of the icon specified in the HIWORD of nIconSize.
     * @param nIconSize   A value that contains the large icon size in its LOWORD and the small icon size in its HIWORD.
     *                   Size is measured in pixels. Pass 0 to specify default large and small sizes.
     * @return This function can return one of these values.
     * - {@link com.sun.jna.platform.win32.WinError#S_OK}: Success.
     * - {@link com.sun.jna.platform.win32.WinError#S_FALSE}: The requested icon is not present.
     * - {@link com.sun.jna.platform.win32.WinError#E_FAIL}: The file cannot be accessed, or is being accessed through a slow link.
     */
    int SHDefExtractIcon(String pszIconFile, int iIndex, int uFlags, HICON[] phiconLarge, HICON[] phiconSmall, int nIconSize);
}
