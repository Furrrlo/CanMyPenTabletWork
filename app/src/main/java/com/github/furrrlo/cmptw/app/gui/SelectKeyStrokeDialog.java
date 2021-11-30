package com.github.furrrlo.cmptw.app.gui;

import com.github.furrrlo.cmptw.hook.KeyboardHookEvent;
import com.github.furrrlo.cmptw.hook.KeyboardHookListener;
import com.github.furrrlo.cmptw.hook.KeyboardHookService;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class SelectKeyStrokeDialog extends JDialog {

    private final KeyboardHookService keyboardHookService;
    private final String targetDeviceId;
    private final CompletableFuture<Result> eventFuture;
    private KeyboardHookEvent lastEvent;
    private int toggleKeysMask;

    public SelectKeyStrokeDialog(KeyboardHookService keyboardHookService,
                                 String targetDeviceId,
                                 int toggleKeysMask,
                                 CompletableFuture<Result> eventFuture) {
        super(null, ModalityType.APPLICATION_MODAL);
        this.keyboardHookService = keyboardHookService;
        this.targetDeviceId = targetDeviceId;
        this.toggleKeysMask = toggleKeysMask;
        this.eventFuture = eventFuture;
        init();
    }

    private SelectKeyStrokeDialog(Frame owner,
                                  KeyboardHookService keyboardHookService,
                                  String targetDeviceId,
                                  int toggleKeysMask,
                                  CompletableFuture<Result> eventFuture) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.keyboardHookService = keyboardHookService;
        this.targetDeviceId = targetDeviceId;
        this.toggleKeysMask = toggleKeysMask;
        this.eventFuture = eventFuture;
        init();
    }

    public SelectKeyStrokeDialog(Dialog owner,
                                 KeyboardHookService keyboardHookService,
                                 String targetDeviceId,
                                 int toggleKeysMask,
                                 CompletableFuture<Result> eventFuture) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.keyboardHookService = keyboardHookService;
        this.targetDeviceId = targetDeviceId;
        this.toggleKeysMask = toggleKeysMask;
        this.eventFuture = eventFuture;
        init();
    }

    public SelectKeyStrokeDialog(Window owner,
                                 KeyboardHookService keyboardHookService,
                                 String targetDeviceId,
                                 int toggleKeysMask,
                                 CompletableFuture<Result> eventFuture) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.keyboardHookService = keyboardHookService;
        this.targetDeviceId = targetDeviceId;
        this.toggleKeysMask = toggleKeysMask;
        this.eventFuture = eventFuture;
        init();
    }

    private void init() {
        setTitle("Select Key Stroke");

        final JPanel contentPane = new JPanel();
        contentPane.setLayout(new MigLayout(new LC().fill().wrapAfter(1)));

        final JLabel textLabel;
        contentPane.add(
                textLabel = new JLabel(getEventText(lastEvent, toggleKeysMask)),
                new CC().push().alignX("center").alignY("center"));

        contentPane.add(new JLabel("Match toggle keys: "));
        final BiFunction<Integer, String, JCheckBox> createToggleKeyCheckBox = (modifier, text) -> {
            final JCheckBox checkBox = new JCheckBox(text);
            checkBox.addActionListener(evt -> {
                final boolean selected = checkBox.isSelected();
                if(selected == ((toggleKeysMask & modifier) != 0))
                    return;

                if(selected)
                    toggleKeysMask |= modifier;
                else
                    toggleKeysMask &= ~modifier;
                textLabel.setText(getEventText(lastEvent, toggleKeysMask));
            });
            return checkBox;
        };
        contentPane.add(createToggleKeyCheckBox.apply(KeyboardHookEvent.CAPS_LOCK_MASK, "Caps Lock"), new CC().split(3));
        contentPane.add(createToggleKeyCheckBox.apply(KeyboardHookEvent.NUM_LOCK_MASK, "Num Lock"));
        contentPane.add(createToggleKeyCheckBox.apply(KeyboardHookEvent.SCROLL_LOCK_MASK, "Scroll Lock"));

        add(contentPane);

        pack();
        setMinimumSize(new Dimension(Math.max(250, getWidth()), Math.max(125, getHeight())));
        setLocationRelativeTo(getOwner());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        keyboardHookService.addListener((service, listener, event) -> {
            if(!event.isKeyDown())
                return KeyboardHookListener.ListenerResult.CONTINUE;
            if(!isFocused())
                return KeyboardHookListener.ListenerResult.CONTINUE;
            if(targetDeviceId != null && !event.device().getId().equals(targetDeviceId))
                return KeyboardHookListener.ListenerResult.CONTINUE;

            textLabel.setText(getEventText(event, toggleKeysMask));
            this.lastEvent = event;

            if(event.isModifierKey() || event.awtKeyCode() == KeyEvent.VK_UNDEFINED)
                return KeyboardHookListener.ListenerResult.CONTINUE;

            eventFuture.complete(new Result(event, toggleKeysMask));
            service.removeListener(listener);
            setVisible(false);
            return KeyboardHookListener.ListenerResult.DELETE;
        });
    }

    private static String getEventText(KeyboardHookEvent event, int toggleKeysMask) {
        if(event == null)
            return "Press the desired key on the target device";

        final StringBuilder sb = new StringBuilder();
        final String modifiersText;
        if(!(modifiersText = KeyboardHookEvent.getModifiersText(event.modifiers(), toggleKeysMask, " + ")).isEmpty())
            sb.append(modifiersText).append(" + ");
        sb.append(KeyEvent.getKeyText(event.awtKeyCode()));
        return sb.toString();
    }

    @Override
    public void dispose() {
        super.dispose();
        eventFuture.complete(null);
    }

    public static CompletableFuture<Result> selectKeyStroke(KeyboardHookService keyboardHookService,
                                                            String targetDeviceId,
                                                            int toggleKeysMask) {
        final var future = new CompletableFuture<Result>();
        new SelectKeyStrokeDialog(keyboardHookService, targetDeviceId, toggleKeysMask, future).setVisible(true);
        return future;
    }

    public static CompletableFuture<Result> selectKeyStroke(Frame owner,
                                                            KeyboardHookService keyboardHookService,
                                                            String targetDeviceId,
                                                            int toggleKeysMask) {
        final var future = new CompletableFuture<Result>();
        new SelectKeyStrokeDialog(owner, keyboardHookService, targetDeviceId, toggleKeysMask, future).setVisible(true);
        return future;
    }

    public static CompletableFuture<Result> selectKeyStroke(Dialog owner,
                                                            KeyboardHookService keyboardHookService,
                                                            String targetDeviceId,
                                                            int toggleKeysMask) {
        final var future = new CompletableFuture<Result>();
        new SelectKeyStrokeDialog(owner, keyboardHookService, targetDeviceId, toggleKeysMask, future).setVisible(true);
        return future;
    }

    public static CompletableFuture<Result> selectKeyStroke(Window owner,
                                                            KeyboardHookService keyboardHookService,
                                                            String targetDeviceId,
                                                            int toggleKeysMask) {
        final var future = new CompletableFuture<Result>();
        new SelectKeyStrokeDialog(owner, keyboardHookService, targetDeviceId, toggleKeysMask, future).setVisible(true);
        return future;
    }

    public record Result(KeyboardHookEvent evt, int toggleKeysMask) {
    }
}
