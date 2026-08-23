package com.cpf.notification.dispatch;

import com.cpf.notification.api.CpfNotificationProviderStatus;
import com.cpf.notification.api.CpfNotificationOperations;
import com.cpf.notification.dispatch.internal.CpfJdbcNotificationOperations;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import com.cpf.notification.spi.CpfNotificationProvider;

import java.sql.Connection;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration
@ConditionalOnProperty(
        prefix = "cpf.notification.dispatch",
        name = "enabled",
        havingValue = "true")
@EnableConfigurationProperties(CpfNotificationProperties.class)
public class CpfNotificationAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "cpfNotificationClock")
    Clock cpfNotificationClock() {
        return Clock.systemUTC();
    }

    @Bean
    CpfNotificationContextCodec cpfNotificationContextCodec(CpfContextExecutionFactory factory) { return new CpfNotificationContextCodec(factory); }

    @Bean
    JdbcCpfNotificationOutbox cpfNotificationOutbox(
            @Qualifier("cpfJdbcTemplate") JdbcTemplate jdbcTemplate,
            @Qualifier("cpfNotificationClock") Clock clock,
            CpfNotificationContextCodec contextCodec) {
        return new JdbcCpfNotificationOutbox(jdbcTemplate, clock, contextCodec);
    }

    @Bean
    CpfNotificationPreferencePolicy cpfNotificationPreferencePolicy() {
        return new CpfNotificationPreferencePolicy();
    }

    @Bean
    CpfNotificationWorker cpfNotificationWorker(
            JdbcCpfNotificationOutbox outbox,
            ObjectProvider<CpfNotificationProvider> providers,
            CpfNotificationPreferencePolicy preferencePolicy,
            CpfNotificationProperties properties,
            @Qualifier("cpfNotificationClock") Clock clock,
            CpfNotificationContextCodec contextCodec) {
        return new CpfNotificationWorker(
                outbox, providers.orderedStream().toList(), preferencePolicy, properties, clock, contextCodec);
    }

    @Bean
    CpfNotificationOperations cpfNotificationOperations(
            JdbcCpfNotificationOutbox outbox,
            @Qualifier("cpfNotificationClock") Clock clock) {
        return new CpfJdbcNotificationOperations(outbox, clock);
    }

    @Bean("cpfNotificationHealthIndicator")
    HealthIndicator cpfNotificationHealthIndicator(
            DataSource dataSource,
            ObjectProvider<CpfNotificationProvider> providers) {
        return () -> health(dataSource, providers.orderedStream().toList());
    }

    private static Health health(
            DataSource dataSource, List<CpfNotificationProvider> providers) {
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(3)) {
                return Health.down()
                        .withDetail("reasonCode", "NOTIFICATION_DB_INVALID")
                        .build();
            }
        } catch (Exception exception) {
            return Health.down()
                    .withDetail("reasonCode", "NOTIFICATION_DB_UNAVAILABLE")
                    .build();
        }

        Map<String, String> providerStates = new LinkedHashMap<>();
        boolean down = false;
        boolean degraded = false;
        for (CpfNotificationProvider provider : providers) {
            CpfNotificationProviderStatus status;
            try {
                status = provider.health();
            } catch (RuntimeException exception) {
                status = CpfNotificationProviderStatus.down("HEALTH_CHECK_FAILED");
            }
            providerStates.put(
                    provider.channel(), status.status() + ":" + status.reasonCode());
            down |= "DOWN".equals(status.status());
            degraded |= "DEGRADED".equals(status.status())
                    || "UNKNOWN".equals(status.status());
        }
        Health.Builder builder = down ? Health.down()
                : degraded ? Health.status("DEGRADED") : Health.up();
        return builder
                .withDetail("providerCount", providers.size())
                .withDetail("providers", Map.copyOf(providerStates))
                .build();
    }
}
