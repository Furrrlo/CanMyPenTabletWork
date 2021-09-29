package me.ferlo.cmptw.hook;

public interface KeyboardHookListener {

    default void keyPressed(KeyboardHookEvent event) {}

    default void keyReleased(KeyboardHookEvent event) {}
}
