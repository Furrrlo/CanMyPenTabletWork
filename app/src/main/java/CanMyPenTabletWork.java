import me.ferlo.cmptw.hook.*;
import me.ferlo.cmptw.process.Process;
import me.ferlo.cmptw.process.ProcessService;
import me.ferlo.cmptw.script.ScriptEngine;
import me.ferlo.cmptw.script.ScriptEnvironment;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.awt.*;
import java.util.Optional;

public class CanMyPenTabletWork {
    public static void main(String[] args) throws Exception {
        // Redirect jul to slf4j
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        final var keyboardHookService = KeyboardHookService.create();
        final HookService hookService = new InMemoryHookService();
        final var processService = ProcessService.create();
        final var scriptEngine = ScriptEnvironment.create().discoverEngine().get();

        keyboardHookService.addListener(event -> hook(hookService, processService, scriptEngine, event));
        keyboardHookService.register();
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
        scriptEngine.execute("msgbox % \"my ahk version: \" A_AhkVersion"); // TODO
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
