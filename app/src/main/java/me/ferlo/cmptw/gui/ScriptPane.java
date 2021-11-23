package me.ferlo.cmptw.gui;

import com.github.weisj.darklaf.components.text.NumberedTextComponent;
import com.github.weisj.darklaf.extensions.rsyntaxarea.DarklafRSyntaxTheme;
import me.ferlo.cmptw.hook.Hook;
import me.ferlo.cmptw.hook.KeyboardHookEvent;
import me.ferlo.cmptw.hook.KeyboardHookService;
import me.ferlo.cmptw.script.ScriptEngine;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.fife.ui.rsyntaxtextarea.Theme;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class ScriptPane extends JPanel {

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
                s -> {
                    final StringBuilder sb = new StringBuilder();
                    if(s.keyStroke().modifiers() != 0)
                        sb.append(KeyboardHookEvent.getModifiersText(s.keyStroke().modifiers(), " + ")).append(" + ");
                    sb.append(KeyEvent.getKeyText(s.keyStroke().keyCode()));
                    return sb.toString();
                },
                (v, newName) -> { throw new UnsupportedOperationException("Key stroke field is not supposed to be editable"); }
        ), new CC().growX().split(2));
        keyStrokeField.setEditable(false);

        final JButton keyStrokeButton;
        infoPanel.add(keyStrokeButton = new JButton("Change"));
        keyStrokeButton.addActionListener(evt -> SelectKeyStrokeDialog
                .selectKeyStroke(SwingUtilities.windowForComponent(this), keyboardHookService, null) // TODO: target device
                .thenAccept(keyboardEvt -> script.update(s -> s
                        .withKeyStroke(new Hook.KeyStroke(keyboardEvt.awtKeyCode(), keyboardEvt.modifiers())))));

        add(infoPanel, new CC().growX());
        add(new JLabel("Script: "));
        add(new NumberedTextComponent(new JListeningRSyntaxTextArea<>(
                script,
                Hook.HookScript::script,
                (v, newScript) -> v.update(a -> a.withScript(newScript))
        ) {{
            if(syntaxTheme == null)
                syntaxTheme = new DarklafRSyntaxTheme();
            syntaxTheme.apply(this);

            setSyntaxEditingStyle(scriptEngine.getSyntaxStyle());
            setCodeFoldingEnabled(true);
        }}), new CC().grow());
    }
}
