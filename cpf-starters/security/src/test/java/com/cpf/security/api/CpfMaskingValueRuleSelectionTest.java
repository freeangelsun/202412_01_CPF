package com.cpf.security.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 마스킹 값 규칙이 **운영자 선택**으로 켜지고 꺼지는지 검증합니다(Harness §28.1).
 *
 * <p>규칙을 코드에 고정해 항상 적용했더니, CPF가 발급한 거래ID·traceId까지 계좌번호로 오인 마스킹되어
 * File/DB/ADM 통합 로그의 상관관계 키가 사라지는 사고가 있었다. 무엇을 가릴지는 그 로그를 운영하는
 * 사람이 정해야 하며, 그 선택이 실제 마스킹 동작에 반영되어야 한다.</p>
 */
class CpfMaskingValueRuleSelectionTest {

    private static final Set<String> KEYS = Set.of("password");

    @AfterEach
    void restoreDefaults() {
        // 다른 테스트에 선택이 새지 않도록 fail-closed 기본값으로 되돌린다.
        CpfMaskingRuntime.replacePolicy(KEYS, 4000, true, CpfMaskingValueRule.defaults());
    }

    private void select(CpfMaskingValueRule... rules) {
        Set<CpfMaskingValueRule> selected = rules.length == 0
                ? EnumSet.noneOf(CpfMaskingValueRule.class)
                : EnumSet.copyOf(java.util.Arrays.asList(rules));
        CpfMaskingRuntime.replacePolicy(KEYS, 4000, true, selected);
    }

    @Test
    void defaultSelectionMasksEveryValueRule() {
        select(CpfMaskingValueRule.values());
        assertFalse(CpfMaskingRuntime.mask("value 12345678901234567 end").contains("12345678901234567"));
        assertFalse(CpfMaskingRuntime.mask("mail a@b.com end").contains("a@b.com"));
        assertFalse(CpfMaskingRuntime.mask("rrn 900101-1234567 end").contains("900101-1234567"));
    }

    @Test
    void operatorCanTurnOffTheLongNumericRuleWithoutAffectingOthers() {
        select(CpfMaskingValueRule.EMAIL, CpfMaskingValueRule.KOREAN_RESIDENT_REGISTRATION_NUMBER);
        // 운영자가 끈 규칙은 적용되지 않는다.
        assertEquals("value 12345678901234567 end",
                CpfMaskingRuntime.mask("value 12345678901234567 end"));
        // 켜 둔 규칙은 그대로 적용된다.
        assertFalse(CpfMaskingRuntime.mask("mail a@b.com end").contains("a@b.com"));
        assertFalse(CpfMaskingRuntime.mask("rrn 900101-1234567 end").contains("900101-1234567"));
    }

    @Test
    void operatorCanSelectNothingAndNoValueRuleIsApplied() {
        select();
        String payload = "value 12345678901234567 mail a@b.com rrn 900101-1234567 end";
        assertEquals(payload, CpfMaskingRuntime.mask(payload));
    }

    @Test
    void keyBasedMaskingStillAppliesRegardlessOfValueRuleSelection() {
        // 값 규칙을 모두 꺼도 키 기반 규칙(운영자가 고른 sensitiveKeys)은 계속 적용되어야 한다.
        select();
        String masked = CpfMaskingRuntime.mask("password=super-secret-value");
        assertFalse(masked.contains("super-secret-value"), masked);
    }

    @Test
    void bearerSelectionAndFlagStayConsistent() {
        // 화면에서 BEARER_TOKEN 을 끄면 실제로도 꺼져야 한다. 두 표현이 어긋나면 안 된다.
        CpfMaskingRuntime.replacePolicy(KEYS, 4000, true,
                EnumSet.complementOf(EnumSet.of(CpfMaskingValueRule.BEARER_TOKEN)));
        CpfMaskingRuntime.MaskingPolicy policy = CpfMaskingRuntime.currentPolicy();
        assertFalse(policy.maskBearerToken());
        assertFalse(policy.valueRules().contains(CpfMaskingValueRule.BEARER_TOKEN));
        assertTrue(CpfMaskingRuntime.mask("Authorization: Bearer abc.def").contains("abc.def"));
    }

    @Test
    void policyHashChangesWhenTheSelectionChanges() {
        select(CpfMaskingValueRule.values());
        String all = CpfMaskingRuntime.currentPolicy().policyHash();
        select(CpfMaskingValueRule.EMAIL);
        assertFalse(all.equals(CpfMaskingRuntime.currentPolicy().policyHash()));
    }

    @Test
    void unknownRuleNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> CpfMaskingValueRule.of("NOT_A_RULE"));
    }

    @Test
    void csvRoundTripKeepsDeclarationOrder() {
        String csv = CpfMaskingValueRule.toCsv(
                EnumSet.of(CpfMaskingValueRule.EMAIL, CpfMaskingValueRule.PRIVATE_KEY));
        assertEquals("PRIVATE_KEY,EMAIL", csv);
        assertEquals(EnumSet.of(CpfMaskingValueRule.PRIVATE_KEY, CpfMaskingValueRule.EMAIL),
                CpfMaskingValueRule.parseCsv(csv));
    }

    @Test
    void blankCsvFallsBackToTheFailClosedDefault() {
        assertEquals(CpfMaskingValueRule.defaults(), CpfMaskingValueRule.parseCsv(""));
        assertEquals(CpfMaskingValueRule.defaults(), CpfMaskingValueRule.parseCsv(null));
    }
}
