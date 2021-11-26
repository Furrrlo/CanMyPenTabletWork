package me.ferlo.cmptw.gui;

import me.ferlo.cmptw.gui.hidpi.JDialogHiDpi;
import me.ferlo.cmptw.hook.KeyboardHookDevice;
import me.ferlo.cmptw.hook.KeyboardHookListener;
import me.ferlo.cmptw.hook.KeyboardHookService;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CompletableFuture;

public class SelectDeviceDialog extends JDialogHiDpi {

    private final KeyboardHookService keyboardHookService;
    private final CompletableFuture<KeyboardHookDevice> deviceFuture;

    public SelectDeviceDialog(KeyboardHookService keyboardHookService,
                              CompletableFuture<KeyboardHookDevice> deviceFuture) {
        super(null, Dialog.ModalityType.APPLICATION_MODAL);
        this.keyboardHookService = keyboardHookService;
        this.deviceFuture = deviceFuture;
        init();
    }

    private SelectDeviceDialog(Frame owner,
                               KeyboardHookService keyboardHookService,
                               CompletableFuture<KeyboardHookDevice> deviceFuture) {
        super(owner, Dialog.ModalityType.APPLICATION_MODAL);
        this.keyboardHookService = keyboardHookService;
        this.deviceFuture = deviceFuture;
        init();
    }

    public SelectDeviceDialog(Dialog owner,
                              KeyboardHookService keyboardHookService,
                              CompletableFuture<KeyboardHookDevice> deviceFuture) {
        super(owner, Dialog.ModalityType.APPLICATION_MODAL);
        this.keyboardHookService = keyboardHookService;
        this.deviceFuture = deviceFuture;
        init();
    }

    public SelectDeviceDialog(Window owner,
                              KeyboardHookService keyboardHookService,
                              CompletableFuture<KeyboardHookDevice> deviceFuture) {
        super(owner, Dialog.ModalityType.APPLICATION_MODAL);
        this.keyboardHookService = keyboardHookService;
        this.deviceFuture = deviceFuture;
        init();
    }

    private void init() {
        setTitle("Select Device");

        final JPanel contentPane = new JPanel();
        contentPane.setLayout(new MigLayout(new LC().fill().align("center", "center")));
        contentPane.add(new JLabel("Press any key on the target device"), new CC().alignX("center").alignY("center"));
        add(contentPane);

        pack();
        setMinimumSize(new Dimension(Math.max(200, getWidth()), Math.max(100, getHeight())));
        setLocationRelativeTo(getOwner());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        keyboardHookService.addListener((service, listener, event) -> {
            if(!isFocused())
                return KeyboardHookListener.ListenerResult.CONTINUE;

            deviceFuture.complete(event.device());
            service.removeListener(listener);
            setVisible(false);
            return KeyboardHookListener.ListenerResult.DELETE;
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        deviceFuture.complete(null);
    }

    public static CompletableFuture<KeyboardHookDevice> selectDevice(KeyboardHookService keyboardHookService) {
        final var future = new CompletableFuture<KeyboardHookDevice>();
        new SelectDeviceDialog(keyboardHookService, future).setVisible(true);
        return future;
    }

    public static CompletableFuture<KeyboardHookDevice> selectDevice(Frame owner,
                                                                     KeyboardHookService keyboardHookService) {
        final var future = new CompletableFuture<KeyboardHookDevice>();
        new SelectDeviceDialog(owner, keyboardHookService, future).setVisible(true);
        return future;
    }

    public static CompletableFuture<KeyboardHookDevice> selectDevice(Dialog owner,
                                                                     KeyboardHookService keyboardHookService) {
        final var future = new CompletableFuture<KeyboardHookDevice>();
        new SelectDeviceDialog(owner, keyboardHookService, future).setVisible(true);
        return future;
    }

    public static CompletableFuture<KeyboardHookDevice> selectDevice(Window owner,
                                                                     KeyboardHookService keyboardHookService) {
        final var future = new CompletableFuture<KeyboardHookDevice>();
        new SelectDeviceDialog(owner, keyboardHookService, future).setVisible(true);
        return future;
    }
}
