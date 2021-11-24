package me.ferlo.cmptw.process;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HICON;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.win32.W32APIOptions;

import static com.sun.jna.platform.win32.WinDef.MAX_PATH;

@SuppressWarnings({ "unused", "SpellCheckingInspection" })
interface Shell32 extends com.sun.jna.platform.win32.Shell32 {

    Shell32 INSTANCE = Native.load("shell32", Shell32.class, W32APIOptions.DEFAULT_OPTIONS);

    /**
     * Indicates that the function should not attempt to access the file specified by pszPath.
     * Rather, it should act as if the file specified by pszPath exists with the file attributes passed in
     * dwFileAttributes.
     * This flag cannot be combined with the SHGFI_ATTRIBUTES, SHGFI_EXETYPE, or SHGFI_PIDL flags.
     */
    int SHGFI_USEFILEATTRIBUTES = 0x000000010;
    /**
     * Retrieve the name of the file that contains the icon representing the file specified by pszPath,
     * as returned by the IExtractIcon::GetIconLocation method of the file's icon handler.
     * Also retrieve the icon index within that file.
     * The name of the file containing the icon is copied to the szDisplayName member of the structure specified by psfi.
     * The icon's index is copied to that structure's iIcon member.
     */
    int SHGFI_ICONLOCATION = 0x000001000;

    /**
     * The szPath and iIcon members of the SHSTOCKICONINFO structure receive the path and icon index of the requested
     * icon, in a format suitable for passing to the ExtractIcon function.
     * The numerical value of this flag is zero, so you always get the icon location regardless of other flags.
     */
    int SHGSI_ICONLOCATION = 0;

    /**
     * Used by SHGetStockIconInfo to identify which stock system icon to retrieve.
     * <p>
     * See https://docs.microsoft.com/en-us/windows/win32/api/shellapi/ne-shellapi-shstockiconid
     */
    interface SHSTOCKICONID {
        int SIID_DOCNOASSOC = 0;
        int SIID_DOCASSOC = 1;
        int SIID_APPLICATION = 2;
        int SIID_FOLDER = 3;
        int SIID_FOLDEROPEN = 4;
        int SIID_DRIVE525 = 5;
        int SIID_DRIVE35 = 6;
        int SIID_DRIVEREMOVE = 7;
        int SIID_DRIVEFIXED = 8;
        int SIID_DRIVENET = 9;
        int SIID_DRIVENETDISABLED = 10;
        int SIID_DRIVECD = 11;
        int SIID_DRIVERAM = 12;
        int SIID_WORLD = 13;
        int SIID_SERVER = 15;
        int SIID_PRINTER = 16;
        int SIID_MYNETWORK = 17;
        int SIID_FIND = 22;
        int SIID_HELP = 23;
        int SIID_SHARE = 28;
        int SIID_LINK = 29;
        int SIID_SLOWFILE = 30;
        int SIID_RECYCLER = 31;
        int SIID_RECYCLERFULL = 32;
        int SIID_MEDIACDAUDIO = 40;
        int SIID_LOCK = 47;
        int SIID_AUTOLIST = 49;
        int SIID_PRINTERNET = 50;
        int SIID_SERVERSHARE = 51;
        int SIID_PRINTERFAX = 52;
        int SIID_PRINTERFAXNET = 53;
        int SIID_PRINTERFILE = 54;
        int SIID_STACK = 55;
        int SIID_MEDIASVCD = 56;
        int SIID_STUFFEDFOLDER = 57;
        int SIID_DRIVEUNKNOWN = 58;
        int SIID_DRIVEDVD = 59;
        int SIID_MEDIADVD = 60;
        int SIID_MEDIADVDRAM = 61;
        int SIID_MEDIADVDRW = 62;
        int SIID_MEDIADVDR = 63;
        int SIID_MEDIADVDROM = 64;
        int SIID_MEDIACDAUDIOPLUS = 65;
        int SIID_MEDIACDRW = 66;
        int SIID_MEDIACDR = 67;
        int SIID_MEDIACDBURN = 68;
        int SIID_MEDIABLANKCD = 69;
        int SIID_MEDIACDROM = 70;
        int SIID_AUDIOFILES = 71;
        int SIID_IMAGEFILES = 72;
        int SIID_VIDEOFILES = 73;
        int SIID_MIXEDFILES = 74;
        int SIID_FOLDERBACK = 75;
        int SIID_FOLDERFRONT = 76;
        int SIID_SHIELD = 77;
        int SIID_WARNING = 78;
        int SIID_INFO = 79;
        int SIID_ERROR = 80;
        int SIID_KEY = 81;
        int SIID_SOFTWARE = 82;
        int SIID_RENAME = 83;
        int SIID_DELETE = 84;
        int SIID_MEDIAAUDIODVD = 85;
        int SIID_MEDIAMOVIEDVD = 86;
        int SIID_MEDIAENHANCEDCD = 87;
        int SIID_MEDIAENHANCEDDVD = 88;
        int SIID_MEDIAHDDVD = 89;
        int SIID_MEDIABLURAY = 90;
        int SIID_MEDIAVCD = 91;
        int SIID_MEDIADVDPLUSR = 92;
        int SIID_MEDIADVDPLUSRW = 93;
        int SIID_DESKTOPPC = 94;
        int SIID_MOBILEPC = 95;
        int SIID_USERS = 96;
        int SIID_MEDIASMARTMEDIA = 97;
        int SIID_MEDIACOMPACTFLASH = 98;
        int SIID_DEVICECELLPHONE = 99;
        int SIID_DEVICECAMERA = 100;
        int SIID_DEVICEVIDEOCAMERA = 101;
        int SIID_DEVICEAUDIOPLAYER = 102;
        int SIID_NETWORKCONNECT = 103;
        int SIID_INTERNET = 104;
        int SIID_ZIPFILE = 105;
        int SIID_SETTINGS = 106;
        int SIID_DRIVEHDDVD = 132;
        int SIID_DRIVEBD = 133;
        int SIID_MEDIAHDDVDROM = 134;
        int SIID_MEDIAHDDVDR = 135;
        int SIID_MEDIAHDDVDRAM = 136;
        int SIID_MEDIABDROM = 137;
        int SIID_MEDIABDR = 138;
        int SIID_MEDIABDRE = 139;
        int SIID_CLUSTEREDDRIVE = 140;
        int SIID_MAX_ICONS = 175;
    }

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

    /**
     * Retrieves information about an object in the file system, such as a file, folder, directory, or drive root.
     *
     * See <a>https://docs.microsoft.com/en-us/windows/win32/api/shellapi/nf-shellapi-shgetfileinfoa</a>
     *
     * @param pszPath          A pointer to a null-terminated string of maximum length MAX_PATH that contains the path and
     *                         file name. Both absolute and relative paths are valid.
     *                         <p>
     *                         If the uFlags parameter includes the SHGFI_PIDL flag, this parameter must be the address
     *                         of an ITEMIDLIST (PIDL) structure that contains the list of item identifiers that
     *                         uniquely identifies the file within the Shell's namespace. The PIDL must be a fully
     *                         qualified PIDL. Relative PIDLs are not allowed.
     *                         <p>
     *                         If the uFlags parameter includes the SHGFI_USEFILEATTRIBUTES flag, this parameter does
     *                         not have to be a valid file name.
     *                         The function will proceed as if the file exists with the specified name and with the
     *                         file attributes passed in the dwFileAttributes parameter.
     *                         This allows you to obtain information about a file type by passing just the extension for
     *                         pszPath and passing FILE_ATTRIBUTE_NORMAL in dwFileAttributes.
     *                         <p>
     *                         This string can use either short (the 8.3 form) or long file names.
     * @param dwFileAttributes A combination of one or more file attribute flags (FILE_ATTRIBUTE_ values as defined
     *                         in {@link com.sun.jna.platform.win32.WinNT}).
     *                         If uFlags does not include the SHGFI_USEFILEATTRIBUTES flag, this parameter is ignored.
     * @param psfi             Pointer to a SHFILEINFO structure to receive the file information.
     * @param cbFileInfo       The size, in bytes, of the SHFILEINFO structure pointed to by the psfi parameter.
     * @param uFlags           The flags that specify the file information to retrieve (see SHGFI_ values).
     * @return Returns a value whose meaning depends on the uFlags parameter.
     * <p>
     * If uFlags does not contain SHGFI_EXETYPE or SHGFI_SYSICONINDEX, the return value is nonzero if successful, or zero otherwise.
     * <p>
     * If uFlags contains the SHGFI_EXETYPE flag, the return value specifies the type of the executable file.
     * It will be one of the following values.
     * - 0: Nonexecutable file or an error condition.
     * - LOWORD = NE or PE and HIWORD = Windows version: Windows application.
     * - LOWORD = MZ and HIWORD = 0: MS-DOS .exe or .com file
     * - LOWORD = PE and HIWORD = 0: Console application or .bat file
     */
    int SHGetFileInfo(String pszPath, int dwFileAttributes, SHFILEINFO psfi, int cbFileInfo, int uFlags);

    @Structure.FieldOrder({ "hIcon", "iIcon", "dwAttributes", "szDisplayName", "szTypeName" })
    class SHFILEINFO extends Structure implements Structure.ByReference {
        public HICON hIcon;
        public int iIcon;
        public DWORD dwAttributes;
        public char[] szDisplayName = new char[MAX_PATH];
        public char[] szTypeName = new char[80];
    }

    /**
     * Retrieves information about system-defined Shell icons.
     *
     * See <a>https://docs.microsoft.com/en-us/windows/win32/api/shellapi/nf-shellapi-shgetstockiconinfo</a>
     *
     * @param siid One of the values from the {@link SHSTOCKICONID} enumeration that specifies which icon should be retrieved.
     * @param uFlags A combination of zero or more of the flags that specify which information is requested (SHGSI_ values).
     * @return If this function succeeds, it returns S_OK. Otherwise, it returns an HRESULT error code.
     */
    HRESULT SHGetStockIconInfo(int siid, int uFlags, SHSTOCKICONINFO psii);

    @Structure.FieldOrder({ "cbSize", "hIcon", "iSysImageIndex", "iIcon", "szDisplayName" })
    class SHSTOCKICONINFO extends Structure implements Structure.ByReference {
        public DWORD cbSize;
        public HICON hIcon;
        public int iSysImageIndex;
        public int iIcon;
        public char[] szDisplayName = new char[MAX_PATH];
    }
}
