package me.ferlo.cmptw.hook;

import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.*;
import java.util.function.IntUnaryOperator;

public record Hook(Device device,
                   Collection<ApplicationHook> applicationHooks,
                   FallbackBehavior fallbackBehavior,
                   Path folder) {

    public Hook(Device device, Collection<ApplicationHook> applicationHooks, FallbackBehavior fallbackBehavior) {
        this(device, applicationHooks, fallbackBehavior, null);
    }

    public Hook(Device device, Collection<ApplicationHook> applicationHooks, FallbackBehavior fallbackBehavior, Path folder) {
        this.device = device;
        this.applicationHooks = Collections.unmodifiableCollection(new LinkedHashSet<>(applicationHooks));
        this.fallbackBehavior = fallbackBehavior;
        this.folder = folder;
    }

    public Hook withDevice(Device device) {
        return new Hook(device, applicationHooks, fallbackBehavior, folder);
    }

    public Hook withApplicationHooks(Collection<ApplicationHook> applicationHooks) {
        return new Hook(device, applicationHooks, fallbackBehavior, folder);
    }

    public Hook addApplicationHook(ApplicationHook applicationHook) {
        final Collection<ApplicationHook> hooks = new LinkedHashSet<>(this.applicationHooks);
        hooks.add(applicationHook);
        return new Hook(device, hooks, fallbackBehavior, folder);
    }

    public Hook removeApplicationHook(ApplicationHook applicationHook) {
        final Collection<ApplicationHook> hooks = new LinkedHashSet<>(this.applicationHooks);
        hooks.remove(applicationHook);
        return new Hook(device, hooks, fallbackBehavior, folder);
    }

    public Hook replaceApplicationHook(ApplicationHook oldApplicationHook, ApplicationHook newApplicationHook) {
        final List<ApplicationHook> hooks = new ArrayList<>(this.applicationHooks);
        hooks.set(hooks.indexOf(oldApplicationHook), newApplicationHook);
        return new Hook(device, hooks, fallbackBehavior, folder);
    }

    public Hook withFallbackBehavior(FallbackBehavior fallbackBehavior) {
        return new Hook(device, applicationHooks, fallbackBehavior, folder);
    }

    public Hook withFolder(Path hookFolder) {
        return new Hook(device, applicationHooks, fallbackBehavior, hookFolder);
    }

    public record Device(String id, String name, String desc) {

        public Device withName(String name) {
            return new Device(id, name, desc);
        }

        public Device withDesc(String desc) {
            return new Device(id, name, desc);
        }
    }

    public record ApplicationHook(Application application, Collection<HookScript> scripts, Path folder) {

        public ApplicationHook(Application application, Collection<HookScript> scripts) {
            this(application, scripts, null);
        }

        public ApplicationHook(Application application, Collection<HookScript> scripts, Path folder) {
            this.application = application;
            this.scripts = Collections.unmodifiableCollection(new LinkedHashSet<>(scripts));
            this.folder = folder;
        }

        public ApplicationHook withApplication(Application application) {
            return new ApplicationHook(application, scripts, folder);
        }

        public ApplicationHook withScripts(Collection<HookScript> scripts) {
            return new ApplicationHook(application, scripts, folder);
        }

        public ApplicationHook addScript(HookScript script) {
            final Collection<HookScript> scripts = new LinkedHashSet<>(this.scripts);
            scripts.add(script);
            return new ApplicationHook(application, scripts, folder);
        }

        public ApplicationHook removeScript(HookScript script) {
            final Collection<HookScript> scripts = new LinkedHashSet<>(this.scripts);
            scripts.remove(script);
            return new ApplicationHook(application, scripts, folder);
        }

        public ApplicationHook replaceScript(HookScript oldScript, HookScript newScript) {
            final List<HookScript> scripts = new ArrayList<>(this.scripts);
            scripts.set(scripts.indexOf(oldScript), newScript);
            return new ApplicationHook(application, scripts, folder);
        }

        public ApplicationHook withFolder(Path appHookFolder) {
            return new ApplicationHook(application, scripts, appHookFolder);
        }
    }

    public record Application(String process, String name, Path icon) {

        public Application withProcess(String process) {
            return new Application(process, name, icon);
        }

        public Application withName(String name) {
            return new Application(process, name, icon);
        }

        public Application withIcon(Path icon) {
            return new Application(process, name, icon);
        }
    }

    public record HookScript(String name, KeyStroke keyStroke, String script, Path scriptFile) {

        public HookScript(String name, KeyStroke keyStroke, String script) {
            this(name, keyStroke, script, null);
        }

        public HookScript withName(String name) {
            return new HookScript(name, keyStroke, script, scriptFile);
        }

        public HookScript withKeyStroke(KeyStroke keyStroke) {
            return new HookScript(name, keyStroke, script, scriptFile);
        }

        public HookScript withScript(String script) {
            return new HookScript(name, keyStroke, script, scriptFile);
        }

        public HookScript withScriptFile(Path scriptFile) {
            return new HookScript(name, keyStroke, script, scriptFile);
        }
    }

    public record KeyStroke(int keyCode, int modifiers, int toggleModifiersMask) {

        public boolean matches(int keyCode, int modifiers) {
            return keyCode() == keyCode && (modifiers() & toggleModifiersMask) == (modifiers & toggleModifiersMask);
        }

        public String getText() {
            return getText("+");
        }

        public String getText(String separator) {
            final StringBuilder sb = new StringBuilder();
            final String modifiersText;
            if(!(modifiersText = getModifiersText(separator)).isEmpty())
                sb.append(modifiersText).append(separator);
            sb.append(KeyEvent.getKeyText(keyCode()));
            return sb.toString();
        }

        public String getModifiersText() {
            return KeyboardHookEvent.getModifiersText(modifiers(), toggleModifiersMask());
        }

        public String getModifiersText(String separator) {
            return KeyboardHookEvent.getModifiersText(modifiers(), toggleModifiersMask(), separator);
        }

        public KeyStroke updateToggleModifiersMask(IntUnaryOperator toggleModifiersMaskUpdater) {
            return new KeyStroke(keyCode, modifiers, toggleModifiersMaskUpdater.applyAsInt(toggleModifiersMask));
        }
    }

    public enum FallbackBehavior { IGNORE, DELETE, DELETE_AND_PLAY_SOUND }
}
