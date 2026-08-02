package com.cpf.starter.sftp;

import com.cpf.starter.secret.CpfSecretProviderRegistry;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.keyverifier.ServerKeyVerifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Assembles the production SFTP client with durable ledger, approved host-key verifier,
 * secret indirection and a non-invasive connectivity health probe.
 */
@AutoConfiguration
@EnableConfigurationProperties(CpfSftpProperties.class)
@ConditionalOnProperty(
        prefix = "cpf.integration.sftp",
        name = "enabled",
        havingValue = "true")
public class CpfSftpAutoConfiguration {

    @Bean
    JdbcCpfSftpTransferLedger cpfSftpTransferLedger(JdbcTemplate jdbcTemplate) {
        return new JdbcCpfSftpTransferLedger(jdbcTemplate);
    }

    @Bean(destroyMethod = "close")
    CpfSftpClient cpfSftpClient(
            CpfSftpProperties properties,
            ObjectProvider<ServerKeyVerifier> verifierProvider,
            JdbcCpfSftpTransferLedger ledger,
            CpfSecretProviderRegistry secrets) {
        properties.validate();
        ServerKeyVerifier verifier = verifierProvider.getIfUnique();
        if (verifier == null) {
            throw new IllegalStateException(
                    "Exactly one approved ServerKeyVerifier is required for SFTP");
        }
        if (verifier instanceof AcceptAllServerKeyVerifier) {
            throw new IllegalStateException("AcceptAllServerKeyVerifier is forbidden");
        }
        return new CpfSftpClient(properties, verifier, ledger, secrets);
    }

    @Bean("cpfSftpHealthIndicator")
    HealthIndicator cpfSftpHealthIndicator(
            CpfSftpClient client, CpfSftpProperties properties) {
        String maskedEndpoint = maskEndpoint(properties.getHost(), properties.getPort());
        return () -> {
            try {
                client.verifyConnection();
                return Health.up()
                        .withDetail("component", "sftp")
                        .withDetail("endpoint", maskedEndpoint)
                        .withDetail("reasonCode", "SFTP_AVAILABLE")
                        .build();
            } catch (RuntimeException exception) {
                return Health.down()
                        .withDetail("component", "sftp")
                        .withDetail("endpoint", maskedEndpoint)
                        .withDetail("reasonCode", "SFTP_CONNECTION_UNAVAILABLE")
                        .withDetail("exceptionType", exception.getClass().getSimpleName())
                        .build();
            }
        };
    }

    static String maskEndpoint(String host, int port) {
        if (host == null || host.isBlank()) {
            return "***:" + port;
        }
        String trimmed = host.trim();
        String masked = trimmed.length() == 1
                ? "*"
                : trimmed.charAt(0) + "***";
        return masked + ":" + port;
    }
}
