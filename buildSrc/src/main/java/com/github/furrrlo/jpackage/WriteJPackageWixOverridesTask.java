package com.github.furrrlo.jpackage;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class WriteJPackageWixOverridesTask extends DefaultTask {

    private final MapProperty<String, String> variables;
    private final RegularFileProperty overridesFile;
    private final ListProperty<String> additionalTextFragments;

    public WriteJPackageWixOverridesTask() {
        this.variables = getProject().getObjects().mapProperty(String.class, String.class);
        this.overridesFile = getProject().getObjects().fileProperty();
        this.additionalTextFragments = getProject().getObjects().listProperty(String.class);
    }

    @TaskAction
    protected void generate() throws IOException {
        final Path file = overridesFile.getAsFile().get().toPath();
        final Map<String, String> variables = this.variables.get();
        final List<String> additionalTextFragments = this.additionalTextFragments.get();

        try(BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            bw.write("""
                    <?xml version="1.0" encoding="utf-8"?>
                    <!-- Stub by design -->
                                        
                    <!--
                    overrides.wxi is a placeholder to set/alter WiX variables referenced from default
                    main.wxs file.
                    Put custom overrides.wxi in resource directory to replace this default file.
                    Override default overrides.wxi if configuring of msi installers through jpackage
                    command line is not sufficient.
                    WiX variables referenced from default main.wxs that can be altered in custom overrides.wxi:
                    - JpProductLanguage
                    Value of `Language` attribute of `Product` WiX element. Default value is 1033.
                    - JpInstallerVersion
                    Value of `InstallerVersion` attribute of `Package` WiX element. Default value is 200.
                    - JpAllowDowngrades
                    Should be defined to enable downgrades and undefined to disable downgrades.
                    By default it is defined for applications and undefined for Runtime installer.
                    Use <?define JpAllowUpgrades = "foo" ?> to enable or <?undef JpAllowUpgrades?>
                    to disable (the value doesn't mater).
                    - JpAllowUpgrades
                    Should be defined to enable upgrades and undefined to disable upgrades.
                    By default it is defined, use <?undef JpAllowUpgrades?> to disable.
                    -->
                    <Include>
                    """);

            for (Map.Entry<String, String> entry : variables.entrySet()) {
                bw.write("<?define " + entry.getKey() + " = \"" + entry.getValue().replace("\"", "&quot;") + "\" ?>");
                bw.newLine();
            }

            for (String textFragment : additionalTextFragments) {
                bw.write(textFragment);
                bw.newLine();
            }

            bw.write("</Include>");
            bw.newLine();
        }
    }

    @Input
    public MapProperty<String, String> getVariables() {
        return variables;
    }

    public void variable(String var, String value) {
        getVariables().put(var, value);
    }

    public void variable(String var, Provider<String> value) {
        getVariables().put(var, value);
    }

    @Input
    public ListProperty<String> getAdditionalTextFragments() {
        return additionalTextFragments;
    }

    public void text(String text) {
        getAdditionalTextFragments().add(text);
    }

    public void text(Provider<String> text) {
        getAdditionalTextFragments().add(text);
    }

    @OutputFile
    public RegularFileProperty getOverridesFile() {
        return overridesFile;
    }
}
