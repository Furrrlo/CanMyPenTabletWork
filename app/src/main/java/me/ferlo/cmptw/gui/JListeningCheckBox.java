package me.ferlo.cmptw.gui;

import javax.swing.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class JListeningCheckBox<T> extends JCheckBox {

    public JListeningCheckBox(String text,
                              ListenableValue<T> value,
                              Function<T, Boolean> getter,
                              BiConsumer<ListenableValue<T>, Boolean> setter) {
        super(text);

        setSelected(getter.apply(value.get()));
        addActionListener(evt -> {
            if(getter.apply(value.get()) != isSelected())
                setter.accept(value, isSelected());
        });
        value.addListener((olvV, newV) -> setSelected(getter.apply(newV)));
    }
}
