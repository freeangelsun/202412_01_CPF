package com.cpf.admin.opr.service;

import com.cpf.admin.config.AdmPersistencePolicy;
import com.cpf.admin.opr.dto.AdmApiPermissionSaveRequest;
import com.cpf.core.api.error.CpfBusinessException;
import com.cpf.core.api.error.CpfValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** ADM Permission 저장소의 validation, vendor-neutral upsert, fail-closed 동작을 검증합니다. */
class AdmPermissionServiceTest {

    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final AdmPermissionService service = new AdmPermissionService(jdbc, databasePolicy());

    @Test
    void createApiPermissionRejectsUnsupportedMethodBeforeDbUpdate() {
        AdmApiPermissionSaveRequest request = new AdmApiPermissionSaveRequest(
                "API_TEST", "PERMISSION", "TRACE", "/adm/api/test", "테스트 API", "READ",
                "PERMISSION", "PERMISSION_READ", "Y", "tester", "API 권한 등록 테스트");

        assertThatThrownBy(() -> service.createApiPermission(request))
                .isInstanceOf(CpfValidationException.class);
        verifyNoInteractions(jdbc);
    }

    @Test
    void updateRoleApiPermissionUsesUpdateThenInsertWithoutVendorSpecificUpsert() {
        when(jdbc.update(contains("UPDATE ADM_ROLE_API_PERMISSION"), eq("Y"), eq("tester"), eq("ADM_VIEWER"), eq("API_LOG_READ")))
                .thenReturn(0);
        when(jdbc.update(contains("INSERT INTO ADM_ROLE_API_PERMISSION"),
                eq("ADM_VIEWER"), eq("API_LOG_READ"), eq("Y"), eq("tester"), eq("tester")))
                .thenReturn(1);
        when(jdbc.queryForMap(anyString(), eq("ADM_VIEWER"), eq("API_LOG_READ")))
                .thenReturn(Map.of("ROLE_ID", "ADM_VIEWER", "API_PERMISSION_ID", "API_LOG_READ", "ALLOW_YN", "Y"));

        Map<String, Object> result = service.updateRoleApiPermission("ADM_VIEWER", "API_LOG_READ", "Y", "tester");

        assertThat(result).containsEntry("ALLOW_YN", "Y");
        verify(jdbc).update(contains("INSERT INTO ADM_ROLE_API_PERMISSION"),
                eq("ADM_VIEWER"), eq("API_LOG_READ"), eq("Y"), eq("tester"), eq("tester"));
    }

    @Test
    void databaseModeDoesNotReturnEmptyPermissionMatrixWhenDbIsUnavailable() {
        when(jdbc.queryForList(argThat(sql -> sql.toUpperCase(java.util.Locale.ROOT).contains("ADM_API_PERMISSION"))))
                .thenThrow(new DataAccessResourceFailureException("down"));

        assertThatThrownBy(service::findApiPermissionMatrix)
                .isInstanceOf(CpfBusinessException.class)
                .hasMessageContaining("권한 저장소");
    }

    @Test
    void explicitMemoryModeMayReturnSafeEmptyFallbackInTestProfile() {
        JdbcTemplate offline = mock(JdbcTemplate.class);
        when(offline.queryForList(argThat(sql -> sql.toUpperCase(java.util.Locale.ROOT).contains("ADM_API_PERMISSION"))))
                .thenThrow(new DataAccessResourceFailureException("down"));
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("test");
        environment.setProperty("cpf.adm.persistence.mode", "MEMORY");
        AdmPermissionService memoryService = new AdmPermissionService(offline, new AdmPersistencePolicy(environment));

        assertThat(memoryService.findApiPermissionMatrix()).isEqualTo(List.of());
    }

    private static AdmPersistencePolicy databasePolicy() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("test");
        environment.setProperty("cpf.adm.persistence.mode", "DATABASE");
        return new AdmPersistencePolicy(environment);
    }
}
