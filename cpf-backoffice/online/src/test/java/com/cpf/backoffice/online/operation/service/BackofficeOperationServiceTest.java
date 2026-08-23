package com.cpf.backoffice.online.operation.service;

import com.cpf.backoffice.online.audit.service.BackofficeBusinessAuditService;
import com.cpf.backoffice.online.auth.repository.BackofficeAuthRepository;
import com.cpf.backoffice.online.operation.repository.BackofficeOperationRepository;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.security.api.password.CpfPasswordEncoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BackofficeOperationServiceTest {

    private final BackofficeOperationRepository repository = mock(BackofficeOperationRepository.class);
    private final CpfPasswordEncoder passwordHashingPort = mock(CpfPasswordEncoder.class);
    private final BackofficeBusinessAuditService auditService = mock(BackofficeBusinessAuditService.class);
    private final BackofficeAuthRepository authRepository = mock(BackofficeAuthRepository.class);
    private final BackofficeOperationService service =
            new BackofficeOperationService(repository, passwordHashingPort, auditService, authRepository);

    @Test
    void createAdminUserStartsPendingWithoutRoleAndDoesNotExposePasswordHash() {
        when(repository.findAdminUser("operator01")).thenReturn(Optional.empty());
        when(passwordHashingPort.encode(org.mockito.ArgumentMatchers.any(char[].class)))
                .thenReturn("{cpf-pbkdf2-sha256-v1}encoded");
        when(repository.insertAdminUser(any())).thenReturn(1);
        var request = new BackofficeOperationService.AdminUserRequest(
                "operator01", "업무 운영자", null, "Change-Me-1234!",
                null, "Y", "N", "Y", null,
                "security-admin", "신규 운영자 등록");

        Map<String, Object> result = service.saveAdminUser(request, "security-admin");

        assertThat(result).doesNotContainKeys("passwordHash", "rawPassword");
        assertThat(result.get("accountStatus")).isEqualTo("PENDING_ACTIVATION");
        verify(repository).insertAdminUser(argThat(values ->
                "{cpf-pbkdf2-sha256-v1}encoded".equals(values.get("passwordHash"))
                        && "PENDING_ACTIVATION".equals(values.get("accountStatus"))));
        verify(authRepository, never()).revokeAllRefreshTokensByLoginId(any());
        verify(auditService).record(
                eq("security-admin"), eq("ADMIN_USER_SAVE"), eq("mbw_admin_user"), eq("operator01"),
                eq("신규 운영자 등록"), isNull(),
                argThat(after -> !String.valueOf(after).contains("encoded")));
    }

    @Test
    void createAdminUserRejectsRoleAutoGrant() {
        when(repository.findAdminUser("operator01")).thenReturn(Optional.empty());
        var request = new BackofficeOperationService.AdminUserRequest(
                "operator01", "업무 운영자", "MBW_OPERATOR", "Change-Me-1234!",
                null, "Y", "N", "Y", null,
                "security-admin", "신규 운영자 등록");

        assertThatThrownBy(() -> service.saveAdminUser(request, "security-admin"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("Role을 자동 부여하지 않습니다");
        verify(repository, never()).insertAdminUser(any());
    }

    @Test
    void updateAdminUserRejectsForbiddenStatusTransitionBeforeWrite() {
        when(repository.findAdminUser("operator01")).thenReturn(Optional.of(Map.of(
                "accountStatus", "ACTIVE", "versionNo", 3L)));
        var request = new BackofficeOperationService.AdminUserRequest(
                "operator01", "업무 운영자", null, null,
                "PENDING_ACTIVATION", "Y", "N", "N", 3L,
                "security-admin", "상태 변경");

        assertThatThrownBy(() -> service.saveAdminUser(request, "security-admin"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("허용되지 않은 관리자 상태 전이");
        verify(repository, never()).updateAdminUser(any());
        verify(authRepository, never()).revokeAllRefreshTokensByLoginId(any());
    }

    @Test
    void updateAdminUserRevokesRefreshSessionsAfterSuccessfulStatusChange() {
        when(repository.findAdminUser("operator01")).thenReturn(Optional.of(Map.of(
                "accountStatus", "ACTIVE", "versionNo", 3L)));
        when(repository.updateAdminUser(any())).thenReturn(1);
        var request = new BackofficeOperationService.AdminUserRequest(
                "operator01", "업무 운영자", null, null,
                "SUSPENDED", "Y", "N", "N", 3L,
                "security-admin", "일시 정지");

        service.saveAdminUser(request, "security-admin");

        verify(authRepository).revokeAllRefreshTokensByLoginId("operator01");
    }

    @Test
    void saveRoleRevokesSessionsOfRoleHolders() {
        when(repository.findRole("MBW_OPERATOR")).thenReturn(Optional.empty());
        when(repository.insertRole(any())).thenReturn(1);
        var request = new BackofficeOperationService.RoleRequest(
                "MBW_OPERATOR", "업무 운영자", "N", "OWN", "Y", null,
                "security-admin", "Role 등록");

        service.saveRole(request, "security-admin");

        verify(authRepository).revokeRefreshTokensByRoleCode("MBW_OPERATOR");
    }

    @Test
    void savePermissionRejectsUnknownHttpMethodBeforeWrite() {
        var request = new BackofficeOperationService.PermissionRequest(
                null,
                "MBW_OPERATOR", "SETTING", "EXECUTE", "API", "TRACE",
                "/api/v1/backoffice/settings/**", "MBW", "ALL", "ROLE", "Y", "Y", null,
                "security-admin", "설정 실행 권한 등록");

        assertThatThrownBy(() -> service.savePermission(request, "security-admin"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("HTTP 메서드");
        verify(repository, never()).insertPermission(any());
        verify(repository, never()).updatePermission(any());
    }
}
