package com.github.furrrlo.cmptw.process;

import java.nio.file.Path;

public interface Process {

    int pid();

    String name();

    Path iconPath();
}
