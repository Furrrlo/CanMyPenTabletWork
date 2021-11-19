package me.ferlo.cmptw.hook;

public interface KeyboardHookListener {

    boolean onKeyHook(KeyboardHookService service, KeyboardHookListener listener, KeyboardHookEvent event);
}
