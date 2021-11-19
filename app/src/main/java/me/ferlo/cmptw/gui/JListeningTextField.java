package me.ferlo.cmptw.gui;

import javax.swing.*;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class JListeningTextField<T> extends JTextField {

    public JListeningTextField(ListenableValue<T> value,
                               Function<T, String> getter,
                               BiConsumer<ListenableValue<T>, String> setter) {
        setText(getter.apply(value.get()));

        value.addListener((oldV, newV) -> {
            final String newName = getter.apply(newV);
            if (Objects.equals(newName, getText()))
                return;

            SwingUtilities.invokeLater(() -> setText(newName));
        });
        getDocument().addDocumentListener((SimpleDocumentListener) e -> SwingUtilities.invokeLater(() -> {
            final String newName = getText();
            if (Objects.equals(newName, getter.apply(value.get())))
                return;

            setter.accept(value, newName);
        }));
    }
}
