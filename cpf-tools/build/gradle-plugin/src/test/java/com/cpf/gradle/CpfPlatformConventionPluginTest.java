package com.cpf.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** CPF Platform Convention이 최소 Build 책임만 수행하는지 검증한다. */
class CpfPlatformConventionPluginTest {
    @Test
    void appliesJava25Toolchain() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("com.cpf.platform-conventions");

        assertEquals(25, project.getExtensions().getByType(JavaPluginExtension.class)
                .getToolchain().getLanguageVersion().get().asInt());
    }

    @Test
    void buildsFreshConsumerWithoutGeneratedDomainMetadata(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("settings.gradle"), "rootProject.name='consumer'\n");
        Files.writeString(dir.resolve("build.gradle"),
                "plugins { id 'com.cpf.platform-conventions' }\n");
        Path java = dir.resolve("src/main/java/example/Consumer.java");
        Files.createDirectories(java.getParent());
        Files.writeString(java, "package example; public final class Consumer {}\n");

        BuildResult result = runner(dir).withArguments("compileJava", "--stacktrace").build();
        TaskOutcome outcome = result.task(":compileJava").getOutcome();
        assertTrue(outcome == TaskOutcome.SUCCESS || outcome == TaskOutcome.UP_TO_DATE);
    }

    @Test
    void doesNotInjectLegacyCommonOrCoreDependency() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("com.cpf.platform-conventions");

        String dependencies = project.getConfigurations().getByName("implementation")
                .getDependencies().toString();
        assertFalse(dependencies.contains("cpf-common"));
        assertFalse(dependencies.contains("cpf-core"));
    }

    private static GradleRunner runner(Path dir) {
        return GradleRunner.create().withProjectDir(dir.toFile()).withPluginClasspath();
    }
}
