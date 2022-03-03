package com.github.furrrlo.cmptw.app.settings;

public interface SettingSerializer<T, R> {

    R serialize(T value);

    T deserialize(R value);
}
