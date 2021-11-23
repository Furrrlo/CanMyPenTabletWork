package me.ferlo.cmptw.script;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public interface ScriptEngine {

    void execute(String script);

    default void execute(Path script) {
        try {
            execute(Files.readString(script));
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to read string from script " + script, ex);
        }
    }

    String getFileExtension();

    String getNewScript();

    default void createSyntaxStyle(AbstractTokenMakerFactory tokenMakerFactory, FoldParserManager foldParserManager) {
    }

    default String getSyntaxStyle() {
        return SyntaxConstants.SYNTAX_STYLE_NONE;
    }
}
