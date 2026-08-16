package com.cpf.admin.opr.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import org.junit.jupiter.api.Test;

/** ADM 운영자 Profile 연락처 Projection 계약과 기존 생성자 호환성을 고정합니다. */
class AdmOperatorContactContractTest {

    @Test
    void contactFieldsAreSeparatedFromOfficePhone() {
        AdmOperator operator = new AdmOperator(
                "operator01", "운영자", "+82-10-1234-5678", "02-1234-5678 (1234)",
                List.of("ADM_VIEWER"), false, false, true, "2026-07-27T00:00:00", "2026-07-27T00:00:00");

        assertEquals("+82-10-1234-5678", operator.mobileNo());
        assertEquals("02-1234-5678 (1234)", operator.officePhoneNo());
    }

    @Test
    void legacyConstructorKeepsOptionalContactsNull() {
        AdmOperator operator = new AdmOperator(
                "operator01", "운영자", List.of("ADM_VIEWER"),
                false, false, true, "2026-07-27T00:00:00", "2026-07-27T00:00:00");
        AdmOperatorCreateRequest request = new AdmOperatorCreateRequest(
                "operator01", "운영자", "not-a-real-password", List.of("ADM_VIEWER"), "ADM", "등록");

        assertNull(operator.mobileNo());
        assertNull(operator.officePhoneNo());
        assertEquals("ACTIVE", operator.accountStatus());
        assertFalse(operator.rawViewAllowed());
        assertNull(request.mobileNo());
        assertNull(request.officePhoneNo());
        assertNull(request.operationId());
    }
}
