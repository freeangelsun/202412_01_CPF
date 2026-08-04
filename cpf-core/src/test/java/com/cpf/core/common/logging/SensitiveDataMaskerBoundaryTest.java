package com.cpf.core.common.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SensitiveDataMaskerBoundaryTest {

    @Test
    void truncateNormalizesNegativeAndTooSmallLimits() {
        String payload = "x".repeat(300);

        assertThat(SensitiveDataMasker.truncate(payload, -1))
                .isEqualTo("x".repeat(256) + "...(truncated)");
        assertThat(SensitiveDataMasker.truncate(payload, 1))
                .isEqualTo("x".repeat(256) + "...(truncated)");
    }

    @Test
    void truncateKeepsNullAndShortValues() {
        assertThat(SensitiveDataMasker.truncate(null, -1)).isNull();
        assertThat(SensitiveDataMasker.truncate("safe", -1)).isEqualTo("safe");
    }
    @Test
    void bearerTokenIsFullyRemovedBeforeAuthorizationKeyMasking() {
        String masked = SensitiveDataMasker.mask("Authorization: Bearer abc.def-123");

        assertThat(masked).doesNotContain("abc.def-123");
        assertThat(masked).contains("***");
    }

}
