package me.ferlo.cmptw.hook;

public interface KeyboardHookListener {

    ListenerResult onKeyHook(KeyboardHookService service, KeyboardHookListener listener, KeyboardHookEvent event);

    enum ListenerResult {
        /** Do not cancel the event and propagate it to the next listeners */
        CONTINUE,
        /** Cancel the event, but propagate it to the next listeners */
        CANCEL,
        /** Cancel the event and do not propagate it to the next listeners */
        DELETE
    }
}
