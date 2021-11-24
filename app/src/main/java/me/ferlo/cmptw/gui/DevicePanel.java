package me.ferlo.cmptw.gui;

import jiconfont.icons.font_awesome.FontAwesome;
import me.ferlo.cmptw.gui.hidpi.MultiResolutionIconFont;
import me.ferlo.cmptw.gui.tabbed.JMyTabbedPane;
import me.ferlo.cmptw.hook.Hook;
import me.ferlo.cmptw.hook.KeyboardHookService;
import me.ferlo.cmptw.process.Process;
import me.ferlo.cmptw.process.ProcessService;
import me.ferlo.cmptw.script.ScriptEngine;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

public class DevicePanel extends JPanel {

    private final KeyboardHookService keyboardHookService;
    private final ScriptEngine scriptEngine;
    private final ProcessService processService;

    private final ListenableValue<Hook> hook;
    private final ListenableValue<Hook.Device> device;

    private final JMyTabbedPane<JTabbedPaneIconTab> applicationsPane;
    private final JButton addApplicationBtn;
    private final JButton removeApplicationBtn;

    public DevicePanel(KeyboardHookService keyboardHookService,
                       ScriptEngine scriptEngine,
                       ProcessService processService,
                       ListenableValue<Hook> hook, ListenableValue<Hook.Device> device) {
        this.processService = processService;
        this.scriptEngine = scriptEngine;
        this.keyboardHookService = keyboardHookService;
        this.hook = hook;
        this.device = device;

        setLayout(new MigLayout(
                new LC().wrapAfter(1).fill(),
                new AC().grow(),
                new AC().gap().grow()
        ));

        final JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new MigLayout(new LC().fillX().wrapAfter(1).minWidth("0px"), new AC().grow()));

        infoPanel.add(new JLabel("Name: "), new CC().split(2));
        infoPanel.add(new JListeningTextField<>(
                device,
                Hook.Device::name,
                (v, newName) -> v.update(d -> d.withName(newName))
        ), new CC().growX());

        final JLabel idLabel;
        infoPanel.add(idLabel = new JLabel("ID: " + device.get().id()), new CC().minWidth("0px"));
        device.addListener((oldV, newV) -> idLabel.setText("ID: " + newV.id()));

        final JLabel nameLabel;
        infoPanel.add(nameLabel = new JLabel("Description: " + device.get().desc()), new CC().minWidth("0px"));
        device.addListener((oldV, newV) -> nameLabel.setText("Description: " + newV.desc()));

        add(infoPanel, new CC().growX());

        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new MigLayout(new LC().flowY().alignY("top").insetsAll("0")));
        add(buttonsPanel, new CC().growY().split(2));

        applicationsPane = new JMyTabbedPane<>();

        final var fallbackBehavior = new ListenableValue<>(hook.get().fallbackBehavior());
        fallbackBehavior.addListener((oldV, newV) -> hook.update(h -> h.withFallbackBehavior(newV)));
        applicationsPane.addTab(new FallbackTab(fallbackBehavior));
        if(applicationsPane.getTabCount() > 0)
            applicationsPane.setSelectedIndex(0);

        hook.get().applicationHooks().forEach(this::addApplicationHookComponent);

        addApplicationBtn = new JButton(new ImageIcon(new MultiResolutionIconFont(FontAwesome.PLUS, 14, new Color(0, 150, 0))));
        addApplicationBtn.setMargin(new Insets(2, 2, 2, 2));
        addApplicationBtn.addActionListener(evt -> SelectProcessDialog
                .selectDevice(SwingUtilities.windowForComponent(this), processService, true)
                .thenAccept(process -> SwingUtilities.invokeLater(() -> {
                    if (process == null)
                        return;

                    final Optional<Hook.ApplicationHook> maybeApplicationHook = getApplicationHookFor(process);
                    if(maybeApplicationHook.isPresent()) {
                        JOptionPane.showMessageDialog(
                                SwingUtilities.windowForComponent(this),
                                String.format("Process %s was already added as %s",
                                        process.name(),
                                        maybeApplicationHook.get().application().name()),
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    final var applicationHook = new Hook.ApplicationHook(
                            new Hook.Application(
                                    process.name(),
                                    process.name().toLowerCase(Locale.ROOT).endsWith(".exe") ?
                                            capitalize(process.name().substring(0, process.name().length() - ".exe".length())) :
                                            capitalize(process.name()),
                                    process.iconPath()),
                            Collections.emptyList());
                    hook.update(h -> h.addApplicationHook(applicationHook));
                    addApplicationHookComponent(applicationHook);
                })));
        buttonsPanel.add(addApplicationBtn);

        removeApplicationBtn = new JButton(new ImageIcon(new MultiResolutionIconFont(FontAwesome.MINUS, 14, new Color(150, 0, 0))));
        removeApplicationBtn.setMargin(new Insets(2, 2, 2, 2));

        removeApplicationBtn.setEnabled(applicationsPane.getSelectedIndex() >= 0 &&
                applicationsPane.getTabCount() > 0 &&
                applicationsPane.getSelectedTab() instanceof ApplicationTab);
        applicationsPane.addChangeListener(evt -> removeApplicationBtn.setEnabled(applicationsPane.getSelectedIndex() >= 0 &&
                applicationsPane.getTabCount() > 0 &&
                applicationsPane.getSelectedTab() instanceof ApplicationTab));

        removeApplicationBtn.addActionListener(evt -> {
            final var tab = applicationsPane.getSelectedTab();
            if(!(tab instanceof ApplicationTab applicationTab))
                return;

            applicationTab.remove();
            final var applicationHook = applicationTab.getApplicationHook();
            hook.update(h -> h.removeApplicationHook(applicationHook));
        });
        buttonsPanel.add(removeApplicationBtn);

        add(applicationsPane, new CC().grow());
    }

    private void addApplicationHookComponent(Hook.ApplicationHook applicationHookIn) {
        final var applicationHook = new ListenableValue<>(applicationHookIn);
        applicationHook.addListener((oldV, newV) -> hook.update(h -> h.replaceApplicationHook(oldV, newV)));

        final var tab = applicationsPane.insertTab(
                new ApplicationTab(
                        keyboardHookService,
                        scriptEngine,
                        processService,
                        this::getApplicationHookFor,
                        applicationHook),
                applicationsPane.getTabCount() - 1);
        tab.select();
    }

    private Optional<Hook.ApplicationHook> getApplicationHookFor(Process process) {
        return applicationsPane.getTabs().stream()
                .filter(ApplicationTab.class::isInstance)
                .map(ApplicationTab.class::cast)
                .map(ApplicationTab::getApplicationHook)
                .filter(a -> a.application().process().equals(process.name()))
                .findFirst();
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
