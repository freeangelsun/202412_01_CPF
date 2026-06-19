package cpf.mbr.common.logging;

import cpf.pfw.common.logging.TransactionIdGenerator;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

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

    /**
     * WAS ID는 운영 로그에서 서버 인스턴스를 식별하는 핵심 값이므로 7자리 표준을 강제합니다.
     */
    @Test
    void rejectsWasIdThatDoesNotMatchSevenCharacterStandard() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-06-11T05:12:34.567Z"),
                ZoneId.of("Asia/Seoul"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new TransactionIdGenerator("MBR", "local001", 7, clock))
                .withMessageContaining("wasId");
    }

    /**
     * 공식 거래 ID는 34자리 기본 규격으로 검증합니다.
     */
    @Test
    void validatesOfficialTransactionGlobalIdFormat() {
        assertThat(TransactionIdGenerator.isValid("20260615120000000MBRlocal010000001", 7)).isTrue();
        assertThat(TransactionIdGenerator.isValid("20260615120000000MBRlocal1000001", 7)).isFalse();
    }
}
