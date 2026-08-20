package com.cpf.backoffice.online.directory.service;

import com.cpf.backoffice.online.auth.repository.BackofficeAuthRepository;
import com.cpf.backoffice.online.directory.repository.BackofficeDirectoryRepository;
import com.cpf.core.api.error.CpfValidationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackofficeDirectoryServiceTest {
    private final BackofficeDirectoryRepository repository = mock(BackofficeDirectoryRepository.class);
    private final BackofficeAuthRepository authRepository = mock(BackofficeAuthRepository.class);
    private final BackofficeDirectoryService service = new BackofficeDirectoryService(repository, authRepository);

    @Test
    void userRoleGrantIsIdempotentByOperationId() {
        when(repository.findUserRoleByOperationId("op-role-1")).thenReturn(Optional.of(Map.of(
                "loginId", "operator01", "roleCode", "MBW_OPERATOR", "userRoleId", 10L)));
        var request = new BackofficeDirectoryService.UserRoleRequest(
                "operator01", "MBW_OPERATOR", Instant.now(), null, "N", "op-role-1", "Role 부여");

        Map<String,Object> result = service.saveUserRole(request, "security-admin");

        assertThat(result.get("idempotent")).isEqualTo(true);
        verify(repository, never()).insertUserRole(any(Long.class), any());
        verify(authRepository, never()).revokeAllRefreshTokens(any(Long.class));
    }

    @Test
    void successfulRoleGrantRevokesExistingRefreshSessions() {
        when(repository.findUserRoleByOperationId("op-role-2")).thenReturn(Optional.empty());
        when(repository.lockUserAndId("operator01")).thenReturn(100L);
        when(repository.insertUserRole(org.mockito.ArgumentMatchers.eq(100L), any())).thenReturn(1);
        var request = new BackofficeDirectoryService.UserRoleRequest(
                "operator01", "MBW_OPERATOR", Instant.now(), null, "N", "op-role-2", "Role 부여");

        service.saveUserRole(request, "security-admin");

        verify(authRepository).revokeAllRefreshTokens(100L);
    }

    @Test
    void operationIdCollisionWithDifferentRoleIsRejected() {
        when(repository.findUserRoleByOperationId("op-role-3")).thenReturn(Optional.of(Map.of(
                "loginId", "operator01", "roleCode", "MBW_VIEWER", "userRoleId", 11L)));
        var request = new BackofficeDirectoryService.UserRoleRequest(
                "operator01", "MBW_OPERATOR", Instant.now(), null, "N", "op-role-3", "Role 변경");

        assertThatThrownBy(() -> service.saveUserRole(request, "security-admin"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("operationId");
        verify(authRepository, never()).revokeAllRefreshTokens(any(Long.class));
    }
}
