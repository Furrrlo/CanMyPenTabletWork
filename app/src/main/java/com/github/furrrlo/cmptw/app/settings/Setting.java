package com.github.furrrlo.cmptw.app.settings;

public interface Setting<T> {

    T get();

    void set(T value);

    void addListener(T value);

    void removeListener(T value);
}
