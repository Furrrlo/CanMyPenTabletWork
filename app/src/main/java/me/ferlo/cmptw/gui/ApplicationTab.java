package me.ferlo.cmptw.gui;

import me.ferlo.cmptw.gui.hidpi.MultiResolutionIconImage;
import me.ferlo.cmptw.hook.Hook;
import me.ferlo.cmptw.hook.KeyboardHookService;
import me.ferlo.cmptw.process.ProcessService;

import javax.swing.*;
import java.awt.*;

class ApplicationTab extends JTabbedPaneIconTab {

    private final KeyboardHookService keyboardHookService;
    private final ProcessService processService;
    private final ListenableValue<Hook.ApplicationHook> applicationHook;

    public ApplicationTab(KeyboardHookService keyboardHookService,
                          ProcessService processService,
                          ListenableValue<Hook.ApplicationHook> applicationHook) {
        this(keyboardHookService, processService, applicationHook, new ListenableValue<>(applicationHook.get().application()));
    }

    private ApplicationTab(KeyboardHookService keyboardHookService,
                           ProcessService processService,
                           ListenableValue<Hook.ApplicationHook> applicationHook,
                           ListenableValue<Hook.Application> application) {
        super(
                applicationHook.get().application().name(),
                new ImageIcon(new MultiResolutionIconImage(
                        TAB_ICON_SIZE,
                        processService.extractProcessIcons(applicationHook.get().application().icon()))),
                tabComponentFor(processService, applicationHook.get().application()),
                new ApplicationPane(keyboardHookService, application, applicationHook),
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

        this.processService = processService;
        this.keyboardHookService = keyboardHookService;
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
