package com.github.furrrlo.cmptw.app.gui;

import com.github.furrrlo.cmptw.app.hook.Hook;

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
