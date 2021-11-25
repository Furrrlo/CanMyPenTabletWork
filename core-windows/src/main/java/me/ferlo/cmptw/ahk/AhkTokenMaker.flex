package me.ferlo.cmptw.ahk;

import java.io.*;
import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.*;

%%

%public
%class AhkTokenMaker
%extends AbstractJFlexCTokenMaker
%unicode
%type org.fife.ui.rsyntaxtextarea.Token

%{

   /**
    * Constructor.  This must be here because JFlex does not generate a
    * no-parameter constructor.
    */
   public AhkTokenMaker() {
   }

   /**
    * Adds the token specified to the current linked list of tokens.
    *
    * @param tokenType The token's type.
    * @see #addToken(int, int, int)
    */
   private void addHyperlinkToken(int start, int end, int tokenType) {
      int so = start + offsetShift;
      addToken(zzBuffer, start,end, tokenType, so, true);
   }

   /**
    * Adds the token specified to the current linked list of tokens.
    *
    * @param tokenType The token's type.
    */
   private void addToken(int tokenType) {
      addToken(zzStartRead, zzMarkedPos-1, tokenType);
   }

   /**
    * Adds the token specified to the current linked list of tokens.
    *
    * @param tokenType The token's type.
    * @see #addHyperlinkToken(int, int, int)
    */
   private void addToken(int start, int end, int tokenType) {
      int so = start + offsetShift;
      addToken(zzBuffer, start,end, tokenType, so, false);
   }

   /**
    * Adds the token specified to the current linked list of tokens.
    *
    * @param array The character array.
    * @param start The starting offset in the array.
    * @param end The ending offset in the array.
    * @param tokenType The token's type.
    * @param startOffset The offset in the document at which this token
    *        occurs.
    * @param hyperlink Whether this token is a hyperlink.
    */
   public void addToken(char[] array, int start, int end, int tokenType,
                  int startOffset, boolean hyperlink) {
      super.addToken(array, start,end, tokenType, startOffset, hyperlink);
      zzStartRead = zzMarkedPos;
   }

   /**
    * Returns the text to place at the beginning and end of a
    * line to "comment" it in a this programming language.
    *
    * @return The start and end strings to add to a line to "comment"
    *         it out.
    */
   public String[] getLineCommentStartAndEnd() {
      return new String[] { "//", null };
   }

   /**
    * Returns the first token in the linked list of tokens generated
    * from <code>text</code>.  This method must be implemented by
    * subclasses so they can correctly implement syntax highlighting.
    *
    * @param text The text from which to get tokens.
    * @param initialTokenType The token type we should start with.
    * @param startOffset The offset into the document at which
    *        <code>text</code> starts.
    * @return The first <code>Token</code> in a linked list representing
    *         the syntax highlighted text.
    */
   public Token getTokenList(Segment text, int initialTokenType, int startOffset) {

      resetTokenList();
      this.offsetShift = -text.offset + startOffset;

      // Start off in the proper state.
      int state = Token.NULL;
      switch (initialTokenType) {
          case Token.COMMENT_MULTILINE:
            state = MLC;
            start = text.offset;
            break;

          case Token.COMMENT_DOCUMENTATION:
            state = MLCD;
            start = text.offset;
            break;

         /* No documentation comments */
         default:
            state = Token.NULL;
      }

      s = text;
      try {
         yyreset(zzReader);
         yybegin(state);
         return yylex();
      } catch (IOException ioe) {
         ioe.printStackTrace();
         return new TokenImpl();
      }

   }

   /**
    * Refills the input buffer.
    *
    * @return      <code>true</code> if EOF was reached, otherwise
    *              <code>false</code>.
    */
   private boolean /* keep */ zzRefill() {
      return zzCurrentPos>=s.offset+s.count;
   }

   /**
    * Resets the scanner to read from a new input stream.
    * Does not close the old reader.
    *
    * All internal variables are reset, the old input stream
    * <b>cannot</b> be reused (internal buffer is discarded and lost).
    * Lexical state is set to <tt>YY_INITIAL</tt>.
    *
    * @param reader   the new input stream
    */
   public final void /* keep */ yyreset(Reader reader) {
      // 's' has been updated.
      zzBuffer = s.array;
      /*
       * We replaced the line below with the two below it because zzRefill
       * no longer "refills" the buffer (since the way we do it, it's always
       * "full" the first time through, since it points to the segment's
       * array).  So, we assign zzEndRead here.
       */
      //zzStartRead = zzEndRead = s.offset;
      zzStartRead = s.offset;
      zzEndRead = zzStartRead + s.count - 1;
      zzCurrentPos = zzMarkedPos = zzPushbackPos = s.offset;
      zzLexicalState = YYINITIAL;
      zzReader = reader;
      zzAtBOL  = true;
      zzAtEOF  = false;
   }

%}

Letter                     = [A-Za-z]
Digit                     = ([0-9])
AnyCharacterButDoubleQuoteOrBackSlash   = ([^\\\"\n])
NonSeparator                  = ([^\t\f\r\n\ \(\)\{\}\[\]\;\,\.\=\>\<\!\~\?\:\+\-\*\/\&\|\^\%\"\']|"#"|"\\")
IdentifierStart               = ({Letter}|"_"|"#"|"@"|"$")
IdentifierPart                  = ({IdentifierStart}|{Digit})
WhiteSpace            = ([ \t\f]+)

BooleanLiteral				= ("true"|"false")

StringLiteral            = ([\"]({AnyCharacterButDoubleQuoteOrBackSlash}|"")*[\"])
UnclosedStringLiteral      = ([\"]([\\].|[^\\\"])*[^\"]?)
ErrorStringLiteral         = ({UnclosedStringLiteral}[\"])

MLCBegin               = "/*"
MLCEnd                 = "*/"
LineCommentBegin       = ";"

IntegerLiteral         = ({Digit}+)
ErrorNumberFormat         = (({IntegerLiteral}){NonSeparator}+)

Separator               = ([\(\)\{\}\[\]])
Separator2            = ([\;,.])

Identifier            = ({IdentifierStart}{IdentifierPart}*)
ErrorIdentifier			= ({NonSeparator}+)

KeyVariable = (\{(((.|
                  F[1-9]|F1{Digit}|F2[0-4]|
                  \!|\#|\+|\^|\{|\}|
                  "Enter"|"Escape"|"Esc"|"Space"|Tab|Backspace|BS|Delete|Del|Insert|Ins|Up|Down|Left|Right|Home|End|PgUp|PgDn|
                  CapsLock|ScrollLock|NumLock|Control|Ctrl|LControl|LCtrl|RControl|RCtrl|Alt|LAlt|RAlt|Shift|LShift|RShift|
                  LWin|RWin|AppsKey|Sleep|
                  ASC {Digit}{5}|U\+{Digit}{5}|vk{Digit}{2}|sc{Digit}{3}|vk{Digit}{2}sc{Digit}{3}|
                  Numpad{Digit}|NumpadDot|NumpadEnter|NumpadMult|NumpadDiv|NumpadAdd|NumpadSub|NumpadDel|NumpadIns|NumpadClear|
                  NumpadUp|NumpadDown|NumpadLeft|NumpadRight|NumpadHome|NumpadEnd|NumpadPgUp|NumpadPgDn|
                  Browser_Back|Browser_Forward|Browser_Refresh|Browser_Stop|Browser_Search|Browser_Favorites|Browser_Home|
                  Volume_Mute|Volume_Down|Volume_Up|
                  Media_Next|Media_Prev|Media_Stop|Media_Play_Pause|
                  Launch_Mail|Launch_Media|Launch_App1|Launch_App2|PrintScreen|CtrlBreak|Pause)({WhiteSpace}+(up|down|{Digit}+))?)
                  |
                  (Click(({WhiteSpace}+{Digit}+){3})?)
                  |
                  (WheelDown|WheelUp|WheelLeft|WheelRight|LButton|RButton|MButton|XButton1|XButton2)
                  |
                  (Blind|Raw|Text))\})

Directive            = (\#{Identifier})
CompilerDirectiveStart          = "/*@Ahk2Exe-"
CompilerDirectiveEnd            = "*/"

%state MLC
%state MLCD
%caseless

%%

<YYINITIAL> {

   /* Keywords */
   "if" |
   "ifequal" | "ifnotequal" |
   "ifexist" | "ifnotexist" |
   "ifgreater" | "ifgreaterorequal" |
   "ifless" | "iflessorequal" |
   "ifinstring" | "ifnotinstring" |
   "ifmsgbox"  |
   "ifwinactive" | "ifwinexist" | "ifwinnotactive" | "ifwinnotexist" |
   "else" |
   "while" | "loop" | "for" | "until" |
   "break" | "continue" |
   "gosub" | "goto" |
   "return" |
   "try" | "catch" | "throw" | "finally" |
   "static" | "global" | "local" | "byref" | "class" |
   "exit" | "exitapp" { addToken(Token.RESERVED_WORD); }

   /* Variables. */
   {KeyVariable} { addToken(Token.VARIABLE); }

   /* Variables. */
   "A_Space" | "A_Tab" |
   "A_Args" | "A_WorkingDir" | "A_ScriptDir" | "A_ScriptName" | "A_ScriptFullPath" |
   "A_ScriptHwnd" | "A_LineNumber" | "A_LineFile" | "A_ThisFunc" | "A_ThisLabel" |
   "A_AhkVersion" | "A_AhkPath" | "A_IsUnicode" | "A_IsCompiled" | "A_ExitReason" |
   "A_YYYY" | "A_Year" | "A_MM" | "A_Mon" | "A_DD" | "A_MDay" | "A_MMMM" | "A_MMM" | "A_DDDD" | "A_DDD" |
   "A_WDay" | "A_YDay" | "A_YWeek" | "A_Hour" | "A_Min" | "A_Sec" | "A_MSec" | "A_Now" | "A_NowUTC" | "A_TickCount" |
   "A_IsSuspended" | "A_IsPaused" | "A_IsCritical" | "A_BatchLines" | "A_ListLines" | "A_TitleMatchMode" |
   "A_TitleMatchModeSpeed" | "A_DetectHiddenWindows" | "A_DetectHiddenText" | "A_AutoTrim" | "A_StringCaseSense" |
   "A_FileEncoding" | "A_FormatInteger" | "A_FormatFloat" | "A_SendMode" | "A_SendLevel" |
   "A_StoreCapsLockMode" | "A_KeyDelay" | "A_KeyDuration" | "A_KeyDelayPlay" | "A_KeyDurationPlay" |
   "A_WinDelay" | "A_ControlDelay" | "A_MouseDelay" | "A_MouseDelayPlay" | "A_DefaultMouseSpeed" |
   "A_CoordModeToolTip" | "A_CoordModePixel" | "A_CoordModeMouse" | "A_CoordModeCaret" |"A_CoordModeMenu" |
   "A_RegView" | "A_IconHidden" | "A_IconTip" | "A_IconFile" | "A_IconNumber" |
   "A_TimeIdle" | "A_TimeIdlePhysical" | "A_TimeIdleKeyboard" | "A_TimeIdleMouse" |
   "A_DefaultGui" | "A_DefaultListView" | "A_DefaultTreeView" | "A_Gui" | "A_GuiControl" |
   "A_GuiWidth" | "A_GuiHeight" | "A_GuiX" | "A_GuiY" | "A_GuiEvent" | "A_GuiControlEvent" | "A_EventInfo" |
   "A_ThisMenuItem" | "A_ThisMenu" | "A_ThisMenuItemPos" | "A_ThisHotkey" | "A_PriorHotkey" |
   "A_PriorKey" | "A_TimeSinceThisHotkey" | "A_TimeSincePriorHotkey" | "A_EndChar" |
   "ComSpec" | "A_ComSpec" | "A_Temp" | "A_OSType" | "A_OSVersion" | "A_Is64bitOS" | "A_PtrSize" |
   "A_Language" | "A_ComputerName" | "A_UserName" | "A_WinDir" | "A_ProgramFiles" | "ProgramFiles" |
   "A_AppData" | "A_AppDataCommon" | "A_Desktop" | "A_DesktopCommon" | "A_StartMenu" | "A_StartMenuCommon" |
   "A_Programs" | "A_ProgramsCommon" | "A_Startup" | "A_StartupCommon" | "A_MyDocuments" | "A_IsAdmin" |
   "A_ScreenWidth" | "A_ScreenHeight" | "A_ScreenDPI" |
   "A_IPAddress1" | "A_IPAddress2" | "A_IPAddress3" | "A_IPAddress4" |
   "A_Cursor" | "A_CaretX" | "A_CaretY" | "Clipboard" | "ClipboardAll" |
   "ErrorLevel" | "A_LastError" { addToken(Token.VARIABLE); }

   /* Commands */
   "AutoTrim" | "BlockInput" | "Click" | "ClipWait" |
   "Control" | "ControlClick" | "ControlFocus" |
   "ControlGet" | "ControlGetFocus" | "ControlGetPos" | "ControlGetText" |
   "ControlMove" | "ControlSend" | "ControlSendRaw" | "ControlSetText" |
   "CoordMode" | "Critical" | "DetectHiddenText" | "DetectHiddenWindows" |
   "Drive" | "DriveGet" | "DriveSpaceFree" |
   "Edit" | "EnvAdd" | "EnvDiv" | "EnvGet" | "EnvMult" | "EnvSet" | "EnvSub" | "EnvUpdate" |
   "FileAppend" | "FileCopy" | "FileCopyDir" | "FileCreateDir" | "FileCreateShortcut" | "FileDelete" |
   "FileEncoding" | "FileInstall" | "FileGetAttrib" | "FileGetShortcut" | "FileGetSize" | "FileGetTime" |
   "FileGetVersion" | "FileMove" | "FileMoveDir" |
   "FileRead" | "FileReadLine" | "FileRecycle" | "FileRecycleEmpty" | "FileRemoveDir" |
   "FileSelectFile" | "FileSelectFolder" | "FileSetAttrib" | "FileSetTime" |
   "FormatTime" |
   "GroupActivate" | "GroupAdd" | "GroupClose" | "GroupDeactivate" |
   "Gui" | "GuiControl" | "GuiControlGet" | "Hotkey" |
   "ImageSearch" |
   "IniDelete" | "IniRead" | "IniWrite" |
   "Input" | "InputBox" |
   "KeyHistory" | "KeyWait" |
   "ListHotkeys" | "ListLines" | "ListVars" | "Menu" |
   "MouseClick" | "MouseClickDrag" | "MouseGetPos" | "MouseMove" |
   "MsgBox" | "OutputDebug" | "Pause" |
   "PixelGetColor" | "PixelSearch" |
   "PostMessage" | "Process" | "Progress" | "Random" |
   "RegDelete" | "RegRead" | "RegWrite" |
   "Reload" |
   "Run" | "RunAs" | "RunWait" |
   "Send" | "SendRaw" | "SendInput" | "SendPlay" | "SendEvent" |
   "SendLevel" | "SendMessage" | "SendMode" |
   "SetBatchLines" | "SetCapsLockState" | "SetControlDelay" | "SetDefaultMouseSpeed" |
   "SetEnv" | "SetFormat" | "SetKeyDelay" | "SetMouseDelay" | "SetNumLockState" | "SetScrollLockState" |
   "SetRegView" | "SetStoreCapsLockMode" | "SetTimer" | "SetTitleMatchMode" | "SetWinDelay" |
   "SetWorkingDir" | "Shutdown" | "Sleep" | "Sort" |
   "SoundBeep" | "SoundGet" | "SoundGetWaveVolume" | "SoundPlay" | "SoundSet" | "SoundSetWaveVolume" |
   "SplashImage" | "SplashTextOn" | "SplashTextOff" |
   "SplitPath" | "StatusBarGetText" | "StatusBarWait" |
   "StringCaseSense" | "StringGetPos" | "StringLeft" | "StringLen" | "StringLower" |
   "StringMid" | "StringReplace" | "StringRight" | "StringSplit" | "StringTrimLeft" | "StringTrimRight" | "StringUpper" |
   "Suspend" | "SysGet" | "Thread" | "ToolTip" | "Transform" | "TrayTip" |
   "UrlDownloadToFile" | "WinActivate" | "WinActivateBottom" | "WinClose" | "WinGetActiveStats" |
   "WinGetActiveTitle" | "WinGetClass" | "WinGet" | "WinGetPos" | "WinGetText" |
   "WinGetTitle" | "WinHide" | "WinKill" | "WinMaximize" | "WinMenuSelectItem" | "WinMinimize" |
   "WinMinimizeAll" | "WinMinimizeAllUndo" | "WinMove" | "WinRestore" | "WinSet" | "WinSetTitle" |
   "WinShow" | "WinWait" | "WinWaitActive" | "WinWaitNotActive" |
   "WinWaitClose" { addToken(Token.FUNCTION); }

   /* Functions */
   "abs" | "acos" | "asc" | "asin" | "atan" | "ceil" | "chr" | "cos" | "dllcall" | "exp" | "fileexist" | "floor" | "getkeystate" | "numget" | "numput" | "registercallback" |
   "il_add" | "il_create" | "il_destroy" | "instr" | "islabel" | "isfunc" | "ln" | "log" | "lv_add" | "lv_delete" | "lv_deletecol" |
   "lv_getcount" | "lv_getnext" | "lv_gettext" | "lv_insert" | "lv_insertcol" | "lv_modify" |
   "lv_modifycol" | "lv_setimagelist" | "mod" | "onmessage" | "round" |
   "regexmatch" | "regexreplace" |
   "sb_seticon" | "sb_setparts" | "sb_settext" | "sin" | "sqrt" | "strlen" | "substr" | "tan" |
   "tv_add" | "tv_delete" | "tv_getchild" | "tv_getcount" | "tv_getnext" | "tv_get" | "tv_getparent" |
   "tv_getprev" | "tv_getselection" | "tv_gettext" | "tv_modify" | "tv_setimagelist" |
   "varsetcapacity" | "winactive" | "winexist" |
   "trim" | "ltrim" | "rtrim" | "fileopen" | "strget" | "strput" |
   "object" | "array" | "isobject" | "objinsert" | "objremove" | "objminindex" | "objmaxindex" | "objsetcapacity" | "objgetcapacity" |
   "objgetaddress" | "objnewenum" | "objaddref" | "objrelease" | "objhaskey" | "objclone" | "objbindmethod" |
   "_newenum" | "comobjcreate" | "comobjget" | "comobjconnect" | "comobjerror" |
   "comobjactive" | "comobject" | "comobjenwrap" | "comobjunwrap" | "comobjmissing" | "comobjtype" | "comobjvalue" | "comobjarray" |
   "comobjquery" | "comobjflags" | "func" |
   "getkeyname" | "getkeyvk" | "getkeysc" | "isbyref" | "exception" | "strsplit" | "format" |
   "onexit" | "onclipboardchange" |
   "objinsertat" | "objpush" | "objrawset" | "objdelete" | "objremoveat" | "objpop" | "objlength" |
   "ord" | "strreplace"      { addToken(Token.FUNCTION); }

   /* Directives */
   {Directive} 			{ addToken(Token.PREPROCESSOR); }

   /* Booleans. */
   {BooleanLiteral}			{ addToken(Token.LITERAL_BOOLEAN); }

   {Identifier}            { addToken(Token.IDENTIFIER); }

   {WhiteSpace}            { addToken(Token.WHITESPACE); }

   /* String/Character literals. */
   {StringLiteral}            { addToken(Token.LITERAL_STRING_DOUBLE_QUOTE); }
   {UnclosedStringLiteral}      { addToken(Token.ERROR_STRING_DOUBLE); addNullToken(); return firstToken; }
   {ErrorStringLiteral}      { addToken(Token.ERROR_STRING_DOUBLE); }

   /* Compiler directives */
   {CompilerDirectiveStart}     { start = zzMarkedPos-11; yybegin(MLCD); }

   /* Comment literals. */
   {MLCBegin}               { start = zzMarkedPos-2; yybegin(MLC); }
   {LineCommentBegin}.*      { addToken(Token.COMMENT_EOL); addNullToken(); return firstToken; }

   /* Separators. */
   {Separator}               { addToken(Token.SEPARATOR); }
   {Separator2}            { addToken(Token.IDENTIFIER); }

   /* Operators. */
   "!" | "%" | "%=" | "&" | "&&" | "*" | "*=" | "**" | "+" | "++" | "+=" | "," | "-" | "--" | "-=" |
   "/" | "/=" | ":" | "<" | "<<" | "<<=" | "=" | ":=" | "==" | ">" | ">>" | ">>=" | "?" | "^" | "|" |
   "||" | "~" | "~="      { addToken(Token.OPERATOR); }

   /* Numbers */
   {IntegerLiteral}         { addToken(Token.LITERAL_NUMBER_DECIMAL_INT); }
   {ErrorNumberFormat}         { addToken(Token.ERROR_NUMBER_FORMAT); }

   {ErrorIdentifier}				{ addToken(Token.ERROR_IDENTIFIER); }

   /* Ended with a line not in a string or comment. */
   \n |
   <<EOF>>                  { addNullToken(); return firstToken; }

   /* Catch any other (unhandled) characters. */
   .                     { addToken(Token.IDENTIFIER); }

}

<MLC> {
   [^\n*]+            {}
   {MLCEnd}         { yybegin(YYINITIAL); addToken(start,zzStartRead+2-1, Token.COMMENT_MULTILINE); }
   "*"               {}
   \n |
   <<EOF>>            { addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE); return firstToken; }
}

<MLCD> {
   [^\n*]+            {}
   {CompilerDirectiveEnd}         { yybegin(YYINITIAL); addToken(start,zzStartRead+2-1, Token.COMMENT_DOCUMENTATION); }
   "*"               {}
   \n |
   <<EOF>>            { addToken(start,zzStartRead-1, Token.COMMENT_DOCUMENTATION); return firstToken; }
}
