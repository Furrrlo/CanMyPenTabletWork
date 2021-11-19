package me.ferlo.cmptw.gui;

import me.ferlo.cmptw.hook.Hook;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class FallbackPane extends JPanel {

    private final ListenableValue<Hook.FallbackBehavior> fallbackBehavior;

    public FallbackPane(ListenableValue<Hook.FallbackBehavior> fallbackBehavior) {
        super(new MigLayout(new LC().wrapAfter(1).fillX()));
        this.fallbackBehavior = fallbackBehavior;

        add(new JLabel("When a key stroke is not matched by any hook:"));

        final ButtonGroup fallbackBehaviorGroup = new ButtonGroup();

        final JRadioButton ignoreBtn = new JRadioButton("Let it through");
        ignoreBtn.setSelected(fallbackBehavior.get() == Hook.FallbackBehavior.IGNORE);
        ignoreBtn.addActionListener(evt -> fallbackBehavior.set(Hook.FallbackBehavior.IGNORE));
        ignoreBtn.addActionListener(evt -> {
            if(fallbackBehavior.get() != Hook.FallbackBehavior.IGNORE)
                fallbackBehavior.set(Hook.FallbackBehavior.IGNORE);
        });
        fallbackBehavior.addListener((olvV, newV) -> ignoreBtn.setSelected(newV.equals(Hook.FallbackBehavior.IGNORE)));
        fallbackBehaviorGroup.add(ignoreBtn);
        add(ignoreBtn);

        final JRadioButton deleteBtn = new JRadioButton("Block it");
        deleteBtn.setSelected(fallbackBehavior.get() == Hook.FallbackBehavior.DELETE);
        deleteBtn.addActionListener(evt -> {
            if(fallbackBehavior.get() != Hook.FallbackBehavior.DELETE)
                fallbackBehavior.set(Hook.FallbackBehavior.DELETE);
        });
        fallbackBehavior.addListener((olvV, newV) -> deleteBtn.setSelected(newV.equals(Hook.FallbackBehavior.DELETE)));
        fallbackBehaviorGroup.add(deleteBtn);
        add(deleteBtn);

        final JRadioButton deleteAndSoundBtn = new JRadioButton("Block it and play error sound");
        deleteAndSoundBtn.setSelected(fallbackBehavior.get() == Hook.FallbackBehavior.DELETE_AND_PLAY_SOUND);
        deleteAndSoundBtn.addActionListener(evt -> {
            if(fallbackBehavior.get() != Hook.FallbackBehavior.DELETE_AND_PLAY_SOUND)
                fallbackBehavior.set(Hook.FallbackBehavior.DELETE_AND_PLAY_SOUND);
        });
        fallbackBehavior.addListener((olvV, newV) -> deleteAndSoundBtn.setSelected(newV.equals(Hook.FallbackBehavior.DELETE_AND_PLAY_SOUND)));
        fallbackBehaviorGroup.add(deleteAndSoundBtn);
        add(deleteAndSoundBtn);
    }
}
