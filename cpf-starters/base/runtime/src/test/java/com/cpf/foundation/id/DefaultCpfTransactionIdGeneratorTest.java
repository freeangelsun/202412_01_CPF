package com.cpf.foundation.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cpf.core.api.transaction.CpfTransactionIds;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class DefaultCpfTransactionIdGeneratorTest {

    @Test
    void generatesCanonical34CharacterTransactionIdAndIncrementsSequence() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-15T01:02:03.004Z"), ZoneOffset.UTC);
        var generator = new DefaultCpfTransactionIdGenerator("MBR", "local01", clock);

        String first = generator.newTransactionId();
        String second = generator.newTransactionId();

        assertThat(first).hasSize(34).isEqualTo("20260815010203004MBRlocal010000001");
        assertThat(second).isEqualTo("20260815010203004MBRlocal010000002");
        assertThat(CpfTransactionIds.isCanonical(first)).isTrue();
    }

    @Test
    void generateOrUseKeepsOnlyCanonicalIncomingTransactionId() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-15T01:02:03.004Z"), ZoneOffset.UTC);
        var generator = new DefaultCpfTransactionIdGenerator("EXS", "node001", clock);
        String canonical = "20260815010203004EXSnode0010000123";

        assertThat(generator.generateOrUse(canonical)).isEqualTo(canonical);
        assertThat(generator.generateOrUse("external-correlation-id"))
                .isEqualTo("20260815010203004EXSnode0010000001");
    }

    @Test
    void rejectsNonCanonicalWasId() {
        assertThatThrownBy(() -> new DefaultCpfTransactionIdGenerator(
                "MBR", "node-01", Clock.systemUTC()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
