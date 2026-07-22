package com.cpf.core.common.logging.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfLogPathPolicyTest {

    @TempDir
    Path tempDir;

    @Test
    void buildsEnvironmentModuleAndInstanceDirectoryFromAbsoluteRoot() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", tempDir.toString())
                .withProperty("cpf.environment", "dev")
                .withProperty("cpf.framework.module-id", "REF")
                .withProperty("CPF_INSTANCE_ID", "ref-api-01");

        CpfLogPathPolicy policy = new CpfLogPathPolicy(environment);

        assertThat(policy.instanceRoot())
                .isEqualTo(tempDir.resolve("dev/ref/ref-api-01"));
        assertThat(policy.transactionLogPath("MBR_MEMBER_FIND", LocalDate.of(2026, 7, 13)))
                .isEqualTo(tempDir.resolve(
                        "dev/ref/ref-api-01/transactions/20260713/MBR_MEMBER_FIND_20260713.log"));
    }

    @Test
    void findsRepositoryLogRootForLocalEnvironment() throws Exception {
        Files.writeString(tempDir.resolve("settings.gradle"), "rootProject.name = 'sample'");
        Files.writeString(tempDir.resolve("gradlew.bat"), "@echo off");
        Path nestedDirectory = Files.createDirectories(tempDir.resolve("cpf-reference/build/classes"));
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.environment", "local")
                .withProperty("cpf.framework.module-id", "REF");

        CpfLogPathPolicy policy = new CpfLogPathPolicy(environment, nestedDirectory);

        assertThat(policy.logRoot()).isEqualTo(tempDir.resolve("logs"));
    }

    @Test
    void rejectsRelativeRootAndMissingProductionInstance() {
        MockEnvironment relativeRoot = new MockEnvironment()
                .withProperty("cpf.environment", "local")
                .withProperty("cpf.framework.module-id", "REF")
                .withProperty("cpf.logging.file.base-path", "./logs");
        assertThatThrownBy(() -> new CpfLogPathPolicy(relativeRoot))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("절대경로");

        MockEnvironment missingInstance = new MockEnvironment()
                .withProperty("cpf.environment", "prod")
                .withProperty("cpf.framework.module-id", "REF")
                .withProperty("cpf.logging.file.base-path", tempDir.toString());
        assertThatThrownBy(() -> new CpfLogPathPolicy(missingInstance))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CPF_INSTANCE_ID");
    }

    @Test
    void rejectsTransactionPathTraversalToken() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", tempDir.toString())
                .withProperty("cpf.framework.module-id", "REF");
        CpfLogPathPolicy policy = new CpfLogPathPolicy(environment);

        assertThatThrownBy(() -> policy.transactionLogPath("..", LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
