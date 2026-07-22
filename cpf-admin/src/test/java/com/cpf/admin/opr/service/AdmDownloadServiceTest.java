package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.DownloadRequest;
import com.cpf.core.common.exception.CpfValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AdmDownloadServiceTest {

    private final JdbcTemplate cpfJdbcTemplate = mock(JdbcTemplate.class);
    private final JdbcTemplate admJdbcTemplate = mock(JdbcTemplate.class);
    private final AdmAuditLogService auditLogService = mock(AdmAuditLogService.class);
    private final AdmDownloadService service = new AdmDownloadService(cpfJdbcTemplate, admJdbcTemplate, auditLogService);

    @Test
    void findPoliciesContainsOperationalDownloadTypes() {
        // 다운로드 정책은 운영자가 조회하는 주요 로그 유형을 모두 노출해야 합니다.
        assertThat(service.findPolicies())
                .extracting("downloadType")
                .containsExactly(
                        "TRANSACTION_LOGS",
                        "ERROR_LOGS",
                        "BATCH_EXECUTIONS",
                        "NOTIFICATION_DELIVERY_LOGS");
    }

    @Test
    void downloadCsvRejectsUnsupportedTypeBeforeAuditAndDbAccess() {
        // 지원하지 않는 다운로드 유형은 감사 사유 확인이나 DB 조회 전에 차단해야 합니다.
        DownloadRequest request = new DownloadRequest(
                "UNKNOWN",
                "UNKNOWN",
                "운영 점검",
                "adm-operator",
                null,
                null,
                null,
                null,
                null,
                10,
                false);

        assertThatThrownBy(() -> service.downloadCsv(request, "adm-operator", "127.0.0.1", "test-agent"))
                .isInstanceOf(CpfValidationException.class);

        verify(auditLogService, never()).requireReason(anyString());
        verifyNoInteractions(cpfJdbcTemplate, admJdbcTemplate);
    }
}
