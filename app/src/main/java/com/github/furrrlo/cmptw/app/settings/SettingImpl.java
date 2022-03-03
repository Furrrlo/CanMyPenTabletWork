package com.github.furrrlo.cmptw.app.settings;

abstract class SettingImpl<T> implements Setting<T> {

    void serialize(SettingSerializer<T, ?> serializer) {
        serializer.serialize(get());
    }

    <R> void deserialize(SettingSerializer<T, R> serializer, R value) {
        set(serializer.deserialize(value));
    }

    // TODO:

    @Override
    public T get() {
        return null;
    }

    @Override
    public void set(T value) {

    }

    @Override
    public void addListener(T value) {

    }

    @Override
    public void removeListener(T value) {

    }
}
