package com.github.furrrlo.cmptw.app.settings;

import com.github.furrrlo.cmptw.script.ScriptEngine;

class SettingsScriptEngine extends DelegateScriptEngine {

    private ScriptEngine scriptEngine;

    @Override
    protected ScriptEngine getDelegate() {
        return scriptEngine;
    }
}
