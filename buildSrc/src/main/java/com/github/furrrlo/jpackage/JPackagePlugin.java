package com.github.furrrlo.jpackage;

import org.beryx.jlink.JlinkPlugin;
import org.beryx.jlink.data.JlinkPluginExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;

import java.util.List;

public class JPackagePlugin implements Plugin<Project> {

    public static final String JPACKAGE_WIX_GEN_OVERRIDES = "jpackageWixGenOverrides";

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JlinkPlugin.class);

        final DirectoryProperty wixSrcDir = project.getObjects().directoryProperty().convention(
                project.getLayout().getProjectDirectory().dir("src/main/wix"));
        final var overridesTask = project.getTasks().register(JPACKAGE_WIX_GEN_OVERRIDES, WriteJPackageWixOverridesTask.class, task -> {
            task.getOverridesFile().set(wixSrcDir.file("overrides.wxi"));
            task.variable("JpxResourcesDir", wixSrcDir.map(it -> it.getAsFile().getAbsolutePath()));
        });

        project.getTasks().named("jpackage", task -> {
            task.getInputs().dir(wixSrcDir);
            task.dependsOn(overridesTask);
        });

        project.afterEvaluate(p -> p.getExtensions().configure(JlinkPluginExtension.class, ext -> ext.jpackage(jPackageData -> {
            boolean alreadyPresent = jPackageData.getInstallerOptions().stream()
                    .anyMatch(opt -> opt.equalsIgnoreCase("--resource-dir"));
            if(alreadyPresent)
                throw new UnsupportedOperationException("Cannot set jpackage resource-dir argument, it is set automatically");

            jPackageData.getInstallerOptions().addAll(List.of(
                    "--resource-dir",
                    wixSrcDir.get().getAsFile().getAbsolutePath()));
        })));
    }
}
