package com.cpf.bizadmin.operation.service;

import com.cpf.bizadmin.audit.service.BzaBusinessAuditService;
import com.cpf.bizadmin.auth.repository.BzaAuthRepository;
import com.cpf.bizadmin.operation.repository.BzaOperationRepository;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.security.api.password.CpfPasswordService;
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
class BzaOperationServiceTest {

    private final BzaOperationRepository repository = mock(BzaOperationRepository.class);
    private final CpfPasswordService passwordHashingPort = mock(CpfPasswordService.class);
    private final BzaBusinessAuditService auditService = mock(BzaBusinessAuditService.class);
    private final BzaAuthRepository authRepository = mock(BzaAuthRepository.class);
    private final BzaOperationService service =
            new BzaOperationService(repository, passwordHashingPort, auditService, authRepository);

    @Test
    void createAdminUserStartsPendingWithoutRoleAndDoesNotExposePasswordHash() {
        when(repository.findAdminUser("operator01")).thenReturn(Optional.empty());
        when(passwordHashingPort.hash(org.mockito.ArgumentMatchers.any(char[].class)))
                .thenReturn("{cpf-pbkdf2-sha256-v1}encoded");
        when(repository.insertAdminUser(any())).thenReturn(1);
        var request = new BzaOperationService.AdminUserRequest(
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
                eq("security-admin"), eq("ADMIN_USER_SAVE"), eq("bza_admin_user"), eq("operator01"),
                eq("신규 운영자 등록"), isNull(),
                argThat(after -> !String.valueOf(after).contains("encoded")));
    }

    @Test
    void createAdminUserRejectsRoleAutoGrant() {
        when(repository.findAdminUser("operator01")).thenReturn(Optional.empty());
        var request = new BzaOperationService.AdminUserRequest(
                "operator01", "업무 운영자", "BZA_OPERATOR", "Change-Me-1234!",
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
        var request = new BzaOperationService.AdminUserRequest(
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
        var request = new BzaOperationService.AdminUserRequest(
                "operator01", "업무 운영자", null, null,
                "SUSPENDED", "Y", "N", "N", 3L,
                "security-admin", "일시 정지");

        service.saveAdminUser(request, "security-admin");

        verify(authRepository).revokeAllRefreshTokensByLoginId("operator01");
    }

    @Test
    void saveRoleRevokesSessionsOfRoleHolders() {
        when(repository.findRole("BZA_OPERATOR")).thenReturn(Optional.empty());
        when(repository.insertRole(any())).thenReturn(1);
        var request = new BzaOperationService.RoleRequest(
                "BZA_OPERATOR", "업무 운영자", "N", "OWN", "Y", null,
                "security-admin", "Role 등록");

        service.saveRole(request, "security-admin");

        verify(authRepository).revokeRefreshTokensByRoleCode("BZA_OPERATOR");
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
        verify(repository, never()).insertPermission(any());
        verify(repository, never()).updatePermission(any());
    }
}
