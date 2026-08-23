package com.cpf.starter.logging;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.common.logging.CpfApplicationLoggingPolicy;
import com.cpf.starter.logging.internal.CpfRuntimeLoggingLifecycle;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class CpfRuntimeLoggingAutoConfigurationTest {
    @TempDir Path temp;

    @Test
    void bindsVisibleMultiFilePolicyAndWritesRuntimeAndErrorFiles() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(CpfRuntimeLoggingAutoConfiguration.class))
                .withPropertyValues(
                        "spring.application.name=demo-app",
                        "cpf.logging.root=" + temp,
                        "cpf.logging.instance-id=test-1",
                        "cpf.logging.maintenance-interval=1h",
                        "cpf.logging.files.runtime.enabled=true",
                        "cpf.logging.files.runtime.file-name=runtime.log",
                        "cpf.logging.files.runtime.rolling=DAILY",
                        "cpf.logging.files.runtime.compress-after-days=5",
                        "cpf.logging.files.runtime.delete-after-days=365",
                        "cpf.logging.files.error.enabled=true",
                        "cpf.logging.files.error.file-name=error.log",
                        "cpf.logging.files.error.level=ERROR",
                        "cpf.logging.files.error.rolling=DAILY",
                        "cpf.logging.files.error.compress-after-days=5",
                        "cpf.logging.files.error.delete-after-days=365")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(CpfApplicationLoggingPolicy.class);
                    assertThat(context).hasSingleBean(CpfRuntimeLoggingLifecycle.class);
                    var logger = LoggerFactory.getLogger("cpf.logging.runtime-test");
                    logger.info("visible runtime message");
                    logger.error("visible error message");
                    Path directory = temp.resolve("demo-app/test-1");
                    assertThat(directory.resolve("runtime.log")).exists();
                    assertThat(directory.resolve("error.log")).exists();
                    assertThat(Files.readString(directory.resolve("runtime.log")))
                            .contains("visible runtime message").contains("visible error message");
                    assertThat(Files.readString(directory.resolve("error.log")))
                            .doesNotContain("visible runtime message").contains("visible error message");
                    var loggerContext = (ch.qos.logback.classic.LoggerContext) LoggerFactory.getILoggerFactory();
                    assertThat(loggerContext.getStatusManager().getCopyOfStatusList())
                            .noneMatch(status -> status.getMessage() != null
                                    && status.getMessage().contains("triggering policy of type TimeBasedRollingPolicy was already set"));
                });
    }

    @Test
    void failsWithActionablePropertyWhenFilesAreMissing() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(CpfRuntimeLoggingAutoConfiguration.class))
                .withPropertyValues(
                        "spring.application.name=demo-app",
                        "cpf.logging.root=" + temp,
                        "cpf.logging.instance-id=test-2")
                .run(context -> assertThat(context).hasFailed()
                        .getFailure().hasMessageContaining("cpf.logging.files"));
    }
}
