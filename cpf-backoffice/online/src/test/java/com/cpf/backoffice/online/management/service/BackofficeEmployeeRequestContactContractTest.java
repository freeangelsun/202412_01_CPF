package com.cpf.backoffice.online.management.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** MBW 직원 연락처 계약과 기존 생성자 호환성을 고정합니다. */
class BackofficeEmployeeRequestContactContractTest {

    @Test
    void employeeRequestSeparatesMobileAndOfficePhone() {
        BackofficeManagementService.EmployeeRequest request = new BackofficeManagementService.EmployeeRequest(
                "E001", null, "ORG001", "직원", null, null, null, null,
                LocalDate.of(2026, 7, 27), null, "employee@example.com", "+82-10-1234-5678",
                "02-1234-5678 (1234)", "Y", null, "MBW", "등록");

        assertEquals("+82-10-1234-5678", request.mobileNo());
        assertEquals("02-1234-5678 (1234)", request.officePhoneNo());
        assertFalse(request.clearEmail());
        assertFalse(request.clearMobileNo());
        assertFalse(request.clearOfficePhoneNo());
    }

    @Test
    void legacyConstructorKeepsOfficePhoneNull() {
        BackofficeManagementService.EmployeeRequest request = new BackofficeManagementService.EmployeeRequest(
                "E001", null, "ORG001", "직원", null, null, null, null,
                LocalDate.of(2026, 7, 27), null, null, null,
                "Y", null, "MBW", "등록");

        assertNull(request.officePhoneNo());
    }
}
