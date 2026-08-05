package com.cpf.core.common.logging;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.core.api.logging.CpfLogLevel;
import com.cpf.core.api.logging.DynamicLogLevelRule;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CpfTraceSamplingPolicyTest {

    @Test
    void appliesBusinessModuleFailureAndTemporaryBoostPrecedence() {
        CpfTraceSamplingPolicy policy = new CpfTraceSamplingPolicy();
        policy.replace(1L, 0.0d, Map.of("ADM", 1.0d), Map.of("PAY", 0.0d), true);

        assertThat(policy.shouldSample("t", "X", "ADM", true, null)).isTrue();
        assertThat(policy.shouldSample("t", "PAY", "ADM", true, null)).isFalse();
        assertThat(policy.shouldSample("t", "PAY", "ADM", false, null)).isTrue();

        LocalDateTime now = LocalDateTime.of(2026, 8, 5, 0, 0);
        DynamicLogLevelRule boost = new DynamicLogLevelRule(
                "rule-1",
                "tenant-a",
                "PAY",
                "ADM",
                CpfLogLevel.DEBUG,
                "incident investigation",
                "operator-hash",
                now,
                now.plusMinutes(1));
        assertThat(policy.shouldSample("t", "PAY", "ADM", true, boost)).isTrue();
    }

    @Test
    void producesStableDecisionForTheSameCorrelationInput() {
        CpfTraceSamplingPolicy policy = new CpfTraceSamplingPolicy();
        policy.replace(2L, 0.5d, Map.of(), Map.of(), true);

        boolean first = policy.shouldSample("stable-transaction", "B", "C", true, null);
        boolean second = policy.shouldSample("stable-transaction", "B", "C", true, null);

        assertThat(second).isEqualTo(first);
    }
}
