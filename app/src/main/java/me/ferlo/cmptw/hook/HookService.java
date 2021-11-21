package me.ferlo.cmptw.hook;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

public interface HookService {

    Collection<Hook> getSaved();

    void save(Collection<Hook> hooks) throws IOException;
}
