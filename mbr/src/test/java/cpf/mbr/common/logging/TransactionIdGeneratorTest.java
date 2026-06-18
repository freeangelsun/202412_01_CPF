package cpf.mbr.common.logging;

import cpf.pfw.common.logging.TransactionIdGenerator;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
class TransactionIdGeneratorTest {

    /**
     * CPF 기능 설명입니다.
     */
    @Test
    void generatesTransactionIdWithTimestampModuleWasAndSequence() {
        // CPF 기능 설명입니다.
        Clock clock = Clock.fixed(
                Instant.parse("2026-06-11T05:12:34.567Z"),
                ZoneId.of("Asia/Seoul"));
        // CPF 기능 설명입니다.
        TransactionIdGenerator generator = new TransactionIdGenerator("ACC", "accAP01", 7, clock);

        // CPF 기능 설명입니다.
        assertThat(generator.generate()).isEqualTo("20260611141234567ACCaccAP010000001");
        // CPF 기능 설명입니다.
        assertThat(generator.generate()).isEqualTo("20260611141234567ACCaccAP010000002");
    }

    /**
     * CPF 기능 설명입니다.
     */
    @Test
    void reusesOnlyValidIncomingTransactionId() {
        // CPF 기능 설명입니다.
        Clock clock = Clock.fixed(
                Instant.parse("2026-06-11T05:12:34.567Z"),
                ZoneId.of("Asia/Seoul"));
        // CPF 기능 설명입니다.
        TransactionIdGenerator generator = new TransactionIdGenerator("MBR", "mbrAP01", 7, clock);

        // CPF 기능 설명입니다.
        assertThat(generator.generateOrUse("20260611141234567ACCaccAP010000001"))
                .isEqualTo("20260611141234567ACCaccAP010000001");
        // CPF 기능 설명입니다.
        assertThat(generator.generateOrUse("TRX-TEST-001"))
                .isEqualTo("20260611141234567MBRmbrAP010000001");
    }
}

