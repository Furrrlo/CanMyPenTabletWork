package com.github.furrrlo.cmptw.app.gui;

import com.github.furrrlo.cmptw.app.hook.Hook;
import jiconfont.icons.font_awesome.FontAwesome;
import com.github.furrrlo.cmptw.app.gui.hidpi.MultiResolutionIconFont;
import com.github.furrrlo.cmptw.app.gui.tabbed.JMyTabbedPane;
import com.github.furrrlo.cmptw.hook.KeyboardHookService;
import com.github.furrrlo.cmptw.process.Process;
import com.github.furrrlo.cmptw.process.ProcessService;
import com.github.furrrlo.cmptw.script.ScriptEngine;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

class ApplicationPane extends JPanel {

    private final KeyboardHookService keyboardHookService;
    private final ScriptEngine scriptEngine;
    private final ProcessService processService;

    private final ListenableValue<Hook.Application> application;
    private final ListenableValue<Hook.ApplicationHook> applicationHook;

    private final JMyTabbedPane<ScriptTab> scriptsPane;
    private final JButton addScriptBtn;
    private final JButton removeScriptBtn;

    public ApplicationPane(KeyboardHookService keyboardHookService,
                           ScriptEngine scriptEngine,
                           ProcessService processService,
                           Function<Process, Optional<Hook.ApplicationHook>> getApplicationHookFor,
                           ListenableValue<Hook.Application> application,
                           ListenableValue<Hook.ApplicationHook> applicationHook) {
        this.keyboardHookService = keyboardHookService;
        this.scriptEngine = scriptEngine;
        this.processService = processService;

        this.application = application;
        this.applicationHook = applicationHook;

        setLayout(new MigLayout(
                new LC().wrapAfter(1).fill(),
                new AC().grow(),
                new AC().gap().grow()
        ));

        final JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new MigLayout(new LC().fillX().wrapAfter(2), new AC().gap().grow()));

        infoPanel.add(new JLabel("Name: "));
        infoPanel.add(new JListeningTextField<>(
                application,
                Hook.Application::name,
                (v, newName) -> v.update(a -> a.withName(newName))
        ), new CC().growX());

        infoPanel.add(new JLabel("Process: "));
        infoPanel.add(new JListeningTextField<>(
                application,
                Hook.Application::process,
                (v, newProcess) -> v.update(a -> a.withProcess(newProcess))
        ), new CC().growX().split(2));

        final JButton selectProcessBtn = new JButton("...");
        selectProcessBtn.addActionListener(evt -> SelectProcessDialog
                .selectDevice(SwingUtilities.windowForComponent(this), processService, true)
                .thenAccept(process -> SwingUtilities.invokeLater(() -> {
                    if (process == null)
                        return;

                    final Optional<Hook.ApplicationHook> maybeApplicationHook = getApplicationHookFor.apply(process);
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

                    application.update(app -> app.withProcess(process.name()));
                })));
        infoPanel.add(selectProcessBtn);

        infoPanel.add(new JLabel("Icon: "));
        infoPanel.add(new JListeningTextField<>(
                application,
                app -> app.icon().toAbsolutePath().toString(),
                (v, newIcon) -> v.update(a -> a.withIcon(Path.of(newIcon)))
        ), new CC().growX().split(2));

        final JButton browseIconBtn = new JButton("...");
        browseIconBtn.addActionListener(evt -> {
            final var chooser = new JFileChooser();
            chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                    String.format("Icons (%s)", processService.getIconExtensions().stream()
                            .map(ext -> "*." + ext)
                            .collect(Collectors.joining(", "))),
                    processService.getIconExtensions().toArray(String[]::new)));
            chooser.setAcceptAllFileFilterUsed(true);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setMultiSelectionEnabled(false);

            final var res = chooser.showOpenDialog(ApplicationPane.this);
            if (res != JFileChooser.APPROVE_OPTION)
                return;

            final File newIcon = chooser.getSelectedFile().getAbsoluteFile();
            if (Objects.equals(newIcon.toPath(), application.get().icon().toAbsolutePath()))
                return;

            application.update(a -> a.withIcon(newIcon.toPath()));
        });
        infoPanel.add(browseIconBtn);

        add(infoPanel, new CC().growX());

        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new MigLayout(new LC().flowY().alignY("top").insetsAll("0")));
        add(buttonsPanel, new CC().growY().split(2));

        scriptsPane = new JMyTabbedPane<>();
        scriptsPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        addScriptBtn = new JButton(new ImageIcon(new MultiResolutionIconFont(FontAwesome.PLUS, 14, new Color(0, 150, 0))));
        addScriptBtn.setMargin(new Insets(2, 2, 2, 2));
        addScriptBtn.addActionListener(evt -> SelectKeyStrokeDialog
                .selectKeyStroke(SwingUtilities.windowForComponent(this), keyboardHookService, null, 0) // TODO: target device
                .thenAccept(res -> SwingUtilities.invokeLater(() -> {
                    if(res == null)
                        return;

                    final var script = new Hook.HookScript(
                            "New",
                            new Hook.KeyStroke(res.evt().awtKeyCode(), res.evt().modifiers(), res.toggleKeysMask()),
                            scriptEngine.getNewScript());
                    addScriptComponent(script);
                    applicationHook.update(d -> d.addScript(script));
                })));
        buttonsPanel.add(addScriptBtn);

        removeScriptBtn = new JButton(new ImageIcon(new MultiResolutionIconFont(FontAwesome.MINUS, 14, new Color(150, 0, 0))));
        removeScriptBtn.setMargin(new Insets(2, 2, 2, 2));
        removeScriptBtn.setEnabled(scriptsPane.getSelectedIndex() >= 0);
        scriptsPane.addChangeListener(evt -> removeScriptBtn.setEnabled(scriptsPane.getSelectedIndex() >= 0));
        removeScriptBtn.addActionListener(evt -> {
            final var tab = scriptsPane.getSelectedTab();
            tab.remove();

            final var script = tab.getScript();
            applicationHook.update(d -> d.removeScript(script));
        });
        buttonsPanel.add(removeScriptBtn);

        scriptsPane.setTabPlacement(JTabbedPane.LEFT);
        applicationHook.get().scripts().forEach(this::addScriptComponent);
        add(scriptsPane, new CC().grow());
    }

    private void addScriptComponent(Hook.HookScript scriptIn) {
        final var script = new ListenableValue<>(scriptIn);
        script.addListener((oldV, newV) -> applicationHook.update(a -> a.replaceScript(oldV, newV)));

        final var tab = scriptsPane.insertTab(new ScriptTab(keyboardHookService, scriptEngine, script), scriptsPane.getTabCount());
        tab.select();
    }
}
