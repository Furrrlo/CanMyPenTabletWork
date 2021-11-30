package com.github.furrrlo.cmptw.app.gui;

import com.github.furrrlo.cmptw.app.hook.HookService;
import com.github.furrrlo.cmptw.hook.KeyboardHookService;
import com.github.furrrlo.cmptw.process.ProcessService;
import com.github.furrrlo.cmptw.script.ScriptEngine;

import javax.swing.*;
import java.awt.*;

public class CanMyPenTabletWorkFrame extends JFrame {

    public CanMyPenTabletWorkFrame(HookService hookService,
                                   KeyboardHookService keyboardHookService,
                                   ScriptEngine scriptEngine,
                                   ProcessService processService,
                                   String title,
                                   Image icon) throws HeadlessException {
        setTitle(title);
        setIconImage(icon);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setContentPane(new HooksPane(hookService, keyboardHookService, scriptEngine, processService));
        setMinimumSize(new Dimension(700, 700));
        setSize(700, 700);
        setLocationRelativeTo(null);
    }
}
