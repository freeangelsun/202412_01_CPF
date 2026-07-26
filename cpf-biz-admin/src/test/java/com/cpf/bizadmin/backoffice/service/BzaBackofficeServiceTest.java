package com.cpf.bizadmin.backoffice.service;

import com.cpf.bizadmin.audit.service.BzaBusinessAuditService;
import com.cpf.bizadmin.backoffice.repository.BzaBackofficeRepository;
import com.cpf.core.common.exception.CpfValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** BZA 결재 상태표, 낙관적 잠금, 중복 행위 방지 계약을 검증합니다. */
class BzaBackofficeServiceTest {
    private final BzaBackofficeRepository repository = mock(BzaBackofficeRepository.class);
    private final BzaBusinessAuditService auditService = mock(BzaBusinessAuditService.class);
    private final BzaBackofficeService service = new BzaBackofficeService(repository, auditService);

    @Test
    void legacyDirectApprovalCreationIsRejectedBeforeRepositoryWrite() {
        var request = new BzaBackofficeService.ApprovalCreateRequest(
                "MASTER_CHANGE", "BZA", "기준정보 변경", "EMP001", "SEQUENTIAL",
                null, "{}", null,
                List.of(new BzaBackofficeService.ApprovalLineRequest(1, "EMP001", "ALL_APPROVE")),
                "EMP001", "기준정보 변경 요청");

        assertThatThrownBy(() -> service.createApproval(request, "operator01"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("정책 기반 Approval Engine");
        verify(repository, never()).createApproval(any());
    }

    @Test
    void legacyDirectApprovalActionIsRejectedBeforeRepositoryWrite() {
        assertThatThrownBy(() -> service.act(
                10L,
                action("SUBMIT", "EMP999", "submit-001"),
                "operator01"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("정책 기반 Approval Engine");

        verify(repository, never()).updateApprovalStatus(
                anyLong(), anyLong(), anyString(), anyInt(), anyString());
        verify(repository, never()).insertApprovalHistory(any());
    }

    @Test
    void approvalReadModelStillAggregatesDocumentLinesAndHistory() {
        when(repository.findApproval(10L)).thenReturn(Optional.of(approval("APPROVED", 2L, 1)));
        when(repository.findApprovalLines(10L)).thenReturn(List.of());
        when(repository.findApprovalHistory(10L)).thenReturn(List.of());

        Map<String, Object> result = service.findApproval(10L);

        assertThat(result.get("approvalStatus")).isEqualTo("APPROVED");
        assertThat(result).containsKeys("lines", "history");
    }

    private BzaBackofficeService.ApprovalActionRequest action(String action, String actor, String key) {
        return new BzaBackofficeService.ApprovalActionRequest(action, actor, key, "테스트 처리 사유", "테스트 의견");
    }

    private Map<String, Object> approval(String status, long version, int step) {
        return Map.of(
                "approvalId", 10L,
                "approvalStatus", status,
                "versionNo", version,
                "currentStepNo", step,
                "requesterEmployeeNo", "EMP001");
    }
}
