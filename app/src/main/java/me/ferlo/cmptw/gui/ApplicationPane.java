package me.ferlo.cmptw.gui;

import jiconfont.icons.font_awesome.FontAwesome;
import me.ferlo.cmptw.gui.hidpi.MultiResolutionIconFont;
import me.ferlo.cmptw.gui.tabbed.JMyTabbedPane;
import me.ferlo.cmptw.hook.Hook;
import me.ferlo.cmptw.hook.KeyboardHookService;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

class ApplicationPane extends JPanel {

    private final KeyboardHookService keyboardHookService;

    private final ListenableValue<Hook.Application> application;
    private final ListenableValue<Hook.ApplicationHook> applicationHook;

    private final JMyTabbedPane<ScriptTab> scriptsPane;
    private final JButton addScriptBtn;
    private final JButton removeScriptBtn;

    public ApplicationPane(KeyboardHookService keyboardHookService,
                           ListenableValue<Hook.Application> application,
                           ListenableValue<Hook.ApplicationHook> applicationHook) {
        this.keyboardHookService = keyboardHookService;

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
        ), new CC().growX());

        infoPanel.add(new JLabel("Icon: "));
        infoPanel.add(new JListeningTextField<>(
                application,
                app -> app.icon().toAbsolutePath().toString(),
                (v, newIcon) -> v.update(a -> a.withIcon(Path.of(newIcon)))
        ), new CC().growX().split(2));

        final JButton browseIconBtn = new JButton("...");
        browseIconBtn.addActionListener(evt -> {
            final var chooser = new JFileChooser();
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

        addScriptBtn = new JButton(new ImageIcon(new MultiResolutionIconFont(FontAwesome.PLUS, 14, new Color(0, 150, 0))));
        addScriptBtn.setMargin(new Insets(2, 2, 2, 2));
        addScriptBtn.addActionListener(evt -> SelectKeyStrokeDialog
                .selectKeyStroke(SwingUtilities.windowForComponent(this), keyboardHookService, null) // TODO: target device
                .thenAccept(keyboardEvt -> SwingUtilities.invokeLater(() -> {
                    if(keyboardEvt == null)
                        return;

                    final var script = new Hook.HookScript(
                            "New",
                            new Hook.KeyStroke(keyboardEvt.awtKeyCode(), keyboardEvt.modifiers()),
                            "");
                    addScriptComponent(script);
                    applicationHook.update(d -> d.addScript(script));
                })));
        buttonsPanel.add(addScriptBtn);

        removeScriptBtn = new JButton(new ImageIcon(new MultiResolutionIconFont(FontAwesome.MINUS, 14, new Color(150, 0, 0))));
        removeScriptBtn.setMargin(new Insets(2, 2, 2, 2));
        removeScriptBtn.setEnabled(scriptsPane.getSelectedIndex() >= 0);
        scriptsPane.addChangeListener(evt -> removeScriptBtn.setEnabled(scriptsPane.getSelectedIndex() >= 0 && scriptsPane.getSelectedIndex() != scriptsPane.getTabCount() - 1));
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

        final var tab = scriptsPane.insertTab(new ScriptTab(keyboardHookService, script), scriptsPane.getTabCount());
        tab.select();
    }
}
