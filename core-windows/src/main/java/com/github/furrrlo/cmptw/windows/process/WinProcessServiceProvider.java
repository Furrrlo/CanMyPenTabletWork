package com.github.furrrlo.cmptw.windows.process;

import com.google.auto.service.AutoService;
import com.github.furrrlo.cmptw.process.ProcessService;
import com.github.furrrlo.cmptw.process.ProcessServiceProvider;

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
