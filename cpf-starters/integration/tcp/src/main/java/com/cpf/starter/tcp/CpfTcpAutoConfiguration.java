package com.cpf.starter.tcp;

import java.nio.file.Path;
import java.util.function.Function;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(CpfTcpProperties.class)
@ConditionalOnProperty(prefix = "cpf.integration.tcp", name = "enabled", havingValue = "true")
public class CpfTcpAutoConfiguration {
    @Bean
    CpfTcpUnknownResultStore cpfTcpUnknownResultStore(CpfTcpProperties properties) {
        properties.validate();
        return new CpfTcpUnknownResultStore(
                properties.getMaxUnknownResults(),
                Path.of(properties.getUnknownResultJournal()));
    }

    @Bean
    CpfTcpCorrelationRegistry cpfTcpCorrelationRegistry(CpfTcpProperties properties) {
        return new CpfTcpCorrelationRegistry(properties.getMaxPending(), properties.getMaxOrphans());
    }

    @Bean
    CpfTcpReconnectPolicy cpfTcpReconnectPolicy(CpfTcpProperties properties) {
        return new CpfTcpReconnectPolicy(
                properties.getReconnectInitial(),
                properties.getReconnectMax(),
                properties.getReconnectJitter());
    }

    @Bean
    @ConditionalOnProperty(prefix = "cpf.integration.tcp", name = "tls", havingValue = "true")
    CpfTcpTlsContextProvider cpfTcpTlsContextProvider(CpfTcpProperties properties) {
        properties.validate();
        return new KeyStoreCpfTcpTlsContextProvider(
                Path.of(properties.getKeyStore()),
                properties.getKeyStorePassword().toCharArray(),
                Path.of(properties.getTrustStore()),
                properties.getTrustStorePassword().toCharArray());
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "cpf.integration.tcp", name = "mode", havingValue = "CLIENT", matchIfMissing = true)
    CpfTcpClient cpfTcpClient(
            CpfTcpProperties properties,
            CpfTcpUnknownResultStore store,
            org.springframework.beans.factory.ObjectProvider<CpfTcpTlsContextProvider> tls) {
        properties.validate();
        return new CpfTcpClient(properties, store, tls.getIfAvailable());
    }

    @Bean(initMethod = "start", destroyMethod = "close")
    @ConditionalOnProperty(prefix = "cpf.integration.tcp", name = "mode", havingValue = "SERVER")
    CpfTcpServer cpfTcpServer(
            CpfTcpProperties properties,
            org.springframework.beans.factory.ObjectProvider<Function<byte[], byte[]>> handlerProvider,
            org.springframework.beans.factory.ObjectProvider<CpfTcpTlsContextProvider> tls) {
        properties.validate();
        Function<byte[], byte[]> handler = handlerProvider.getIfAvailable();
        if (handler == null) {
            throw new IllegalStateException("TCP SERVER mode requires Function<byte[],byte[]> handler");
        }
        return new CpfTcpServer(properties, handler, tls.getIfAvailable());
    }

    @Bean
    CpfTcpOperations cpfTcpOperations(
            CpfTcpUnknownResultStore unknown,
            CpfTcpCorrelationRegistry correlations) {
        return new CpfTcpOperations(unknown, correlations);
    }

    @Bean("cpfTcpHealthIndicator")
    HealthIndicator health(CpfTcpProperties properties, CpfTcpOperations operations) {
        return () -> Health.up()
                .withDetail("mode", properties.getMode())
                .withDetail("frame", properties.getFrame())
                .withDetail("tls", properties.isTls())
                .withDetail("operations", operations.snapshot())
                .build();
    }
}
