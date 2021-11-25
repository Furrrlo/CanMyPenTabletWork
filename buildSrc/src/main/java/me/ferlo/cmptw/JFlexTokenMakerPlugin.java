package me.ferlo.cmptw;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

import java.io.File;

public class JFlexTokenMakerPlugin extends JFlexPlugin {

    @Override
    public void apply(Project project) {
        super.apply(project);

        final var dstDirProvider = super.makeDestDir();
        final var fixTokenMakers = project.getTasks().register("fixTokenMakers", FixJFlexTokenMakerTask.class, task -> {
            task.dependsOn(jFlexTask);
            task.getSources().from(jFlexTask.map(JFlexTask::getDestination));
            task.getDestination().fileProvider(dstDirProvider);
        });

        getMainSourceSet().configure(ss -> project.getTasks().named(
                ss.getCompileJavaTaskName(),
                task -> task.dependsOn(fixTokenMakers)));
    }

    @Override
    protected Provider<File> makeDestDir() {
        return project.provider(() -> new File(project.getBuildDir(), "token-maker-jflex/"));
    }
}
