package me.ferlo.cmptw.gui;

import jiconfont.icons.font_awesome.FontAwesome;
import me.ferlo.cmptw.gui.hidpi.MultiResolutionIconFont;
import me.ferlo.cmptw.hook.HookService;
import me.ferlo.cmptw.hook.KeyboardHookService;
import me.ferlo.cmptw.process.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class CanMyPenTabletWorkTray extends TrayIcon implements LifecycleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CanMyPenTabletWorkTray.class);

    private final HookService hookService;
    private final KeyboardHookService keyboardHookService;
    private final ProcessService processService;

    private CanMyPenTabletWorkFrame gui;

    public CanMyPenTabletWorkTray(HookService hookService,
                                  KeyboardHookService keyboardHookService,
                                  ProcessService processService) {
        super(
                new MultiResolutionIconFont(FontAwesome.PENCIL_SQUARE_O, 16, Color.WHITE),
                "CanMyPenTabletWork");
        setImageAutoSize(true);
        addActionListener(evt -> showGui());

        this.hookService = hookService;
        this.keyboardHookService = keyboardHookService;
        this.processService = processService;

        final PopupMenu popup = new PopupMenu();

        // Create a pop-up menu components
        final var guiItem = new MenuItem("GUI");
        guiItem.addActionListener(evt -> showGui());
        popup.add(guiItem);

        final var exitItem = new MenuItem("Exit");
        exitItem.addActionListener(evt -> exit());
        popup.addSeparator();
        popup.add(exitItem);

        setPopupMenu(popup);
    }

    public void showGui() {
        if(gui != null)
            return;

        gui = new Gui(hookService, keyboardHookService, processService, getToolTip(), getImage());
        gui.setVisible(true);
    }

    @Override
    public void exit() {
        boolean failed = false;
        if(keyboardHookService != null) {
            try {
                keyboardHookService.unregister();
            } catch (Throwable t) {
                LOGGER.error("Failed to shut down KeyboardHookService", t);
                failed = true;
            }
        }

        if(gui != null) {
            try {
                gui.dispose();
            } catch (Throwable t) {
                LOGGER.error("Failed to dispose of GUI", t);
                failed = true;
            }
        }

        System.exit(failed ? 1 : 0);
    }

    private class Gui extends CanMyPenTabletWorkFrame {

        public Gui(HookService hookService,
                   KeyboardHookService keyboardHookService,
                   ProcessService processService,
                   String title,
                   Image icon) throws HeadlessException {
            super(hookService, keyboardHookService, processService, title, icon);
        }

        @Override
        public void dispose() {
            super.dispose();

            if(gui == this)
                gui = null;
        }
    }
}
