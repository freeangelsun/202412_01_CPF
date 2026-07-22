package com.cpf.member.common.logging;

import com.cpf.core.common.logging.TransactionIdGenerator;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/** 거래 ID 생성 규격과 외부 거래 ID 재사용 조건을 검증합니다. */
class TransactionIdGeneratorTest {

    /** 시각·시스템 코드·WAS ID·순번이 정해진 위치에 조합되는지 확인합니다. */
    @Test
    void generatesTransactionIdWithTimestampModuleWasAndSequence() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-06-11T05:12:34.567Z"),
                ZoneId.of("Asia/Seoul"));
        TransactionIdGenerator generator = new TransactionIdGenerator("REF", "refAP01", 7, clock);

        assertThat(generator.generate()).isEqualTo("20260611141234567REFrefAP010000001");
        assertThat(generator.generate()).isEqualTo("20260611141234567REFrefAP010000002");
    }

    /** 정규 거래 ID만 재사용하고 비표준 입력은 새 ID로 대체하는지 확인합니다. */
    @Test
    void reusesOnlyValidIncomingTransactionId() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-06-11T05:12:34.567Z"),
                ZoneId.of("Asia/Seoul"));
        TransactionIdGenerator generator = new TransactionIdGenerator("MBR", "mbrAP01", 7, clock);

        assertThat(generator.generateOrUse("20260611141234567REFrefAP010000001"))
                .isEqualTo("20260611141234567REFrefAP010000001");
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
