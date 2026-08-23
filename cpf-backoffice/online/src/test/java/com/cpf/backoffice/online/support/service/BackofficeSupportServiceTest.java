package com.cpf.backoffice.online.support.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpf.backoffice.online.audit.service.BackofficeBusinessAuditService;
import com.cpf.backoffice.online.support.repository.BackofficeSupportRepository;
import com.cpf.file.attachment.api.CpfAttachmentStream;
import com.cpf.file.attachment.api.CpfAttachmentStoragePort;
import com.cpf.file.attachment.api.CpfStoredAttachment;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.testkit.context.CpfContextTestSupport;
import com.cpf.testkit.context.CpfTestContextRuntime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackofficeSupportServiceTest {
    private static CpfTestContextRuntime contextRuntime;
    private final CpfContextTestSupport contexts =
            new CpfContextTestSupport("MBW-SUPPORT", LocalDate.of(2026, 8, 22));
    private final BackofficeSupportRepository repository = mock(BackofficeSupportRepository.class);
    private final CpfAttachmentStoragePort storagePort = mock(CpfAttachmentStoragePort.class);
    private final BackofficeBusinessAuditService auditService = mock(BackofficeBusinessAuditService.class);
    private final BackofficeSupportService service =
            new BackofficeSupportService(repository, storagePort, new ObjectMapper(), auditService);

    @BeforeAll
    static void installContextRuntime() {
        contextRuntime = CpfTestContextRuntime.install();
    }

    @AfterAll
    static void closeContextRuntime() {
        contextRuntime.close();
    }

    @AfterEach
    void assertContextClear() {
        contexts.assertClear();
    }

    @Test
    void savedSearchCanonicalizesObjectJsonAndUsesAuthenticatedOperator() {
        var request = new BackofficeSupportService.SavedSearchRequest(
                "approval", "대기 결재", "{\"status\":\"WAITING\"}", "N", "업무 검색 저장");

        Map<String, Object> result = service.saveSavedSearch(request, "operator01");

        assertThat(result.get("ownerLoginId")).isEqualTo("operator01");
        assertThat(result.get("screenCode")).isEqualTo("APPROVAL");
        assertThat(result.get("criteriaJson")).isEqualTo("{\"status\":\"WAITING\"}");
        verify(repository).saveSavedSearch(any());
        verify(auditService).record(
                eq("operator01"),
                eq("SAVED_SEARCH_SAVE"),
                eq("mbw_saved_search"),
                eq("APPROVAL:대기 결재"),
                eq("업무 검색 저장"),
                isNull(),
                any());
    }

    @Test
    void savedSearchRejectsArrayJsonBeforeRepositoryWrite() {
        var request = new BackofficeSupportService.SavedSearchRequest(
                "APPROVAL", "잘못된 조건", "[]", "N", "검증");

        assertThatThrownBy(() -> service.saveSavedSearch(request, "operator01"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("JSON object");
        verify(repository, never()).saveSavedSearch(any());
    }

    @Test
    void permissionSimulationMatchesMethodPathEnvironmentAndDomain() {
        Map<String, Object> permission = new LinkedHashMap<>();
        permission.put("roleCode", "MBW_MANAGER");
        permission.put("menuCode", "APPROVAL");
        permission.put("buttonCode", "WRITE");
        permission.put("httpMethod", "POST");
        permission.put("apiPattern", "/api/v1/backoffice/backoffice/approvals/**");
        permission.put("environmentCode", "ALL");
        permission.put("domainCode", "MBW");
        permission.put("dataScope", "ORGANIZATION");
        permission.put("allowYn", "Y");
        when(repository.findRolePermissions(List.of("MBW_MANAGER"))).thenReturn(List.of(permission));
        var request = new BackofficeSupportService.PermissionSimulationRequest(
                "MBW_MANAGER", "APPROVAL", "WRITE", "POST", "/api/v1/backoffice/backoffice/approvals/10",
                "PROD", "MBW", "배포 전 권한 확인");

        Map<String, Object> result = service.simulatePermission(request, "security-admin");

        assertThat(result.get("allowed")).isEqualTo(true);
        assertThat((List<?>) result.get("matchedRules")).hasSize(1);
        verify(auditService).record(
                eq("security-admin"),
                eq("PERMISSION_SIMULATE"),
                eq("mbw_permission"),
                eq("MBW_MANAGER:APPROVAL:WRITE"),
                eq("배포 전 권한 확인"),
                isNull(),
                any());
    }

    @Test
    void attachmentMetadataFailureCompensatesStoredFile() {
        CpfStoredAttachment stored = new CpfStoredAttachment(
                "GROUP/a.txt", "a.txt", "a.txt", "text/plain", 1, "a".repeat(64), Instant.now());
        when(storagePort.store(
                eq("GROUP"), eq("a.txt"), eq("text/plain"), any(InputStream.class), eq(1L)))
                .thenReturn(stored);
        when(repository.insertAttachment(any())).thenThrow(new IllegalStateException("DB 실패"));

        assertThatThrownBy(() -> service.storeAttachment(
                "GROUP", "a.txt", "text/plain", new byte[]{1}, "증적 첨부", "operator01"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DB 실패");
        verify(storagePort).delete("GROUP/a.txt");
    }

    @Test
    void attachmentDownloadRejectsChecksumMismatchAndWritesFailureAudit() throws Exception {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("attachmentId", 1L);
        metadata.put("originalFileName", "evidence.txt");
        metadata.put("storageKey", "GROUP/evidence.txt");
        metadata.put("contentType", "text/plain");
        metadata.put("checksumSha256", "a".repeat(64));
        metadata.put("scanStatus", "CLEAN");
        when(repository.findAttachment(1L)).thenReturn(java.util.Optional.of(metadata));
        when(storagePort.open("GROUP/evidence.txt"))
                .thenReturn(new CpfAttachmentStream(
                        new ByteArrayInputStream(new byte[]{1}), 1, "b".repeat(64)));

        try (AutoCloseable ignored = contexts.bindRoot("correlation-a", null, "operator01")) {
            assertThatThrownBy(() -> service.downloadAttachment(1L, "감사 증적 확인", "operator01"))
                    .isInstanceOf(CpfValidationException.class)
                    .hasMessageContaining("checksum");
        }
        verify(repository).insertDownloadAudit(any());
    }
}
