package com.cpf.starter.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class CpfNotificationPropertiesTest {
    @Test
    void appliesBoundedDefaults() {
        var properties = new CpfNotificationProperties(true, " ", 0, null, null, null);
        assertThat(properties.workerId()).isEqualTo("cpf-notification-worker");
        assertThat(properties.batchSize()).isEqualTo(100);
        assertThat(properties.leaseDuration()).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void rejectsNonPositiveLease() {
        assertThatThrownBy(() -> new CpfNotificationProperties(
                true, "w", 1, Duration.ZERO, Duration.ofSeconds(1), Duration.ofSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
