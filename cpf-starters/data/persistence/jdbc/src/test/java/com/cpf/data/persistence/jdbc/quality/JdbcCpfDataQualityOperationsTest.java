package com.cpf.data.persistence.jdbc.quality;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cpf.data.api.quality.CpfDataQualityOperations.ReplayCommand;
import com.cpf.data.api.quality.CpfDataQualityRule;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JdbcCpfDataQualityOperationsTest {
    @Test
    void evaluatesTheSameCanonicalRuleExpressionsAsTheReferenceProvider() {
        assertThat(JdbcCpfDataQualityOperations.matches(rule("NOT_BLANK"), "x")).isTrue();
        assertThat(JdbcCpfDataQualityOperations.matches(rule("MIN_LENGTH:3"), "abc")).isTrue();
        assertThat(JdbcCpfDataQualityOperations.matches(rule("MAX_LENGTH:3"), "abcd")).isFalse();
        assertThat(JdbcCpfDataQualityOperations.matches(rule("REGEX:[A-Z]{2}[0-9]{2}"), "AB12")).isTrue();
        assertThatThrownBy(() -> JdbcCpfDataQualityOperations.matches(rule("SCRIPT:unsafe"), "x"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void replayFingerprintBindsTargetVersionActorAndReason() {
        ReplayCommand base = new ReplayCommand("Q-1", 7L, "OP-1", "operator-a", "recover");
        String fingerprint = JdbcCpfDataQualityOperations.replayFingerprint(base);
        assertThat(fingerprint).hasSize(64);
        assertThat(JdbcCpfDataQualityOperations.replayFingerprint(new ReplayCommand("Q-1", 7L, "OP-2", "operator-a", "recover")))
                .isEqualTo(fingerprint); // operationId is the ledger key, not part of the immutable command payload.
        assertThat(JdbcCpfDataQualityOperations.replayFingerprint(new ReplayCommand("Q-1", 8L, "OP-1", "operator-a", "recover")))
                .isNotEqualTo(fingerprint);
        assertThat(JdbcCpfDataQualityOperations.replayFingerprint(new ReplayCommand("Q-1", 7L, "OP-1", "operator-b", "recover")))
                .isNotEqualTo(fingerprint);
        assertThat(JdbcCpfDataQualityOperations.replayFingerprint(new ReplayCommand("Q-1", 7L, "OP-1", "operator-a", "other")))
                .isNotEqualTo(fingerprint);
    }

    private static CpfDataQualityRule rule(String expression) {
        return new CpfDataQualityRule("R-1", 1L, "field", expression,
                CpfDataQualityRule.Severity.ERROR, CpfDataQualityRule.State.ACTIVE, Map.of());
    }
}
