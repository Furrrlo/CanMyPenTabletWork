package com.github.furrrlo.cmptw.app.gui;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.components.loading.LoadingIndicator;
import com.github.weisj.darklaf.components.text.NumberedTextComponent;
import com.github.weisj.darklaf.extensions.rsyntaxarea.DarklafRSyntaxTheme;
import jiconfont.icons.font_awesome.FontAwesome;
import com.github.furrrlo.cmptw.app.gui.hidpi.MultiResolutionIconFont;
import com.github.furrrlo.cmptw.app.hook.Hook;
import com.github.furrrlo.cmptw.hook.KeyboardHookEvent;
import com.github.furrrlo.cmptw.hook.KeyboardHookService;
import com.github.furrrlo.cmptw.script.ScriptEngine;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ScriptPane extends JPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptPane.class);
    private static final ExecutorService VALIDATION_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        final Thread th = Executors.defaultThreadFactory().newThread(r);
        th.setName("script-validation-thread");
        th.setDaemon(true);
        th.setUncaughtExceptionHandler((t, e) -> LOGGER.error("Uncaught exception in script validation thread", e));
        return th;
    });
    private static Theme syntaxTheme;

    private final KeyboardHookService keyboardHookService;
    private final ScriptEngine scriptEngine;
    private final ListenableValue<Hook.HookScript> script;

    public ScriptPane(KeyboardHookService keyboardHookService,
                      ScriptEngine scriptEngine,
                      ListenableValue<Hook.HookScript> script) {
        this.keyboardHookService = keyboardHookService;
        this.scriptEngine = scriptEngine;
        this.script = script;

        setLayout(new MigLayout(
                new LC().wrapAfter(1).fill(),
                new AC().grow(),
                new AC().gap().gap().grow()
        ));

        final JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new MigLayout(new LC().fillX().wrapAfter(2).insetsAll("0"), new AC().gap().grow()));

        infoPanel.add(new JLabel("Name: "));
        infoPanel.add(new JListeningTextField<>(
                script,
                Hook.HookScript::name,
                (v, newName) -> script.update(s -> s.withName(newName))
        ), new CC().growX());

        infoPanel.add(new JLabel("Key stroke: "));

        final JTextField keyStrokeField;
        infoPanel.add(keyStrokeField = new JListeningTextField<>(
                script,
                s -> s.keyStroke().getText(" + "),
                (v, newName) -> { throw new UnsupportedOperationException("Key stroke field is not supposed to be editable"); }
        ), new CC().growX().split(2));
        keyStrokeField.setEditable(false);

        final JButton keyStrokeButton;
        infoPanel.add(keyStrokeButton = new JButton("Change"));
        keyStrokeButton.addActionListener(evt -> SelectKeyStrokeDialog
                .selectKeyStroke(
                        SwingUtilities.windowForComponent(this),
                        keyboardHookService,
                        null,  // TODO: target device
                        script.get().keyStroke().toggleModifiersMask())
                .thenAccept(res -> {
                    if(res != null)
                        script.update(s -> s.withKeyStroke(new Hook.KeyStroke(
                                res.evt().awtKeyCode(), res.evt().modifiers(), res.toggleKeysMask())));
                }));

        infoPanel.add(new JLabel("Match toggle keys: "));
        final BiFunction<Integer, String, JListeningCheckBox<Hook.HookScript>> makeToggleKeyCheckBox =
                (modifier, text) -> new JListeningCheckBox<>(
                        text,
                        script,
                        v -> (v.keyStroke().toggleModifiersMask() & modifier) != 0,
                        (v, bool) -> v.update(s -> s.withKeyStroke(s.keyStroke().updateToggleModifiersMask(mask -> {
                            if(bool)
                                return mask | modifier;
                            return mask & ~modifier;
                        }))));
        infoPanel.add(makeToggleKeyCheckBox.apply(KeyboardHookEvent.CAPS_LOCK_MASK, "Caps Lock"), new CC().split(3));
        infoPanel.add(makeToggleKeyCheckBox.apply(KeyboardHookEvent.NUM_LOCK_MASK, "Num Lock"));
        infoPanel.add(makeToggleKeyCheckBox.apply(KeyboardHookEvent.SCROLL_LOCK_MASK, "Scroll Lock"));

        add(infoPanel, new CC().growX());
        add(new JLabel("Script: "), new CC().split(3).growX());

        final LoadingIndicator validatingIndicator = new LoadingIndicator();
        validatingIndicator.setEnabled(false);
        add(validatingIndicator);

        final Function<Boolean, ImageIcon> iconSupplier = valid -> new ImageIcon(new MultiResolutionIconFont(
                FontAwesome.WRENCH, 14,
                valid ? Color.GREEN.darker() : Color.RED.darker()));
        final JButton validateBtn = new JButton(iconSupplier.apply(true));
        validateBtn.setMargin(new Insets(2, 2, 2, 2));
        validateBtn.setToolTipText("Validate");
        validateBtn.addActionListener(evt -> {
            validatingIndicator.setEnabled(true);
            validatingIndicator.setRunning(true);
            validateBtn.setEnabled(false);

            VALIDATION_EXECUTOR.submit(() -> {
                final Boolean[] valid = { null };
                try {
                    valid[0] = scriptEngine.validate(script.get().script(), true);
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        if(valid[0] != null)
                            validateBtn.setIcon(iconSupplier.apply(valid[0]));

                        validateBtn.setEnabled(true);
                        validatingIndicator.setRunning(false);
                        validatingIndicator.setEnabled(false);
                    });
                }
            });
        });
        add(validateBtn);

        final var textArea = new JListeningRSyntaxTextArea<>(
                script,
                Hook.HookScript::script,
                (v, newScript) -> v.update(a -> a.withScript(newScript))
        ) {{
            if(LafManager.isInstalled()) {
                if (syntaxTheme == null)
                    syntaxTheme = new DarklafRSyntaxTheme();
                syntaxTheme.apply(this);
            }
            scriptEngine.getCompletionProvider().ifPresent(provider -> {
                final AutoCompletion autoCompletion = new AutoCompletion(provider);
                autoCompletion.setTriggerKey(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK));
                autoCompletion.setAutoCompleteEnabled(true);
                autoCompletion.setAutoCompleteSingleChoices(false);
                autoCompletion.setAutoActivationEnabled(true);
                autoCompletion.setShowDescWindow(true);
                autoCompletion.install(this);
            });

            setSyntaxEditingStyle(scriptEngine.getSyntaxStyle());
            setCodeFoldingEnabled(true);
        }};
        add(LafManager.isInstalled() ? new NumberedTextComponent(textArea) : new RTextScrollPane(textArea), new CC().grow());
    }
}
