package com.cpf.security.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 정본 CPF 거래ID가 계좌번호로 오인되어 마스킹되지 않는지 검증합니다.
 *
 * <p>거래ID는 {@code yyyyMMddHHmmssSSS(17)+issuerCode(3)+instanceToken(7)+sequence(7)}이라
 * 앞 17자리가 timestamp다. 이 숫자열이 10~19자리 계좌 패턴에 걸려 마스킹되면 File/DB/ADM 통합
 * 로그를 잇는 상관관계 키가 사라진다. 실제로 Batch→Domain 응답유실 검증에서 파일 로그의
 * transactionId가 {@code ***6762BATS1JCXLU0000001}로 훼손되어 DB 거래와 대조되지 않았다.
 *
 * <p>동시에 이 예외가 마스킹 우회 통로가 되면 안 된다. 아래 음성 케이스가 그것을 고정한다.
 */
class CpfMaskingRuntimeTransactionIdTest {

    /** 실제 Runtime이 발급한 형태의 정본 거래ID. */
    private static final String CANONICAL_TRANSACTION_ID = "20260903001256762BATS1JCXLU0000001";

    @Test
    void canonicalTransactionIdSurvivesMaskingSoIntegratedLogsStayCorrelated() {
        assertTrue(CpfTransactionIdsProbe.isCanonical(CANONICAL_TRANSACTION_ID),
                "테스트 입력 자체가 정본 규격이어야 한다");
        assertEquals(CANONICAL_TRANSACTION_ID, CpfMaskingRuntime.mask(CANONICAL_TRANSACTION_ID));
    }

    @Test
    void canonicalTransactionIdSurvivesInsideAJsonLogLine() {
        String line = "{\"transactionId\":\"" + CANONICAL_TRANSACTION_ID + "\",\"status\":\"SUCCESS\"}";
        assertEquals(line, CpfMaskingRuntime.mask(line));
    }

    @Test
    void plainLongNumericIdentifierIsStillMasked() {
        // 계좌/카드번호 형태는 종전대로 마스킹되어야 한다. 예외가 전면 해제되면 안 된다.
        String masked = CpfMaskingRuntime.mask("account=12345678901234567");
        assertFalse(masked.contains("12345678901234567"), masked);
        assertTrue(masked.contains("***"), masked);
    }

    @Test
    void standaloneDigitRunIsStillMaskedRegardlessOfLength() {
        // 독립 토큰으로 나타난 숫자열은 길이와 무관하게 계좌/카드번호 후보로 보고 마스킹한다.
        for (String candidate : new String[] {"1234567890", "1234567890123456789"}) {
            String masked = CpfMaskingRuntime.mask("value " + candidate + " end");
            assertFalse(masked.contains(candidate), masked);
        }
    }

    @Test
    void shorterDigitRunIsNotTreatedAsAnAccountNumber() {
        // 9자리 이하는 계좌/카드번호로 보지 않는다(기존 계약 유지).
        assertEquals("value 123456789 end", CpfMaskingRuntime.mask("value 123456789 end"));
    }

    @Test
    void quotedAccountNumberIsStillMasked() {
        // 따옴표/구분자로 둘러싸인 순수 숫자열은 계좌/카드번호로 보고 계속 마스킹한다.
        String masked = CpfMaskingRuntime.mask("{\"cardNumber\":\"1234567890123456\"}");
        assertFalse(masked.contains("1234567890123456"), masked);
    }

    @Test
    void cpfTraceIdentifierIsNotDamagedByTheAccountNumberRule() {
        // trace/span id 는 CPF 가 발급한 상관관계 키다. 16진 문자열 안의 숫자열이 계좌번호로
        // 오인되어 `da3f***6562...` 로 바뀌면 File/DB 로그를 잇는 키가 사라진다.
        String traceId = "da3f0a1b6562169d2089980d121abcde";
        assertEquals(traceId, CpfMaskingRuntime.mask(traceId));
        String spanId = "801265b74e6d515b";
        assertEquals(spanId, CpfMaskingRuntime.mask(spanId));
    }

    @Test
    void digitRunGluedToAnIdentifierTokenIsTreatedAsAnIdentifier() {
        // 영숫자 토큰 안에 들어 있는 숫자열은 계좌번호가 아니라 식별자의 일부다.
        // 라벨이 붙은 민감값은 key 기반 규칙이 따로 막는다(아래 단정으로 고정).
        assertEquals("req12345678901abc", CpfMaskingRuntime.mask("req12345678901abc"));
        String labelled = CpfMaskingRuntime.mask("accountNo=req12345678901abc");
        assertFalse(labelled.contains("req12345678901abc"), labelled);
    }

    /** core 계약을 테스트에서 직접 부르기 위한 얇은 접근자. */
    private static final class CpfTransactionIdsProbe {
        static boolean isCanonical(String value) {
            return com.cpf.core.api.transaction.CpfTransactionIds.isCanonical(value);
        }
    }
}
