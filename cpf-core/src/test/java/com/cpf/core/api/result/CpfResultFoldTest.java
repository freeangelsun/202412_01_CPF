package com.cpf.core.api.result;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** JTA 없는 분산 Boundary의 SUCCESS/FAILURE/UNKNOWN 후처리 분기를 고정한다. */
class CpfResultFoldTest {
    @Test
    void fourWayFoldKeepsBusinessTechnicalAndUnknownSeparate() {
        assertEquals("A:OK", CpfResult.success("OK").fold(
                value -> "A:" + value,
                result -> "B",
                result -> "T",
                result -> "U"));
        assertEquals("B", CpfResult.<String>businessFailure("B001", "business").fold(
                value -> "A", result -> "B", result -> "T", result -> "U"));
        assertEquals("T", CpfResult.<String>technicalFailure("T001", "technical").fold(
                value -> "A", result -> "B", result -> "T", result -> "U"));
    }

    @Test
    void threeWayFoldNeverMergesUnknownIntoOrdinaryFailure() {
        CpfRecoveryInfo recovery = new CpfRecoveryInfo("REC-1", "RECONCILE");
        assertEquals("U:RECONCILE", CpfResult.<String>unknown("U001", "unknown", recovery).fold(
                value -> "A",
                result -> "B",
                result -> "U:" + result.recoveryInfo().action()));
    }
}
