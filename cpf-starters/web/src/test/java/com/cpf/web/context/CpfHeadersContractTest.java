package com.cpf.web.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cpf.web.api.CpfHeaders;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** 짧은 canonical Header와 내부 거래 필수값 검증 계약을 고정한다. */
class CpfHeadersContractTest {
    @Test
    void builderCreatesAndUpdatesCanonicalHeadersWithoutLiteralNames() {
        Map<String, String> headers = CpfHeaders.builder()
                .txId("20260813010101999MBRlocal010000001")
                .execId("EX-20260813-0001")
                .caller("MBR")
                .target("EXS")
                .buildInternal();

        assertEquals("20260813010101999MBRlocal010000001", CpfHeaders.get(headers, CpfHeaders.transactionId()));
        assertEquals("EX-20260813-0001", CpfHeaders.get(headers, CpfHeaders.executionId()));
        assertTrue(headers.containsKey("X-Transaction-Id"));

        Map<String, String> changed = CpfHeaders.from(headers).target("ACC").remove(CpfHeaders.caller()).build();
        assertEquals("ACC", CpfHeaders.get(changed, CpfHeaders.target()));
        assertEquals(null, CpfHeaders.get(changed, CpfHeaders.caller()));
    }

    @Test
    void internalHeadersFailFastWhenTransactionLineageIsMissing() {
        assertThrows(CpfHeaderValidationException.class,
                () -> CpfHeaders.builder().txId("20260813010101999MBRlocal010000001").buildInternal());
        assertThrows(CpfHeaderValidationException.class,
                () -> CpfHeaders.builder().txId("20260813010101999MBRlocal010000001").execId("EX-20260813-0001").buildInternal());
        assertThrows(CpfHeaderValidationException.class,
                () -> CpfHeaders.builder().txId("20260813010101999MBRlocal010000001").execId("EX-20260813-0001")
                        .caller("MBR").buildInternal());
    }

    @Test
    void internalHeadersRejectNonCanonicalTransactionIdEvenWhenRequiredHeadersExist() {
        assertThrows(IllegalArgumentException.class,
                () -> CpfHeaders.builder().txId("TX-INVALID").execId("EX-1")
                        .caller("MBR").target("EXS").buildInternal());
    }
}
