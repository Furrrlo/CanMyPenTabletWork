package me.ferlo.cmptw.hook;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class InMemoryHookService implements HookService {

    private final Set<Hook> saved = new LinkedHashSet<>();
    private final Collection<Hook> unmodifiableSaved = Collections.unmodifiableSet(saved);

    @Override
    public Collection<Hook> getSaved() {
        return unmodifiableSaved;
    }

    @Override
    public void save(Collection<Hook> hooks) {
        synchronized (saved) {
            saved.clear();
            saved.addAll(hooks);
        }
    }
}
