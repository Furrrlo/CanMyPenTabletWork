package com.github.furrrlo.jflex;

import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;

public class JFlexPlugin implements Plugin<Project> {

    protected static final String JFLEX_TASK_NAME = "jflex";

    protected Project project;
    protected TaskProvider<JFlexTask> jFlexTask;

    @Override
    public void apply(Project project) {
        this.project = project;

        project.getPlugins().apply(JavaBasePlugin.class);

        final var mainSourceSet = getMainSourceSet();
        final var configuration = project.getConfigurations().register("jflex");

        final var dstDirProvider = makeDestDir();
        jFlexTask = project.getTasks().register(JFLEX_TASK_NAME, JFlexTask.class, task -> {
            task.getSources().from(mainSourceSet.map(ss -> ss.getAllJava().getSourceDirectories()));
            task.getDestination().fileProvider(dstDirProvider);
            task.getExecClasspath().from(configuration);
            task.getNoBackup().convention(true);
        });

        mainSourceSet.configure(ss -> project.getTasks().named(
                ss.getCompileJavaTaskName(),
                task -> task.dependsOn(jFlexTask)));
    }

    protected NamedDomainObjectProvider<SourceSet> getMainSourceSet() {
        return project.getExtensions().getByType(SourceSetContainer.class).named(SourceSet.MAIN_SOURCE_SET_NAME);
    }

    protected Provider<File> makeDestDir() {
        final var mainSourceSet = getMainSourceSet();
        final var provider = mainSourceSet.map(ss -> new File(
                project.getBuildDir(),
                "generated/sources/jflex/" + ss.getJava().getName() + "/" + mainSourceSet.getName() + "/"));
        mainSourceSet.configure(ss -> ss.getJava().srcDir(provider));
        return provider;
    }
}
