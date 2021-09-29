package me.ferlo.cmptw.util;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

public class SameThreadExecutorService extends AbstractExecutorService {

    private volatile boolean terminated;

    @Override
    public void shutdown() {
        terminated = true;
    }

    @Override
    public boolean isShutdown() {
        return terminated;
    }

    @Override
    public boolean isTerminated() {
        return terminated;
    }

    @Override
    public boolean awaitTermination(long theTimeout, TimeUnit theUnit) {
        shutdown();
        return terminated;
    }

    @Override
    public List<Runnable> shutdownNow() {
        return Collections.emptyList();
    }

    @Override
    public void execute(Runnable theCommand) {
        theCommand.run();
    }
}
