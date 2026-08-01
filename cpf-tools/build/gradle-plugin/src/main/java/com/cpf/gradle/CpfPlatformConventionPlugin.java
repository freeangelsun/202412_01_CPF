package com.cpf.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

/** CPF product modules에 동일한 Java 25 compile contract를 적용한다. */
public final class CpfPlatformConventionPlugin implements Plugin<Project> {
    @Override public void apply(Project project) {
        project.getPluginManager().apply(JavaPlugin.class);
        project.getExtensions().configure(JavaPluginExtension.class,
                java -> java.getToolchain().getLanguageVersion().set(JavaLanguageVersion.of(25)));
        project.getTasks().withType(JavaCompile.class).configureEach(task -> {
            task.getOptions().setEncoding("UTF-8");
            task.getOptions().getRelease().set(25);
            task.getOptions().getCompilerArgs().add("-parameters");
        });
    }
}
