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
    requires transitive com.github.furrrlo.cmptw.core;

    provides ProcessServiceProvider with WinProcessServiceProvider;
    provides KeyboardHookServiceProvider with WinKeyboardHookServiceProvider;
    provides ScriptEnvironmentProvider with AhkScriptEnvironmentProvider;
}