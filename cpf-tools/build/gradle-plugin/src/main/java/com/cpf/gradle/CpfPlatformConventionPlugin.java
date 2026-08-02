package com.cpf.gradle;

import java.io.File;
import java.util.Map;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

/** CPF product convention and Generated Domain standard inheritance. */
public final class CpfPlatformConventionPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(JavaPlugin.class);
        project.getExtensions().configure(JavaPluginExtension.class,
                java -> java.getToolchain().getLanguageVersion().set(JavaLanguageVersion.of(25)));
        project.getTasks().withType(JavaCompile.class).configureEach(task -> {
            task.getOptions().setEncoding("UTF-8");
            task.getOptions().getRelease().set(25);
            task.getOptions().getCompilerArgs().add("-parameters");
        });
        project.getConfigurations().configureEach(configuration ->
                configuration.getResolutionStrategy().failOnVersionConflict());

        File manifest = project.file(CpfGeneratedDomainPolicySupport.MANIFEST_PATH);
        if (!manifest.isFile()) {
            return;
        }
        inheritGeneratedDomainFoundation(project);
        TaskProvider<CpfGeneratedDomainPolicyTask> policy = project.getTasks().register(
                "verifyCpfGeneratedDomainPolicy", CpfGeneratedDomainPolicyTask.class,
                task -> {
                    task.setGroup("verification");
                    task.setDescription("Verifies CPF Generated Domain inheritance and approved override policy.");
                });
        project.getTasks().named("check").configure(task -> task.dependsOn(policy));
    }

    private static void inheritGeneratedDomainFoundation(Project project) {
        DependencyHandler dependencies = project.getDependencies();
        Project core = project.getRootProject().findProject(":cpf-core");
        Project common = project.getRootProject().findProject(":cpf-common");
        if (core != null && common != null) {
            dependencies.add("implementation", dependencies.project(Map.of("path", ":cpf-core")));
            dependencies.add("implementation", dependencies.project(Map.of("path", ":cpf-common")));
            return;
        }
        Object versionValue = project.findProperty("cpfPlatformVersion");
        if (versionValue == null || versionValue.toString().isBlank()) {
            throw new IllegalStateException(
                    "cpfPlatformVersion is required for a published-artifact Generated Domain.");
        }
        String version = versionValue.toString().trim();
        dependencies.add("implementation", "com.cpf.core:cpf-core:" + version);
        dependencies.add("implementation", "com.cpf.common:cpf-common:" + version);
    }
}
