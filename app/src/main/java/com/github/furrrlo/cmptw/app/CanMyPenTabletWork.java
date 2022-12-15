package com.github.furrrlo.cmptw.app;

import com.github.furrrlo.cmptw.app.gui.CanMyPenTabletWorkTray;
import com.github.furrrlo.cmptw.app.hook.FileBasedHookService;
import com.github.furrrlo.cmptw.app.hook.Hook;
import com.github.furrrlo.cmptw.app.hook.HookService;
import com.github.furrrlo.cmptw.hook.KeyboardHookEvent;
import com.github.furrrlo.cmptw.hook.KeyboardHookListener;
import com.github.furrrlo.cmptw.hook.KeyboardHookService;
import com.github.furrrlo.cmptw.process.Process;
import com.github.furrrlo.cmptw.process.ProcessService;
import com.github.furrrlo.cmptw.script.ScriptEngine;
import com.github.furrrlo.cmptw.script.ScriptEnvironment;
import com.github.furrrlo.cmptw.windows.gui.hidpi.WinWindowHiDpiFix;
import com.github.weisj.darklaf.LafManager;
import in.pratanumandal.unique4j.*;
import in.pratanumandal.unique4j.unixsocketchannel.UnixSocketChannelIpcFactory;
import io.github.furrrlo.jlaunchcmd.JLaunchCmd;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
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
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

class CanMyPenTabletWork {

    private static final String APP_ID = "com.github.furrrlo.cmptw.app-huisdfhuisef-78t34678234.j8";

    public static void main(String[] args) {
        try {
            doMain();
        } catch (Exception ex) {
            LoggerFactory.getLogger(CanMyPenTabletWork.class).error("Failed to start", ex);
            TaskDialogs.showException(new Exception("Failed to start", ex));
            System.exit(-1);
        }
    }

    private static void doMain() throws Exception {
        // Here we only have jna non-redirected jul logging, which will be redirected later
        final Path configFolder = Paths.get(Optional
                .ofNullable(System.getenv("CMPTW_CONFIG_DATA"))
                .orElse(AppDirsFactory.getInstance().getUserConfigDir("CanMyPenTabletWork", null, null)));

        // Change the working directory. This is the only supported way in Java :I
        // Do not do it if running from an IDE as it breaks debugging (for obvious reasons)
        final boolean isRunFromIde = !System.getProperty("cmptw.ide", "false").equalsIgnoreCase("false");
        if(!isRunFromIde && !Path.of("").toAbsolutePath().equals(configFolder.toAbsolutePath())) {
            System.out.println("Restarting to change working dir...");
            System.exit(JLaunchCmd.create()
                    .restartProcessBuilder()
                    .directory(configFolder.toFile())
                    .inheritIO()
                    .start()
                    .waitFor());
            throw new AssertionError("System.exit(...)");
        }

        // Set log4j config: try to use the external file (if present) in the working dir (that we just changed),
        // otherwise it will fall back to the one on the classpath
        System.setProperty("log4j.configurationFile", "log4j2.xml");

        // TODO: remove this once fixed
        // Suppress slf4j error message (see https://issues.apache.org/jira/browse/LOG4J2-3139)
        final var errorStream = System.err;
        try {
            System.setErr(new PrintStream(new OutputStream() {
                @Override
                public void write(int arg0) {
                }
            }));
            // Force slf4j to find the log4j implementation
            LoggerFactory.getILoggerFactory();
        } finally {
            System.setErr(errorStream);
        }

        // Redirect jul to slf4j
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        if (!SystemTray.isSupported())
            throw new AssertionError("System tray is not supported");

        final Integer exitCode = Unique4j.withConfig(Unique4jConfig
                .createDefault(APP_ID)
                .lockFolder(configFolder.resolve("lock").toFile())
                .ipcFactory(new UnixSocketChannelIpcFactory())
                .exceptionHandler(new UnexpectedExceptionHandler() {
                    private static final Logger LOGGER = LoggerFactory.getLogger(Unique4j.class);

                    @Override
                    public void unexpectedException(IpcServer server, IpcClient client, Throwable t) {
                        LOGGER.error("Unexpected Unique4j exception (server: {}, client: {})", server, client, t);
                    }
                })
        ).requestSingleInstanceThenReturn(instance -> instance.firstInstance(ctx -> {
            final var keyboardHookService = KeyboardHookService.create();
            final var processService = ProcessService.create();
            final var scriptEngine = ScriptEnvironment.create().discoverEngine().get();
            final HookService hookService = new FileBasedHookService(scriptEngine, configFolder);

            keyboardHookService.addListener((s0, l0, event) -> hook(hookService, processService, scriptEngine, event));

            WinWindowHiDpiFix.install();
            IconFontSwing.register(FontAwesome.getIconFont());
            LafManager.registerDefaultsAdjustmentTask((currentTheme, properties) -> {
                UIManager.getDefaults().put(IContentDesign.COLOR_MESSAGE_BACKGROUND, properties.get("controlBackground"));
                UIManager.getDefaults().put(IContentDesign.COLOR_INSTRUCTION_FOREGROUND, properties.get("textForegroundDefault"));
            });
            // TODO: Theme Settings
            LafManager.install();
            LafManager.installTheme(LafManager.getPreferredThemeStyle());
            scriptEngine.createSyntaxStyle((AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance(), FoldParserManager.get());

            final CompletableFuture<Integer> exitFuture = new CompletableFuture<>();
            final CanMyPenTabletWorkTray tray;
            SystemTray.getSystemTray().add(tray = new CanMyPenTabletWorkTray(
                    hookService,
                    keyboardHookService,
                    scriptEngine,
                    processService,
                    exitFuture::complete));

            LafManager.enabledPreferenceChangeReporting(true);
            LafManager.addThemePreferenceChangeListener(tray);

            keyboardHookService.register();
            SwingUtilities.invokeLater(tray::showGui);

            ctx.otherInstancesListener(otherInstanceClient -> {
                // Other instance opened, bring up UI
                SwingUtilities.invokeLater(tray::showGui);
            });

            return ctx.waitForEventThenReturn(exitFuture::thenAccept);
        }).otherInstances(ctx -> ctx.doNothingThenReturn(0)));

        System.exit(Objects.requireNonNull(exitCode, "exitCode is null"));
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
