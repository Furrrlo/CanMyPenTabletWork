package com.github.furrrlo.cmptw.app.settings;

import com.github.furrrlo.cmptw.script.ScriptEngine;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;

import java.nio.file.Path;
import java.util.Optional;

abstract class DelegateScriptEngine {

    protected abstract ScriptEngine getDelegate();

    public boolean validate(String script, boolean canShowGui) {
        return getDelegate().validate(script, canShowGui);
    }

    public boolean validate(Path script, boolean canShowGui) {
        return getDelegate().validate(script, canShowGui);
    }

    public void execute(String script) {
        getDelegate().execute(script);
    }

    public void execute(Path script) {
        getDelegate().execute(script);
    }

    public String getFileExtension() {
        return getDelegate().getFileExtension();
    }

    public String getNewScript() {
        return getDelegate().getNewScript();
    }

    public void createSyntaxStyle(AbstractTokenMakerFactory tokenMakerFactory, FoldParserManager foldParserManager) {
        getDelegate().createSyntaxStyle(tokenMakerFactory, foldParserManager);
    }

    public String getSyntaxStyle() {
        return getDelegate().getSyntaxStyle();
    }

    public Optional<CompletionProvider> getCompletionProvider() {
        return getDelegate().getCompletionProvider();
    }
}
