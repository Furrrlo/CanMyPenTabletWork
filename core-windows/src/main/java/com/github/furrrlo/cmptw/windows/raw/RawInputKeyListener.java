package com.github.furrrlo.cmptw.windows.raw;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public interface RawInputKeyListener {

    void onRawKeyEvent(RawKeyEvent rawEvent, Supplier<List<RawKeyEvent>> peek);

    default void onDevicesChange(Collection<RawInputDevice> currentDevices,
                                 Collection<RawInputDevice> added,
                                 Collection<RawInputDevice> removed) {
    }
}
