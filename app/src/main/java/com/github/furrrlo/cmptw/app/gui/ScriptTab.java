package com.github.furrrlo.cmptw.app.gui;

import com.github.furrrlo.cmptw.app.gui.tabbed.JTabbedPaneTab;
import com.github.furrrlo.cmptw.app.hook.Hook;
import com.github.furrrlo.cmptw.hook.KeyboardHookService;
import com.github.furrrlo.cmptw.script.ScriptEngine;

public class ScriptTab extends JTabbedPaneTab {

    private final KeyboardHookService keyboardHookService;
    private final ScriptEngine scriptEngine;
    private final ListenableValue<Hook.HookScript> script;

    public ScriptTab(KeyboardHookService keyboardHookService,
                     ScriptEngine scriptEngine,
                     ListenableValue<Hook.HookScript> script) {
        super(
                script.get().name(),
                null,
                new ScriptPane(keyboardHookService, scriptEngine, script),
                script.get().name()
        );

        this.keyboardHookService = keyboardHookService;
        this.scriptEngine = scriptEngine;
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
