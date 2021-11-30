package com.github.furrrlo.cmptw.hook;

public interface KeyboardHookServiceProvider {

    boolean isSupported();

    KeyboardHookService create();
}
