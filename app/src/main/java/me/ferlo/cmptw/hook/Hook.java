package me.ferlo.cmptw.hook;

import java.nio.file.Path;
import java.util.*;

public record Hook(Device device,
                   Collection<ApplicationHook> applicationHooks,
                   FallbackBehavior fallbackBehavior) {

    public Hook(Device device, Collection<ApplicationHook> applicationHooks, FallbackBehavior fallbackBehavior) {
        this.device = device;
        this.applicationHooks = Collections.unmodifiableCollection(new LinkedHashSet<>(applicationHooks));
        this.fallbackBehavior = fallbackBehavior;
    }

    public Hook withDevice(Device device) {
        return new Hook(device, applicationHooks, fallbackBehavior);
    }

    public Hook withApplicationHooks(Collection<ApplicationHook> applicationHooks) {
        return new Hook(device, applicationHooks, fallbackBehavior);
    }

    public Hook addApplicationHook(ApplicationHook applicationHook) {
        final Collection<ApplicationHook> hooks = new LinkedHashSet<>(this.applicationHooks);
        hooks.add(applicationHook);
        return new Hook(device, hooks, fallbackBehavior);
    }

    public Hook removeApplicationHook(ApplicationHook applicationHook) {
        final Collection<ApplicationHook> hooks = new LinkedHashSet<>(this.applicationHooks);
        hooks.remove(applicationHook);
        return new Hook(device, hooks, fallbackBehavior);
    }

    public Hook replaceApplicationHook(ApplicationHook oldApplicationHook, ApplicationHook newApplicationHook) {
        final List<ApplicationHook> hooks = new ArrayList<>(this.applicationHooks);
        hooks.set(hooks.indexOf(oldApplicationHook), newApplicationHook);
        return new Hook(device, hooks, fallbackBehavior);
    }

    public Hook withFallbackBehavior(FallbackBehavior fallbackBehavior) {
        return new Hook(device, applicationHooks, fallbackBehavior);
    }

    public record Device(String id, String name, String desc) {

        public Device withName(String name) {
            return new Device(id, name, desc);
        }

        public Device withDesc(String desc) {
            return new Device(id, name, desc);
        }
    }

    public record ApplicationHook(Application application, Collection<HookScript> scripts) {

        public ApplicationHook(Application application, Collection<HookScript> scripts) {
            this.application = application;
            this.scripts = Collections.unmodifiableCollection(new LinkedHashSet<>(scripts));
        }

        public ApplicationHook withApplication(Application application) {
            return new ApplicationHook(application, scripts);
        }

        public ApplicationHook withScripts(Collection<HookScript> scripts) {
            return new ApplicationHook(application, scripts);
        }

        public ApplicationHook addScript(HookScript script) {
            final Collection<HookScript> scripts = new LinkedHashSet<>(this.scripts);
            scripts.add(script);
            return new ApplicationHook(application, scripts);
        }

        public ApplicationHook removeScript(HookScript script) {
            final Collection<HookScript> scripts = new LinkedHashSet<>(this.scripts);
            scripts.remove(script);
            return new ApplicationHook(application, scripts);
        }

        public ApplicationHook replaceScript(HookScript oldScript, HookScript newScript) {
            final List<HookScript> scripts = new ArrayList<>(this.scripts);
            scripts.set(scripts.indexOf(oldScript), newScript);
            return new ApplicationHook(application, scripts);
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

    public record HookScript(String name, KeyStroke keyStroke, String script) {

        public HookScript withName(String name) {
            return new HookScript(name, keyStroke, script);
        }

        public HookScript withKeyStroke(KeyStroke keyStroke) {
            return new HookScript(name, keyStroke, script);
        }

        public HookScript withScript(String script) {
            return new HookScript(name, keyStroke, script);
        }
    }

    public record KeyStroke(int keyCode, int modifiers) {
    }

    public enum FallbackBehavior { IGNORE, DELETE, DELETE_AND_PLAY_SOUND }
}
