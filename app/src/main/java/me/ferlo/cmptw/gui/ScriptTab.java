package me.ferlo.cmptw.gui;

import me.ferlo.cmptw.gui.tabbed.JTabbedPaneTab;
import me.ferlo.cmptw.hook.Hook;
import me.ferlo.cmptw.hook.KeyboardHookService;

public class ScriptTab extends JTabbedPaneTab {

    private final KeyboardHookService keyboardHookService;
    private final ListenableValue<Hook.HookScript> script;

    public ScriptTab(KeyboardHookService keyboardHookService,
                     ListenableValue<Hook.HookScript> script) {
        super(
                script.get().name(),
                null,
                new ScriptPane(keyboardHookService, script),
                script.get().name()
        );

        this.keyboardHookService = keyboardHookService;
        this.script = script;

        script.addListener((oldV, newV) -> {
            setTitle(newV.name());
            setTooltipText(newV.name());
        });
    }

    public Hook.HookScript getScript() {
        return script.get();
    }
}
