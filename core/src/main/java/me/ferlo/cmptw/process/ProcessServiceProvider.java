package me.ferlo.cmptw.process;

public interface ProcessServiceProvider {

    boolean isSupported();

    ProcessService create();
}
