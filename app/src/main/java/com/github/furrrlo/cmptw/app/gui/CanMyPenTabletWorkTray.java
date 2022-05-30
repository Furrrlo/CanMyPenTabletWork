package com.github.furrrlo.cmptw.app.gui;

import com.github.furrrlo.cmptw.app.gui.hidpi.MultiResolutionIconFont;
import com.github.furrrlo.cmptw.app.hook.HookService;
import com.github.weisj.darklaf.theme.event.ThemePreferenceChangeEvent;
import com.github.weisj.darklaf.theme.event.ThemePreferenceListener;
import com.github.weisj.darklaf.theme.spec.ColorToneRule;
import jiconfont.icons.font_awesome.FontAwesome;
import com.github.furrrlo.cmptw.hook.KeyboardHookService;
import com.github.furrrlo.cmptw.process.ProcessService;
import com.github.furrrlo.cmptw.script.ScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CanMyPenTabletWorkTray extends TrayIcon implements LifecycleService, ThemePreferenceListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CanMyPenTabletWorkTray.class);

    private final HookService hookService;
    private final KeyboardHookService keyboardHookService;
    private final ScriptEngine scriptEngine;
    private final ProcessService processService;

    private CanMyPenTabletWorkFrame gui;

    public CanMyPenTabletWorkTray(HookService hookService,
                                  KeyboardHookService keyboardHookService,
                                  ScriptEngine scriptEngine,
                                  ProcessService processService) {
        super(
                new MultiResolutionIconFont(FontAwesome.PENCIL_SQUARE_O, 16, Color.WHITE),
                "CanMyPenTabletWork");
        setImageAutoSize(true);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1)
                    showGui();
            }
        });

        this.hookService = hookService;
        this.keyboardHookService = keyboardHookService;
        this.scriptEngine = scriptEngine;
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
        if(gui != null) {
            gui.setVisible(true);
            gui.setExtendedState(gui.getExtendedState() & ~JFrame.ICONIFIED);
            gui.toFront();
            gui.requestFocus();
            return;
        }

        gui = new Gui(hookService, keyboardHookService, scriptEngine, processService, getToolTip(), getImage());
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

    @Override
    public void themePreferenceChanged(ThemePreferenceChangeEvent e) {
        boolean isDarkTheme = e.getPreferredThemeStyle().getColorToneRule() == ColorToneRule.DARK;
        setImage(new MultiResolutionIconFont(FontAwesome.PENCIL_SQUARE_O, 16, isDarkTheme ? Color.WHITE : Color.BLACK));
        if(gui != null)
            gui.setIconImage(getImage());
    }

    private class Gui extends CanMyPenTabletWorkFrame {

        public Gui(HookService hookService,
                   KeyboardHookService keyboardHookService,
                   ScriptEngine scriptEngine,
                   ProcessService processService,
                   String title,
                   Image icon) throws HeadlessException {
            super(hookService, keyboardHookService, scriptEngine, processService, title, icon);
//            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }

//        @Override
//        public void dispose() {
//            super.dispose();
//
//            if(gui == this)
//                gui = null;
//        }
    }
}
