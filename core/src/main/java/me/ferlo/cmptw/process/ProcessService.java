package me.ferlo.cmptw.process;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

public interface ProcessService {

    static ProcessService create() {
        final List<ProcessServiceProvider> services = ServiceLoader.load(ProcessServiceProvider.class).stream()
                .map(ServiceLoader.Provider::get)
                .filter(ProcessServiceProvider::isSupported)
                .toList();

        if(services.isEmpty())
            throw new IllegalStateException("Couldn't find any supported ProcessServiceProvider");
        if(services.size() > 1)
            throw new IllegalStateException("Multiple valid ProcessServiceProvider found, don't know which one to choose");
        return services.get(0).create();
    }

    Optional<Process> getProcessForPid(long pid);

    Collection<Process> enumerateProcesses();

    Collection<String> getProcessExtensions();

    List<BufferedImage> extractProcessIcons(Path processFile);

    List<BufferedImage> getFallbackIcons();

    Collection<String> getIconExtensions();
}
