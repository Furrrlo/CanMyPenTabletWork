package me.ferlo.cmptw.gui;

import me.ferlo.cmptw.hook.Hook;
import me.ferlo.cmptw.hook.KeyboardHookService;
import me.ferlo.cmptw.process.ProcessService;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

class FallbackTab extends JTabbedPaneIconTab {

    private final ListenableValue<Hook.FallbackBehavior> fallbackBehavior;

    public FallbackTab(ListenableValue<Hook.FallbackBehavior> fallbackBehavior) {
        super(
                "Fallback",
                null,
                tabComponentFor("Fallback", null),
                new FallbackPane(fallbackBehavior),
                "Fallback"
        );

        this.fallbackBehavior = fallbackBehavior;
    }
}
