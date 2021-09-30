package me.ferlo.cmptw.hook;

public interface KeyboardHookListener {

    default void keyPressed(KeyboardHookEvent event) {}
}
