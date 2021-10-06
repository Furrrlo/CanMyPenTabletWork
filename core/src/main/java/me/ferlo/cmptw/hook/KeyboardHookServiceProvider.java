package me.ferlo.cmptw.hook;

public interface KeyboardHookServiceProvider {

    boolean isSupported();

    KeyboardHookService create();
}
