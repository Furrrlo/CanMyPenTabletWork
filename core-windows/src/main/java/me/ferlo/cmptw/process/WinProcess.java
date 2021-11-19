package me.ferlo.cmptw.process;

import java.nio.file.Path;
import java.util.Objects;

class WinProcess implements Process {

    private final int id;
    private final String name;
    private final Path fileName;

    public WinProcess(int id, String name, Path fileName) {
        this.id = id;
        this.name = name;
        this.fileName = fileName;
    }

    @Override
    public int pid() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Path iconPath() {
        return fileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WinProcess that)) return false;
        return id == that.id && Objects.equals(name, that.name) && Objects.equals(fileName, that.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, fileName);
    }

    @Override
    public String toString() {
        return "WinProcess{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
