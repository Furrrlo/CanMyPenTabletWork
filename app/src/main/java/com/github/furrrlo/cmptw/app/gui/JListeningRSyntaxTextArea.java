package com.github.furrrlo.cmptw.app.gui;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class JListeningRSyntaxTextArea<T> extends RSyntaxTextArea {

    public JListeningRSyntaxTextArea(ListenableValue<T> value,
                                     Function<T, String> getter,
                                     BiConsumer<ListenableValue<T>, String> setter) {
        setText(getter.apply(value.get()));
        discardAllEdits();

        value.addListener((oldV, newV) -> {
            final String newName = getter.apply(newV);
            if (Objects.equals(newName, getText()))
                return;

            SwingUtilities.invokeLater(() -> {
                discardAllEdits();
                setText(newName);
            });
        });
        getDocument().addDocumentListener((SimpleDocumentListener) e -> SwingUtilities.invokeLater(() -> {
            final String newName = getText();
            if (Objects.equals(newName, getter.apply(value.get())))
                return;

            setter.accept(value, newName);
        }));
    }
}
