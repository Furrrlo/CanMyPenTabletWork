package com.github.furrrlo.cmptw.app;

import com.github.weisj.darklaf.LafManager;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import com.github.furrrlo.cmptw.app.gui.CanMyPenTabletWorkTray;
import com.github.furrrlo.cmptw.app.hook.FileBasedHookService;
import com.github.furrrlo.cmptw.app.hook.Hook;
import com.github.furrrlo.cmptw.app.hook.HookService;
import com.github.furrrlo.cmptw.windows.gui.hidpi.WinWindowHiDpiFix;
import com.github.furrrlo.cmptw.hook.KeyboardHookEvent;
import com.github.furrrlo.cmptw.hook.KeyboardHookListener;
import com.github.furrrlo.cmptw.hook.KeyboardHookService;
import com.github.furrrlo.cmptw.process.Process;
import com.github.furrrlo.cmptw.process.ProcessService;
import com.github.furrrlo.cmptw.script.ScriptEngine;
import com.github.furrrlo.cmptw.script.ScriptEnvironment;
import net.harawata.appdirs.AppDirsFactory;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.oxbow.swingbits.dialog.task.IContentDesign;
import org.oxbow.swingbits.dialog.task.TaskDialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.nio.file.Paths;
import java.util.Optional;

class CanMyPenTabletWork {

    private static final Logger LOGGER = LoggerFactory.getLogger(CanMyPenTabletWork.class);

    public static void main(String[] args) {
        try {
            // Redirect jul to slf4j
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();

            if (!SystemTray.isSupported())
                throw new AssertionError("System tray is not supported");

            final var keyboardHookService = KeyboardHookService.create();
            final var processService = ProcessService.create();
            final var scriptEngine = ScriptEnvironment.create().discoverEngine().get();
            final HookService hookService = new FileBasedHookService(scriptEngine, Paths.get(Optional
                    .ofNullable(System.getenv("CMPTW_CONFIG_DATA"))
                    .orElse(AppDirsFactory.getInstance().getUserConfigDir("CanMyPenTabletWork", null, null))));

            keyboardHookService.addListener((s0, l0, event) -> hook(hookService, processService, scriptEngine, event));

            WinWindowHiDpiFix.install();
            IconFontSwing.register(FontAwesome.getIconFont());
            LafManager.registerDefaultsAdjustmentTask((currentTheme, properties) -> {
                UIManager.getDefaults().put(IContentDesign.COLOR_MESSAGE_BACKGROUND, properties.get("controlBackground"));
                UIManager.getDefaults().put(IContentDesign.COLOR_INSTRUCTION_FOREGROUND, properties.get("textForegroundDefault"));
            });
            LafManager.installTheme(LafManager.getPreferredThemeStyle());
            scriptEngine.createSyntaxStyle((AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance(), FoldParserManager.get());

            final CanMyPenTabletWorkTray tray;
            SystemTray.getSystemTray().add(tray = new CanMyPenTabletWorkTray(hookService, keyboardHookService, scriptEngine, processService));

            LafManager.enabledPreferenceChangeReporting(true);
            LafManager.addThemePreferenceChangeListener(tray);

            keyboardHookService.register();
            SwingUtilities.invokeLater(tray::showGui);
        } catch (Exception ex) {
            LOGGER.error("Failed to start", ex);
            TaskDialogs.showException(new Exception("Failed to start", ex));
            System.exit(-1);
        }
    }

    private static KeyboardHookListener.ListenerResult hook(HookService hookService,
                                                            ProcessService processService,
                                                            ScriptEngine scriptEngine,
                                                            KeyboardHookEvent event) {

        final Optional<Hook> maybeHook = hookService.getSaved().stream()
                .filter(h -> h.device().id().equalsIgnoreCase(event.device().getId()))
                .findFirst();
        if(maybeHook.isEmpty())
            return KeyboardHookListener.ListenerResult.CONTINUE;

        final Hook hook = maybeHook.get();
        final Optional<Process> maybeProcess = processService.getProcessForPid(event.pid());
        if(maybeProcess.isEmpty())
            return fallbackBehavior(event, hook);

        final Process process = maybeProcess.get();
        final Optional<Hook.ApplicationHook> maybeApplication = hook.applicationHooks().stream()
                .filter(a -> a.application().process().equalsIgnoreCase(process.name()))
                .findFirst();
        if(maybeApplication.isEmpty())
            return fallbackBehavior(event, hook);

        final Hook.ApplicationHook application = maybeApplication.get();
        final Optional<Hook.HookScript> maybeScript = application.scripts().stream()
                .filter(s -> s.keyStroke().matches(event.awtKeyCode(), event.modifiers()))
                .findFirst();
        if(maybeScript.isEmpty())
            return fallbackBehavior(event, hook);

        final Hook.HookScript script = maybeScript.get();
        if(event.isKeyDown()) {
            if(script.scriptFile() != null)
                scriptEngine.execute(script.scriptFile());
            else
                scriptEngine.execute(script.script());
        }

        return KeyboardHookListener.ListenerResult.CANCEL;
    }

    private static KeyboardHookListener.ListenerResult fallbackBehavior(KeyboardHookEvent event, Hook hook) {
        return switch (hook.fallbackBehavior()) {
            case IGNORE -> KeyboardHookListener.ListenerResult.CONTINUE;
            case DELETE -> KeyboardHookListener.ListenerResult.CANCEL;
            case DELETE_AND_PLAY_SOUND -> {
                if(event.isKeyDown() && !event.isModifierKey() && event.awtKeyCode() != KeyEvent.VK_UNDEFINED)
                    Toolkit.getDefaultToolkit().beep();
                yield KeyboardHookListener.ListenerResult.CANCEL;
            }
        };
    }
}
