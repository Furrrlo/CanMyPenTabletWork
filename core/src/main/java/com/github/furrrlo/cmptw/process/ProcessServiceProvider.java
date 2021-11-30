package com.github.furrrlo.cmptw.process;

public interface ProcessServiceProvider {

    boolean isSupported();

    ProcessService create();
}
