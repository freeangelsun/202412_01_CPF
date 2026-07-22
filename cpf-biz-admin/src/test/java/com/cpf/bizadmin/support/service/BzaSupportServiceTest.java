package com.cpf.bizadmin.support.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpf.bizadmin.support.repository.BzaSupportRepository;
import com.cpf.core.common.attachment.CpfAttachmentContent;
import com.cpf.core.common.attachment.CpfAttachmentStoragePort;
import com.cpf.core.common.attachment.CpfStoredAttachment;
import com.cpf.core.common.exception.CpfValidationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BzaSupportServiceTest {
    private final BzaSupportRepository repository = mock(BzaSupportRepository.class);
    private final CpfAttachmentStoragePort storagePort = mock(CpfAttachmentStoragePort.class);
    private final BzaSupportService service = new BzaSupportService(repository, storagePort, new ObjectMapper());

    @Test
    void savedSearchCanonicalizesObjectJsonAndUsesAuthenticatedOperator() {
        var request = new BzaSupportService.SavedSearchRequest(
                "order", "고액 주문", "{\"amount\":10000,\"status\":\"WAITING\"}", "N", "업무 검색 저장");

        Map<String, Object> result = service.saveSavedSearch(request, "operator01");

        assertThat(result.get("ownerLoginId")).isEqualTo("operator01");
        assertThat(result.get("screenCode")).isEqualTo("ORDER");
        assertThat(result.get("criteriaJson")).isEqualTo("{\"amount\":10000,\"status\":\"WAITING\"}");
        verify(repository).saveSavedSearch(any());
        verify(repository).insertBusinessAudit(any());
    }

    @Test
    void savedSearchRejectsArrayJsonBeforeRepositoryWrite() {
        var request = new BzaSupportService.SavedSearchRequest(
                "ORDER", "잘못된 조건", "[]", "N", "검증");

        assertThatThrownBy(() -> service.saveSavedSearch(request, "operator01"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("JSON object");
        verify(repository, never()).saveSavedSearch(any());
    }

    @Test
    void permissionSimulationMatchesMethodPathEnvironmentAndDomain() {
        Map<String, Object> permission = new LinkedHashMap<>();
        permission.put("roleCode", "BZA_MANAGER");
        permission.put("menuCode", "ORDER");
        permission.put("buttonCode", "WRITE");
        permission.put("httpMethod", "POST");
        permission.put("apiPattern", "/api/bza/orders/**");
        permission.put("environmentCode", "ALL");
        permission.put("domainCode", "BZA");
        permission.put("dataScope", "ORGANIZATION");
        permission.put("allowYn", "Y");
        when(repository.findRolePermissions(List.of("BZA_MANAGER"))).thenReturn(List.of(permission));
        var request = new BzaSupportService.PermissionSimulationRequest(
                "BZA_MANAGER", "ORDER", "WRITE", "POST", "/api/bza/orders/10",
                "PROD", "BZA", "배포 전 권한 확인");

        Map<String, Object> result = service.simulatePermission(request, "security-admin");

        assertThat(result.get("allowed")).isEqualTo(true);
        assertThat((List<?>) result.get("matchedRules")).hasSize(1);
        verify(repository).insertBusinessAudit(any());
    }

    @Test
    void attachmentMetadataFailureCompensatesStoredFile() {
        CpfStoredAttachment stored = new CpfStoredAttachment(
                "GROUP/a.txt", "a.txt", "a.txt", "text/plain", 1, "a".repeat(64), Instant.now());
        when(storagePort.store("GROUP", "a.txt", "text/plain", new byte[]{1})).thenReturn(stored);
        when(repository.insertAttachment(any())).thenThrow(new IllegalStateException("DB 실패"));

        assertThatThrownBy(() -> service.storeAttachment(
                "GROUP", "a.txt", "text/plain", new byte[]{1}, "증적 첨부", "operator01"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DB 실패");
        verify(storagePort).delete("GROUP/a.txt");
    }

    @Test
    void attachmentDownloadRejectsChecksumMismatchAndWritesFailureAudit() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("attachmentId", 1L);
        metadata.put("originalFileName", "evidence.txt");
        metadata.put("storageKey", "GROUP/evidence.txt");
        metadata.put("contentType", "text/plain");
        metadata.put("checksumSha256", "a".repeat(64));
        metadata.put("scanStatus", "PASSED_LOCAL_POLICY");
        when(repository.findAttachment(1L)).thenReturn(java.util.Optional.of(metadata));
        when(storagePort.read("GROUP/evidence.txt"))
                .thenReturn(new CpfAttachmentContent(new byte[]{1}, "b".repeat(64)));

        assertThatThrownBy(() -> service.downloadAttachment(1L, "감사 증적 확인", "operator01"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("checksum");
        verify(repository).insertDownloadAudit(any());
    }
}
