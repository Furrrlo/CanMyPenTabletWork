package com.github.furrrlo.cmptw.windows.hook;

import com.google.auto.service.AutoService;
import com.github.furrrlo.cmptw.hook.KeyboardHookService;
import com.github.furrrlo.cmptw.hook.KeyboardHookServiceProvider;

@AutoService(KeyboardHookServiceProvider.class)
public class WinKeyboardHookServiceProvider implements KeyboardHookServiceProvider {

    @Override
    public boolean isSupported() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    @Override
    public KeyboardHookService create() {
        return new WinKeyboardHookService();
    }
}
