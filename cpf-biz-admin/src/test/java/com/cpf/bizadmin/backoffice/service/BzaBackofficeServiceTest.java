package com.cpf.bizadmin.backoffice.service;

import com.cpf.bizadmin.audit.service.BzaBusinessAuditService;
import com.cpf.bizadmin.backoffice.controller.BzaBackofficeController;
import com.cpf.bizadmin.backoffice.repository.BzaBackofficeRepository;
import com.cpf.core.common.exception.CpfValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** BZA Backoffice의 폐기 API, 순환 방지와 감사 fail-closed 계약을 검증합니다. */
class BzaBackofficeServiceTest {
    private final BzaBackofficeRepository repository = mock(BzaBackofficeRepository.class);
    private final BzaBusinessAuditService auditService = mock(BzaBusinessAuditService.class);
    private final BzaBackofficeService service = new BzaBackofficeService(repository, auditService);

    @Test
    void legacyDirectApprovalEndpointsRemainGoneWithoutCallingService() {
        BzaBackofficeService mockedService = mock(BzaBackofficeService.class);
        BzaBackofficeController controller = new BzaBackofficeController(mockedService);

        assertGone(() -> controller.approvals(null, null, 100));
        assertGone(() -> controller.createApproval(Map.of(), "operator01"));
        assertGone(() -> controller.approval(10L));
        assertGone(() -> controller.actApproval(10L, Map.of(), "operator01"));

        verify(mockedService, never()).findOrganizations();
    }

    @Test
    void organizationCycleIsRejectedBeforeWriteAndAudit() {
        when(repository.wouldCreateOrganizationCycle("ORG001", "ORG002")).thenReturn(true);
        var request = new BzaBackofficeService.OrganizationRequest(
                "ORG001", "ORG002", "테스트 조직", "DEPARTMENT", 1,
                null, null, "Y", null, null, "순환 방지 검증");

        assertThatThrownBy(() -> service.saveOrganization(request, "operator01"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("조직 순환");

        verify(repository, never()).saveOrganization(any());
        verify(auditService, never()).record(
                anyString(), anyString(), anyString(), anyString(), anyString(), any(), any());
    }

    @Test
    void rawContactAuditFailureIsNotSwallowed() {
        when(repository.findEmployeeRawContact("EMP001")).thenReturn(Optional.of(Map.of(
                "email", "employee@example.com",
                "mobileNo", "010-1234-5678",
                "officePhoneNo", "02-1234-5678")));
        when(auditService.record(
                anyString(), anyString(), anyString(), anyString(), anyString(), any(), any()))
                .thenThrow(new IllegalStateException("audit store down"));

        assertThatThrownBy(() -> service.findEmployeeRaw("EMP001", "operator01", "원문 조회 검증"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("audit store down");
    }

    private void assertGone(Runnable request) {
        ResponseStatusException exception = org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class, request::run);
        org.assertj.core.api.Assertions.assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.GONE);
        org.assertj.core.api.Assertions.assertThat(exception.getReason())
                .contains("정책 기반 /api/bza/approvals/**");
    }
}
