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

class CpfPlatformConventionPluginTest {
    @Test
    void appliesJava25Toolchain() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("com.cpf.platform-conventions");
        assertEquals(25, project.getExtensions().getByType(JavaPluginExtension.class)
                .getToolchain().getLanguageVersion().get().asInt());
    }

    @Test
    void canonicalPluginBuildsFreshNonDomainConsumer(@TempDir Path dir) throws IOException {
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
    void generatedDomainRejectsDirectKafka(@TempDir Path dir) throws IOException {
        generated(dir, "implementation 'org.springframework.kafka:spring-kafka:1.0.0'\n", "");
        BuildResult result = runner(dir)
                .withArguments("verifyCpfGeneratedDomainPolicy", "--stacktrace").buildAndFail();
        assertTrue(result.getOutput().contains("DIRECT_KAFKA"));
    }

    @Test
    void generatedDomainRejectsDirectTemplateImport(@TempDir Path dir) throws IOException {
        generated(dir, "", "import org.springframework.kafka.core.KafkaTemplate;\n");
        BuildResult result = runner(dir)
                .withArguments("verifyCpfGeneratedDomainPolicy", "--stacktrace").buildAndFail();
        assertTrue(result.getOutput().contains("IMPORT_KAFKA_TEMPLATE"));
    }



    @Test
    void generatedDomainRejectsWildcardTemplateUsage(@TempDir Path dir) throws IOException {
        generated(dir, "", "import org.springframework.kafka.core.*;\nprivate KafkaTemplate<String,String> template;\n");
        BuildResult result = runner(dir)
                .withArguments("verifyCpfGeneratedDomainPolicy", "--stacktrace").buildAndFail();
        assertTrue(result.getOutput().contains("IMPORT_KAFKA_TEMPLATE"));
    }

    @Test
    void generatedDomainRejectsDirectDataSourceBean(@TempDir Path dir) throws IOException {
        generated(dir, "", "import javax.sql.DataSource;\nimport org.springframework.context.annotation.Bean;\n@Bean DataSource customDataSource(){ return null; }\n");
        BuildResult result = runner(dir)
                .withArguments("verifyCpfGeneratedDomainPolicy", "--stacktrace").buildAndFail();
        assertTrue(result.getOutput().contains("DIRECT_DATASOURCE_BEAN"));
    }

    @Test
    void generatedDomainRejectsDirectMyBatisProvider(@TempDir Path dir) throws IOException {
        generated(dir, "implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:4.0.0'\n", "");
        BuildResult result = runner(dir)
                .withArguments("verifyCpfGeneratedDomainPolicy", "--stacktrace").buildAndFail();
        assertTrue(result.getOutput().contains("DIRECT_MYBATIS"));
    }

    @Test
    void generatedDomainRejectsDirectKafkaConfiguration(@TempDir Path dir) throws IOException {
        generated(dir, "", "");
        Path config = dir.resolve("src/main/resources/application.yml");
        Files.createDirectories(config.getParent());
        Files.writeString(config, "spring:\n  kafka:\n    bootstrap-servers: localhost:9092\n");
        BuildResult result = runner(dir)
                .withArguments("verifyCpfGeneratedDomainPolicy", "--stacktrace").buildAndFail();
        assertTrue(result.getOutput().contains("CONFIG_KAFKA_BYPASS"));
    }

    @Test
    void generatedDomainRejectsInternalStarterImplementationImport(@TempDir Path dir) throws IOException {
        generated(dir, "", "import com.cpf.starter.kafka.CpfKafkaBindingAutoConfiguration;\n");
        BuildResult result = runner(dir)
                .withArguments("verifyCpfGeneratedDomainPolicy", "--stacktrace").buildAndFail();
        assertTrue(result.getOutput().contains("INTERNAL_PROVIDER_IMPORT"));
    }

    private static GradleRunner runner(Path dir) {
        return GradleRunner.create().withProjectDir(dir.toFile()).withPluginClasspath();
    }

    private static void generated(Path dir, String dependency, String sourceImport) throws IOException {
        Files.writeString(dir.resolve("settings.gradle"), "rootProject.name='cpf-sample'\n");
        Files.writeString(dir.resolve("build.gradle"), """
                plugins { id 'com.cpf.platform-conventions' }
                dependencies {
                    implementation 'com.cpf.starter:cpf-starter-profile-minimal-domain:1.0.0'
                    %s
                }
                """.formatted(dependency));
        Path manifest = dir.resolve("manifest");
        Files.createDirectories(manifest);
        Files.writeString(manifest.resolve("domain-manifest.json"),
                "{\"projectName\":\"cpf-sample\",\"domainName\":\"sample\"}\n");
        Files.writeString(manifest.resolve("resolved-starter-lock.json"), """
                {"profile":"minimal-domain","capabilityGroups":[],"providerBindings":{},
                 "resolvedStarterVersions":{},"exceptionRegistrySha256":"placeholder",
                 "approvedExceptions":[]}
                """);
        Path config = dir.resolve("config");
        Path resources = dir.resolve("src/main/resources/META-INF/cpf");
        Files.createDirectories(config);
        Files.createDirectories(resources);
        String registry = String.join(",", CpfGeneratedDomainPolicySupport.EXCEPTION_FIELDS) + "\n";
        Files.writeString(config.resolve("cpf-approved-exceptions.csv"), registry);
        Files.writeString(resources.resolve("cpf-approved-exceptions.csv"), registry);
        String sha = CpfGeneratedDomainPolicySupport.sha256(registry.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        Files.writeString(resources.resolve("generated-domain-policy.properties"),
                "policyVersion=1.0\nmodule=cpf-sample\nprofile=minimal-domain\n"
                        + "capabilities=\nrequiredStandards="
                        + String.join(",", CpfGeneratedDomainPolicySupport.REQUIRED_STANDARDS)
                        + "\napprovedExceptionIds=\nexceptionRegistrySha256=" + sha + "\nfailClosed=true\n");
        String lock = Files.readString(manifest.resolve("resolved-starter-lock.json"))
                .replace("placeholder", sha);
        Files.writeString(manifest.resolve("resolved-starter-lock.json"), lock);
        Path java = dir.resolve("src/main/java/example/Consumer.java");
        Files.createDirectories(java.getParent());
        Files.writeString(java, "package example;\n" + sourceImport + "public final class Consumer {}\n");
    }
}
