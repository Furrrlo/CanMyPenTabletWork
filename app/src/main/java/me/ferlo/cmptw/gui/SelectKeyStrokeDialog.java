package me.ferlo.cmptw.gui;

import me.ferlo.cmptw.hook.KeyboardHookEvent;
import me.ferlo.cmptw.hook.KeyboardHookListener;
import me.ferlo.cmptw.hook.KeyboardHookService;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.CompletableFuture;

public class SelectKeyStrokeDialog extends JDialog {

    private final KeyboardHookService keyboardHookService;
    private final String targetDeviceId;
    private final CompletableFuture<KeyboardHookEvent> eventFuture;

    public SelectKeyStrokeDialog(KeyboardHookService keyboardHookService,
                                 String targetDeviceId,
                                 CompletableFuture<KeyboardHookEvent> eventFuture) {
        super(null, ModalityType.APPLICATION_MODAL);
        this.keyboardHookService = keyboardHookService;
        this.targetDeviceId = targetDeviceId;
        this.eventFuture = eventFuture;
        init();
    }

    private SelectKeyStrokeDialog(Frame owner,
                                  KeyboardHookService keyboardHookService,
                                  String targetDeviceId,
                                  CompletableFuture<KeyboardHookEvent> eventFuture) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.keyboardHookService = keyboardHookService;
        this.targetDeviceId = targetDeviceId;
        this.eventFuture = eventFuture;
        init();
    }

    public SelectKeyStrokeDialog(Dialog owner,
                                 KeyboardHookService keyboardHookService,
                                 String targetDeviceId,
                                 CompletableFuture<KeyboardHookEvent> eventFuture) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.keyboardHookService = keyboardHookService;
        this.targetDeviceId = targetDeviceId;
        this.eventFuture = eventFuture;
        init();
    }

    public SelectKeyStrokeDialog(Window owner,
                                 KeyboardHookService keyboardHookService,
                                 String targetDeviceId,
                                 CompletableFuture<KeyboardHookEvent> eventFuture) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.keyboardHookService = keyboardHookService;
        this.targetDeviceId = targetDeviceId;
        this.eventFuture = eventFuture;
        init();
    }

    private void init() {
        setTitle("Select Key Stroke");

        final JPanel contentPane = new JPanel();
        contentPane.setLayout(new MigLayout(new LC().fill().align("center", "center")));
        final JLabel textLabel;
        contentPane.add(textLabel = new JLabel("Press the desired key on the target device"), new CC().alignX("center").alignY("center"));
        add(contentPane);

        pack();
        setSize(Math.max(200, getWidth()), Math.max(100, getHeight()));
        setLocationRelativeTo(getOwner());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        keyboardHookService.addListener((service, listener, event) -> {
            if(!event.isKeyDown())
                return KeyboardHookListener.ListenerResult.CONTINUE;
            if(!isFocused())
                return KeyboardHookListener.ListenerResult.CONTINUE;
            if(targetDeviceId != null && !event.device().getId().equals(targetDeviceId))
                return KeyboardHookListener.ListenerResult.CONTINUE;

            final StringBuilder sb = new StringBuilder();
            if(event.modifiers() != 0)
                sb.append(event.getModifiersText(" + ")).append(" + ");
            sb.append(KeyEvent.getKeyText(event.awtKeyCode()));
            textLabel.setText(sb.toString());

            switch (event.awtKeyCode()) {
                case KeyEvent.VK_SHIFT:
                case KeyEvent.VK_CONTROL:
                case KeyEvent.VK_ALT:
                case KeyEvent.VK_ALT_GRAPH:
                case KeyEvent.VK_META:
                case KeyEvent.VK_NUM_LOCK:
                case KeyEvent.VK_CAPS_LOCK:
                case KeyEvent.VK_SCROLL_LOCK:
                case KeyEvent.VK_UNDEFINED:
                    return KeyboardHookListener.ListenerResult.CONTINUE;
            }

            eventFuture.complete(event);
            service.removeListener(listener);
            setVisible(false);
            return KeyboardHookListener.ListenerResult.DELETE;
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        eventFuture.complete(null);
    }

    public static CompletableFuture<KeyboardHookEvent> selectKeyStroke(KeyboardHookService keyboardHookService,
                                                                       String targetDeviceId) {
        final var future = new CompletableFuture<KeyboardHookEvent>();
        new SelectKeyStrokeDialog(keyboardHookService, targetDeviceId, future).setVisible(true);
        return future;
    }

    public static CompletableFuture<KeyboardHookEvent> selectKeyStroke(Frame owner,
                                                                       KeyboardHookService keyboardHookService,
                                                                       String targetDeviceId) {
        final var future = new CompletableFuture<KeyboardHookEvent>();
        new SelectKeyStrokeDialog(owner, keyboardHookService, targetDeviceId, future).setVisible(true);
        return future;
    }

    public static CompletableFuture<KeyboardHookEvent> selectKeyStroke(Dialog owner,
                                                                       KeyboardHookService keyboardHookService,
                                                                       String targetDeviceId) {
        final var future = new CompletableFuture<KeyboardHookEvent>();
        new SelectKeyStrokeDialog(owner, keyboardHookService, targetDeviceId, future).setVisible(true);
        return future;
    }

    public static CompletableFuture<KeyboardHookEvent> selectKeyStroke(Window owner,
                                                                       KeyboardHookService keyboardHookService,
                                                                       String targetDeviceId) {
        final var future = new CompletableFuture<KeyboardHookEvent>();
        new SelectKeyStrokeDialog(owner, keyboardHookService, targetDeviceId, future).setVisible(true);
        return future;
    }
}
