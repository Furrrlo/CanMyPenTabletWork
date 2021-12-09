import com.github.furrrlo.cmptw.hook.KeyboardHookServiceProvider;
import com.github.furrrlo.cmptw.process.ProcessServiceProvider;
import com.github.furrrlo.cmptw.script.ScriptEnvironmentProvider;

module com.github.furrrlo.cmptw.core {
    requires org.slf4j;

    requires transitive java.desktop;
    requires transitive com.fifesoft.rsyntaxtextarea;
    requires transitive com.fifesoft.autocomplete;

    exports com.github.furrrlo.cmptw.hook;
    exports com.github.furrrlo.cmptw.process;
    exports com.github.furrrlo.cmptw.script;

    uses KeyboardHookServiceProvider;
    uses ProcessServiceProvider;
    uses ScriptEnvironmentProvider;
}