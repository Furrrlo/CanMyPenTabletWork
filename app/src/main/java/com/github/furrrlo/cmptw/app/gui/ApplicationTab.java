package com.github.furrrlo.cmptw.app.gui;

import com.github.furrrlo.cmptw.app.gui.hidpi.MultiResolutionIconImage;
import com.github.furrrlo.cmptw.app.hook.Hook;
import com.github.furrrlo.cmptw.hook.KeyboardHookService;
import com.github.furrrlo.cmptw.process.Process;
import com.github.furrrlo.cmptw.process.ProcessService;
import com.github.furrrlo.cmptw.script.ScriptEngine;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.util.function.Function;

class ApplicationTab extends JTabbedPaneIconTab {

    private final KeyboardHookService keyboardHookService;
    private final ScriptEngine scriptEngine;
    private final ProcessService processService;
    private final ListenableValue<Hook.ApplicationHook> applicationHook;

    public ApplicationTab(KeyboardHookService keyboardHookService,
                          ScriptEngine scriptEngine,
                          ProcessService processService,
                          Function<Process, Optional<Hook.ApplicationHook>> getApplicationHookFor,
                          ListenableValue<Hook.ApplicationHook> applicationHook) {
        this(keyboardHookService, scriptEngine, processService, getApplicationHookFor, applicationHook, new ListenableValue<>(applicationHook.get().application()));
    }

    private ApplicationTab(KeyboardHookService keyboardHookService,
                           ScriptEngine scriptEngine,
                           ProcessService processService,
                           Function<Process, Optional<Hook.ApplicationHook>> getApplicationHookFor,
                           ListenableValue<Hook.ApplicationHook> applicationHook,
                           ListenableValue<Hook.Application> application) {
        super(
                applicationHook.get().application().name(),
                new ImageIcon(new MultiResolutionIconImage(
                        TAB_ICON_SIZE,
                        processService.extractProcessIcons(applicationHook.get().application().icon()))),
                tabComponentFor(processService, applicationHook.get().application()),
                new ApplicationPane(keyboardHookService, scriptEngine, processService, getApplicationHookFor, application, applicationHook),
                applicationHook.get().application().name()
        );

        application.addListener((oldV, newV) -> {
            applicationHook.update(d -> d.withApplication(newV));

            setTitle(newV.name());
            setTooltipText(newV.name());
            setIcon(new ImageIcon(new MultiResolutionIconImage(
                    TAB_ICON_SIZE,
                    processService.extractProcessIcons(application.get().icon()))));
            setTabComponent(tabComponentFor(processService, newV));
        });

        this.keyboardHookService = keyboardHookService;
        this.scriptEngine = scriptEngine;
        this.processService = processService;
        this.applicationHook = applicationHook;
    }

    private static Component tabComponentFor(ProcessService processService, Hook.Application app) {
        return tabComponentFor(
                app.name(),
                new MultiResolutionIconImage(
                        TAB_ICON_SIZE,
                        processService.extractProcessIcons(app.icon())));
    }

    public Hook.ApplicationHook getApplicationHook() {
        return applicationHook.get();
    }
}
