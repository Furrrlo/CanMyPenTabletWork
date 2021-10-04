package me.ferlo.cmptw.raw;

import java.util.Collection;

public interface RawInputKeyListener {

    void onRawKeyEvent(RawKeyEvent rawEvent);

    default void onDevicesChange(Collection<RawInputDevice> currentDevices,
                                 Collection<RawInputDevice> added,
                                 Collection<RawInputDevice> removed) {
    }
}
