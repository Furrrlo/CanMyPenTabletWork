package me.ferlo.cmptw.gui;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ListenableValue<T> {

    private T value;
    private final Collection<BiConsumer<T, T>> listeners = new ConcurrentLinkedQueue<>();

    public ListenableValue() {
        this(null);
    }

    public ListenableValue(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        final var old = this.value;
        this.value = value;
        listeners.forEach(l -> l.accept(old, this.value));
    }

    public T update(Function<T, T> updater) {
        set(updater.apply(get()));
        return get();
    }

    public void addListener(BiConsumer<T, T> listener) {
        listeners.add(listener);
    }

    public void removeListener(BiConsumer<T, T> listener) {
        listeners.remove(listener);
    }
}
