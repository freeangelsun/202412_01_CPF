package com.cpf.starter.kafka;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.messaging.kafka")
public record CpfKafkaProperties(Duration acknowledgementTimeout, int maximumPayloadBytes, boolean requireIdempotence) {
    public CpfKafkaProperties {
        acknowledgementTimeout = acknowledgementTimeout == null ? Duration.ofSeconds(10) : acknowledgementTimeout;
        maximumPayloadBytes = maximumPayloadBytes <= 0 ? 1_048_576 : maximumPayloadBytes;
        if (acknowledgementTimeout.isNegative() || acknowledgementTimeout.isZero()) throw new IllegalArgumentException("Kafka acknowledgement timeout must be positive.");
        if (maximumPayloadBytes > 16_777_216) throw new IllegalArgumentException("Kafka payload limit is too large.");
    }
}
