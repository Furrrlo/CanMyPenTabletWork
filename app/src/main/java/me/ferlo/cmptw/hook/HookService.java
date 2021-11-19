package me.ferlo.cmptw.hook;

import java.util.Collection;

public interface HookService {

    Collection<Hook> getSaved();

    void save(Collection<Hook> hooks);
}
