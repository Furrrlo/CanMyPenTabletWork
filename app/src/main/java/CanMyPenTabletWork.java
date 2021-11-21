import com.github.weisj.darklaf.LafManager;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import me.ferlo.cmptw.gui.CanMyPenTabletWorkTray;
import me.ferlo.cmptw.hook.*;
import me.ferlo.cmptw.process.Process;
import me.ferlo.cmptw.process.ProcessService;
import me.ferlo.cmptw.script.ScriptEngine;
import me.ferlo.cmptw.script.ScriptEnvironment;
import net.harawata.appdirs.AppDirsFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Paths;
import java.util.Optional;

public class CanMyPenTabletWork {
    public static void main(String[] args) throws Exception {
        // Redirect jul to slf4j
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        // TODO: catch startup exceptions and show a panel
        if (!SystemTray.isSupported())
            throw new AssertionError("System tray is not supported");

        final var keyboardHookService = KeyboardHookService.create();
        final var processService = ProcessService.create();
        final var scriptEngine = ScriptEnvironment.create().discoverEngine().get();
        final HookService hookService = new FileBasedHookService(scriptEngine, Paths.get(Optional
                .ofNullable(System.getenv("CMPTW_CONFIG_DATA"))
                .orElse(AppDirsFactory.getInstance().getUserConfigDir("CanMyPenTabletWork", null, null))));

        keyboardHookService.addListener((s0, l0, event) -> hook(hookService, processService, scriptEngine, event));

        IconFontSwing.register(FontAwesome.getIconFont());
        LafManager.installTheme(LafManager.getPreferredThemeStyle());

        final CanMyPenTabletWorkTray tray;
        SystemTray.getSystemTray().add(tray = new CanMyPenTabletWorkTray(hookService, keyboardHookService, processService));

        LafManager.enabledPreferenceChangeReporting(true);
        LafManager.addThemePreferenceChangeListener(tray);

        keyboardHookService.register();
        SwingUtilities.invokeLater(tray::showGui);
    }

    private static boolean hook(HookService hookService,
                                ProcessService processService,
                                ScriptEngine scriptEngine,
                                KeyboardHookEvent event) {
        if(!event.isKeyDown())
            return false;

        final Optional<Hook> maybeHook = hookService.getSaved().stream()
                .filter(h -> h.device().id().equalsIgnoreCase(event.device().getId()))
                .findFirst();
        if(maybeHook.isEmpty())
            return false;

        final Hook hook = maybeHook.get();
        final Optional<Process> maybeProcess = processService.getProcessForPid(event.pid());
        if(maybeProcess.isEmpty())
            return fallbackBehavior(hook);

        final Process process = maybeProcess.get();
        final Optional<Hook.ApplicationHook> maybeApplication = hook.applicationHooks().stream()
                .filter(a -> a.application().process().equalsIgnoreCase(process.name()))
                .findFirst();
        if(maybeApplication.isEmpty())
            return fallbackBehavior(hook);

        final Hook.ApplicationHook application = maybeApplication.get();
        final Optional<Hook.HookScript> maybeScript = application.scripts().stream()
                .filter(s -> s.keyStroke().keyCode() == event.awtKeyCode() && s.keyStroke().modifiers() == event.modifiers())
                .findFirst();
        if(maybeScript.isEmpty())
            return fallbackBehavior(hook);

        final Hook.HookScript script = maybeScript.get();
        if(script.scriptFile() != null)
            scriptEngine.execute(script.scriptFile());
        else
            scriptEngine.execute(script.script());
        return true;
    }

    private static boolean fallbackBehavior(Hook hook) {
        return switch (hook.fallbackBehavior()) {
            case IGNORE -> false;
            case DELETE -> true;
            case DELETE_AND_PLAY_SOUND -> {
                Toolkit.getDefaultToolkit().beep();
                yield true;
            }
        };
    }
}
