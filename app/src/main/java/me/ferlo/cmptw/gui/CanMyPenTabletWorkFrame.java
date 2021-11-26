package me.ferlo.cmptw.gui;

import me.ferlo.cmptw.gui.hidpi.JFrameHiDpi;
import me.ferlo.cmptw.hook.HookService;
import me.ferlo.cmptw.hook.KeyboardHookService;
import me.ferlo.cmptw.process.ProcessService;
import me.ferlo.cmptw.script.ScriptEngine;

import javax.swing.*;
import java.awt.*;

public class CanMyPenTabletWorkFrame extends JFrameHiDpi {

    public CanMyPenTabletWorkFrame(HookService hookService,
                                   KeyboardHookService keyboardHookService,
                                   ScriptEngine scriptEngine,
                                   ProcessService processService,
                                   String title,
                                   Image icon) throws HeadlessException {
        setTitle(title);
        setIconImage(icon);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setContentPane(new HooksPane(hookService, keyboardHookService, scriptEngine, processService));
        setMinimumSize(new Dimension(700, 700));
        setSize(700, 700);
        setLocationRelativeTo(null);
    }
}
