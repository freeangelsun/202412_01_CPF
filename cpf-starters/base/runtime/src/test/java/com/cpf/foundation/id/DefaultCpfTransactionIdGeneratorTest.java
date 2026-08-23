package com.cpf.foundation.id;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(first).hasSize(34).isEqualTo("20260815010203004MBRLOCAL010000001");
        assertThat(second).isEqualTo("20260815010203004MBRLOCAL010000002");
        assertThat(CpfTransactionIds.isCanonical(first)).isTrue();
        assertThat(CpfTransactionIds.issuerCode(first)).isEqualTo("MBR");
        assertThat(CpfTransactionIds.instanceToken(first)).isEqualTo("LOCAL01");
    }

    @Test
    void generateOrUseKeepsOnlyCanonicalIncomingTransactionId() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-15T01:02:03.004Z"), ZoneOffset.UTC);
        var generator = new DefaultCpfTransactionIdGenerator("EXS", "node001", clock);
        String canonical = "20260815010203004EXSNODE0010000123";

        assertThat(generator.generateOrUse(canonical)).isEqualTo(canonical);
        assertThat(generator.generateOrUse("external-correlation-id"))
                .isEqualTo("20260815010203004EXSNODE0010000001");
    }

    @Test
    void derivesCanonicalSevenCharacterTokenFromArbitraryRuntimeInstanceId() {
        var generator = new DefaultCpfTransactionIdGenerator("MBR", "mbr-was-host-01", Clock.systemUTC());
        assertThat(generator.getInstanceToken()).hasSize(7).matches("[A-Z0-9]{7}");
    }
}
