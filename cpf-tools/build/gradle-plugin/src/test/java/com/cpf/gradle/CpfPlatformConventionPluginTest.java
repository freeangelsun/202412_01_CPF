package com.cpf.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

/** Canonical Plugin ID, Java 25 contract and fresh consumer resolution을 검증한다. */
class CpfPlatformConventionPluginTest {

    @Test
    void appliesJava25Toolchain() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("com.cpf.platform-conventions");
        assertEquals(25, project.getExtensions().getByType(JavaPluginExtension.class)
                .getToolchain().getLanguageVersion().get().asInt());
    }

    @Test
    void canonicalPluginBuildsFreshGeneratedConsumer(@TempDir Path projectDir) throws IOException {
        Files.writeString(projectDir.resolve("settings.gradle"), "rootProject.name='generated-consumer'\n");
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins { id 'com.cpf.platform-conventions' }
                tasks.register('assertCpfContract') {
                    doLast {
                        assert java.toolchain.languageVersion.get().asInt() == 25
                        assert tasks.named('compileJava').get().options.release.get() == 25
                    }
                }
                """);
        Path java = projectDir.resolve("src/main/java/example/GeneratedConsumer.java");
        Files.createDirectories(java.getParent());
        Files.writeString(java, "package example; public final class GeneratedConsumer {}\n");

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withArguments("assertCpfContract", "compileJava", "--stacktrace")
                .build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":assertCpfContract").getOutcome());
        assertTrue(result.task(":compileJava").getOutcome() == TaskOutcome.SUCCESS
                || result.task(":compileJava").getOutcome() == TaskOutcome.UP_TO_DATE);
    }

    @Test
    void legacyPluginIdIsNotPublishedAsSecondPrimary(@TempDir Path projectDir) throws IOException {
        Files.writeString(projectDir.resolve("settings.gradle"), "rootProject.name='legacy-consumer'\n");
        Files.writeString(projectDir.resolve("build.gradle"), "plugins { id 'com.cpf.domain-conventions' }\n");

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withArguments("help", "--stacktrace")
                .buildAndFail();

        assertTrue(result.getOutput().contains("com.cpf.domain-conventions"));
    }
}
