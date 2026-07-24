package com.cpf.bizadmin.operation.service;

import com.cpf.bizadmin.operation.repository.BzaOperationRepository;
import com.cpf.core.common.exception.CpfValidationException;
import com.cpf.core.common.security.password.CpfPasswordHashingPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BzaOperationServiceTest {

    private final BzaOperationRepository repository = mock(BzaOperationRepository.class);
    private final CpfPasswordHashingPort passwordHashingPort = mock(CpfPasswordHashingPort.class);
    private final BzaOperationService service = new BzaOperationService(repository, passwordHashingPort);

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
        verify(repository).insertBusinessAudit(org.mockito.ArgumentMatchers.argThat(values ->
                !String.valueOf(values.get("afterData")).contains("encoded")
                        && "security-admin".equals(values.get("actorId"))
                        && "신규 운영자 등록".equals(values.get("reason"))));
    }

    @Test
    void savePermissionRejectsUnknownHttpMethodBeforeWrite() {
        var request = new BzaOperationService.PermissionRequest(
                "BZA_OPERATOR", "SETTING", "EXECUTE", "API", "TRACE",
                "/api/bza/settings/**", "BZA", "ALL", "ROLE", "Y", "Y",
                "security-admin", "설정 실행 권한 등록");

        assertThatThrownBy(() -> service.savePermission(request, "security-admin"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("HTTP 메서드");
        verify(repository, never()).savePermission(org.mockito.ArgumentMatchers.any());
    }

}
