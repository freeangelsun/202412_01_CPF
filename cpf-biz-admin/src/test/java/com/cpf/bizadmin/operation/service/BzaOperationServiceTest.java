package com.cpf.bizadmin.operation.service;

import com.cpf.bizadmin.audit.service.BzaBusinessAuditService;
import com.cpf.bizadmin.operation.repository.BzaOperationRepository;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.security.password.CpfPasswordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BzaOperationServiceTest {

    private final BzaOperationRepository repository = mock(BzaOperationRepository.class);
    private final CpfPasswordService passwordHashingPort = mock(CpfPasswordService.class);
    private final BzaBusinessAuditService auditService = mock(BzaBusinessAuditService.class);
    private final BzaOperationService service =
            new BzaOperationService(repository, passwordHashingPort, auditService);

    @Test
    void saveAdminUserHashesPasswordAndExcludesSecretFromResponseAndAudit() {
        when(repository.findAdminUser("operator01")).thenReturn(Optional.empty());
        when(passwordHashingPort.hash(org.mockito.ArgumentMatchers.any(char[].class)))
                .thenReturn("{cpf-pbkdf2-sha256-v1}encoded");
        var request = new BzaOperationService.AdminUserRequest(
                "operator01", "업무 운영자", "BZA_OPERATOR", "Change-Me-1234!",
                "Y", "N", "Y", "security-admin", "신규 운영자 등록");

        Map<String, Object> result = service.saveAdminUser(request, "security-admin");

        assertThat(result).doesNotContainKeys("passwordHash", "rawPassword");
        verify(repository).saveAdminUser(org.mockito.ArgumentMatchers.argThat(values ->
                "{cpf-pbkdf2-sha256-v1}encoded".equals(values.get("passwordHash"))));
        verify(auditService).record(
                eq("security-admin"),
                eq("ADMIN_USER_SAVE"),
                eq("bza_admin_user"),
                eq("operator01"),
                eq("신규 운영자 등록"),
                isNull(),
                argThat(after -> !String.valueOf(after).contains("encoded")));
    }

    @Test
    void savePermissionRejectsUnknownHttpMethodBeforeWrite() {
        var request = new BzaOperationService.PermissionRequest(
                null,
                "BZA_OPERATOR", "SETTING", "EXECUTE", "API", "TRACE",
                "/api/bza/settings/**", "BZA", "ALL", "ROLE", "Y", "Y", null,
                "security-admin", "설정 실행 권한 등록");

        assertThatThrownBy(() -> service.savePermission(request, "security-admin"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("HTTP 메서드");
        verify(repository, never()).insertPermission(org.mockito.ArgumentMatchers.any());
        verify(repository, never()).updatePermission(org.mockito.ArgumentMatchers.any());
    }

}
