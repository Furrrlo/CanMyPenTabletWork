import com.github.furrrlo.cmptw.windows.ahk.AhkScriptEnvironmentProvider;
import com.github.furrrlo.cmptw.windows.hook.WinKeyboardHookServiceProvider;
import com.github.furrrlo.cmptw.windows.process.WinProcessServiceProvider;
import com.github.furrrlo.cmptw.hook.KeyboardHookServiceProvider;
import com.github.furrrlo.cmptw.process.ProcessServiceProvider;
import com.github.furrrlo.cmptw.script.ScriptEnvironmentProvider;

module com.github.furrrlo.cmptw.windows {
    requires org.slf4j;

    requires com.sun.jna;
    requires com.sun.jna.platform;
    exports com.github.furrrlo.cmptw.windows.raw to com.sun.jna;

    // Export for RSyntaxTextArea AbstractTokenMakerFactory
    exports com.github.furrrlo.cmptw.windows.ahk.syntax;

    requires transitive com.github.furrrlo.cmptw.core;
    exports com.github.furrrlo.cmptw.windows.process to com.github.furrrlo.cmptw.core, com.sun.jna;
    exports com.github.furrrlo.cmptw.windows.hook to com.github.furrrlo.cmptw.core;
    exports com.github.furrrlo.cmptw.windows.ahk to com.github.furrrlo.cmptw.core;

    requires static com.google.auto.service;
    provides ProcessServiceProvider with WinProcessServiceProvider;
    provides KeyboardHookServiceProvider with WinKeyboardHookServiceProvider;
    provides ScriptEnvironmentProvider with AhkScriptEnvironmentProvider;
}