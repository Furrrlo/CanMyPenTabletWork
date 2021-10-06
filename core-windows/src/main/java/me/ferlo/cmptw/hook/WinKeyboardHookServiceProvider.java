package me.ferlo.cmptw.hook;

import com.google.auto.service.AutoService;

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
