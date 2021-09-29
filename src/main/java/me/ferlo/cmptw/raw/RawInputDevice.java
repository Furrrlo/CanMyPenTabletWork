package me.ferlo.cmptw.raw;

public record RawInputDevice(String hwid, String name, int type, User32.RAWINPUTDEVICE handle) {
}
