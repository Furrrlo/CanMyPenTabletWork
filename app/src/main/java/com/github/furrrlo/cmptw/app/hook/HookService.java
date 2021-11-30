package com.github.furrrlo.cmptw.app.hook;

import java.io.IOException;
import java.util.Collection;

public interface HookService {

    Collection<Hook> getSaved();

    void save(Collection<Hook> hooks) throws IOException;
}
