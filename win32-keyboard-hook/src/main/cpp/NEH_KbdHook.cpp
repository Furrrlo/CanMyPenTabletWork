/*
MIT License

Copyright (c) 2021 VollRahm

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

// This is just a higher level keyboard hook that needs to be in a different Assembly
//

#include "pch.h"
#include "framework.h"
#include "NEH_KbdHook.h"

#pragma data_seg (".SHARED")
HWND callback_reciever = NULL;
HHOOK kbHook = NULL;
#pragma data_seg ()
#pragma comment (linker, "/section:.SHARED,RWS")

BOOL alreadyHooked = false;

static LRESULT CALLBACK HookCallback(int nCode, WPARAM wParam, LPARAM lParam)
{
	if (nCode == HC_ACTION || nCode == HC_NOREMOVE)
	{
		DWORD_PTR blockKey;
		// TODO: make timeout configurable
		LRESULT success = SendMessageTimeout(callback_reciever, WM_HOOK + nCode, wParam, lParam, SMTO_ABORTIFHUNG | SMTO_ERRORONEXIT, 100, &blockKey);
		if(success)
		{
			return blockKey ? 1 : CallNextHookEx(kbHook, nCode, wParam, lParam);
		}

		if(GetLastError() == ERROR_INVALID_WINDOW_HANDLE)
		{
		    StopHook();
		    return CallNextHookEx(kbHook, nCode, wParam, lParam);
		}

        return CallNextHookEx(kbHook, nCode, wParam, lParam);
	}
	return CallNextHookEx(kbHook, nCode, wParam, lParam);
}


NEHKBDHOOK_API BOOL StartHook(HWND _callback_reciever)
{
	if (alreadyHooked)
	{
		return FALSE;
	}

	if (hookInstance == NULL)
	{
		MessageBox(NULL, L"Failed to inject hook (DLL module is null)", L"Error", MB_OK);
		return FALSE;
	}

	kbHook = SetWindowsHookEx(WH_KEYBOARD, (HOOKPROC)HookCallback, hookInstance, 0);
	if (kbHook == NULL)
	{
	    DWORD err = GetLastError();

	    wchar_t msg[MAX_PATH + 40] = L"Failed to inject hook using DLL module: ";
        if(!GetModuleFileName(hookInstance, msg + 40, MAX_PATH))
        {
		    MessageBox(NULL, L"Failed to inject hook", L"Error", MB_OK);
		    SetLastError(err);
		    return FALSE;
        }

		MessageBox(NULL, msg, L"Error", MB_OK);
		SetLastError(err);
		return FALSE;
	}

	callback_reciever = _callback_reciever;
	return TRUE;
}

NEHKBDHOOK_API BOOL StopHook()
{
	if (kbHook == NULL) return TRUE;

	BOOL success = UnhookWindowsHookEx(kbHook);

	if (!success)
	{
		return FALSE;
	}

	callback_reciever = FALSE;
	kbHook = NULL;
	return TRUE;
}
