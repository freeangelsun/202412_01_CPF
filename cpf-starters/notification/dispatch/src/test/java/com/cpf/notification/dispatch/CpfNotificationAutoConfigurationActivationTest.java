package com.cpf.notification.dispatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.cpf.foundation.execution.CpfContextExecutionFactory;
import com.cpf.notification.api.CpfNotificationOperations;
import com.cpf.notification.api.CpfNotificationRequest;
import com.cpf.notification.api.CpfNotificationResult;
import com.cpf.notification.spi.CpfNotificationProvider;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.JdbcTemplate;

class CpfNotificationAutoConfigurationActivationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CpfNotificationAutoConfiguration.class));

    @Test
    void classpathPresenceDoesNotActivateDisabledDispatch() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(CpfNotificationWorker.class);
            assertThat(context).doesNotHaveBean(CpfNotificationOperations.class);
        });
    }

    @Test
    void explicitActivationCreatesWorkerWithNamedProvider() {
        DataSource dataSource = mock(DataSource.class);
        contextRunner
                .withPropertyValues("cpf.notification.dispatch.enabled=true")
                .withBean(DataSource.class, () -> dataSource)
                .withBean("cpfJdbcTemplate", JdbcTemplate.class, () -> new JdbcTemplate(dataSource))
                .withBean(CpfContextExecutionFactory.class,
                        () -> mock(CpfContextExecutionFactory.class))
                .withBean(CpfNotificationProvider.class, TestProvider::new)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(CpfNotificationWorker.class);
                    assertThat(context).hasSingleBean(CpfNotificationOperations.class);
                });
    }

    private static final class TestProvider implements CpfNotificationProvider {
        @Override
        public String channel() {
            return "EMAIL";
        }

        @Override
        public CpfNotificationResult send(CpfNotificationRequest request) {
            throw new UnsupportedOperationException("Activation test does not dispatch messages");
        }
    }
}
