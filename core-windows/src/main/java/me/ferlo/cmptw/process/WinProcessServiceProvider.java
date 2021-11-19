package me.ferlo.cmptw.process;

import com.google.auto.service.AutoService;

@AutoService(ProcessServiceProvider.class)
public class WinProcessServiceProvider implements ProcessServiceProvider {

    @Override
    public boolean isSupported() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    @Override
    public ProcessService create() {
        return new WinProcessService();
    }
}
