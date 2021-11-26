package me.ferlo.cmptw.gui;

import me.ferlo.cmptw.hook.Hook;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.function.BiFunction;

public class FallbackPane extends JPanel {

    private final ListenableValue<Hook.FallbackBehavior> fallbackBehavior;

    public FallbackPane(ListenableValue<Hook.FallbackBehavior> fallbackBehavior) {
        super(new MigLayout(new LC().wrapAfter(1).fillX()));
        this.fallbackBehavior = fallbackBehavior;

        add(new JLabel("When a key stroke is not matched by any hook:"));

        final ButtonGroup fallbackBehaviorGroup = new ButtonGroup();

        final BiFunction<Hook.FallbackBehavior, String, JListeningRadioButton<Hook.FallbackBehavior>> createBehaviorBtn =
                (behavior, text) -> {
                    final var btn = new JListeningRadioButton<>(
                            text,
                            fallbackBehavior,
                            b -> b == behavior,
                            (v, bool) -> {
                                if(bool)
                                    fallbackBehavior.set(behavior);
                            });
                    fallbackBehaviorGroup.add(btn);
                    return btn;
                };
        add(createBehaviorBtn.apply(Hook.FallbackBehavior.IGNORE, "Let it through"));
        add(createBehaviorBtn.apply(Hook.FallbackBehavior.DELETE, "Block it"));
        add(createBehaviorBtn.apply(Hook.FallbackBehavior.DELETE_AND_PLAY_SOUND, "Block it and play error sound"));
    }
}
