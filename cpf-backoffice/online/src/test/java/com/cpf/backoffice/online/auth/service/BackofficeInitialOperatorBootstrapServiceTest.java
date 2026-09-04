package com.cpf.backoffice.online.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.backoffice.online.audit.service.BackofficeBusinessAuditService;
import com.cpf.backoffice.online.auth.repository.BackofficeAuthRepository;
import com.cpf.security.api.password.CpfPasswordEncoder;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BackofficeInitialOperatorBootstrapServiceTest {
    private final BackofficeAuthRepository repository = mock(BackofficeAuthRepository.class);
    private final BackofficeBusinessAuditService audit = mock(BackofficeBusinessAuditService.class);
    private final CpfPasswordEncoder passwordEncoder = mock(CpfPasswordEncoder.class);
    private BackofficeInitialOperatorBootstrapService service;

    @BeforeEach
    void setUp() {
        service = new BackofficeInitialOperatorBootstrapService(repository, audit, passwordEncoder);
        when(audit.withAuditChainLock(any())).thenAnswer(invocation -> {
            Supplier<?> work = invocation.getArgument(0);
            return work.get();
        });
    }

    @Test
    void freshEnvironmentFailsClosedWhenBootstrapSecretIsMissing() {
        when(repository.findBootstrapOperation(BackofficeInitialOperatorBootstrapService.INITIAL_OPERATION_ID))
                .thenReturn(java.util.Optional.empty());
        when(repository.hasAnyOperator()).thenReturn(false);

        assertThatThrownBy(() -> service.bootstrap(command(null)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("MBW_INITIAL_OPERATOR_BOOTSTRAP_SECRET_REQUIRED");

        verify(repository, never()).bootstrapOperator(any(), any(), any(), any(), any(), any());
        verify(audit, never()).record(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void existingOperatorNeverConsumesOrOverwritesBootstrapCredential() {
        when(repository.findBootstrapOperation(BackofficeInitialOperatorBootstrapService.INITIAL_OPERATION_ID))
                .thenReturn(java.util.Optional.empty());
        when(repository.hasAnyOperator()).thenReturn(true);

        BackofficeInitialOperatorBootstrapService.Result result = service.bootstrap(command("Strong!Password2026".toCharArray()));

        assertThat(result.status()).isEqualTo(BackofficeInitialOperatorBootstrapService.Status.EXISTING_OPERATOR);
        verify(repository, never()).bootstrapOperator(any(), any(), any(), any(), any(), any());
        verify(passwordEncoder, never()).encode(any(char[].class));
        verify(audit, never()).record(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void freshOperatorIsCreatedAndAuditedWithoutSecretPayload() {
        char[] password = "Strong!Password2026".toCharArray();
        when(repository.findBootstrapOperation(BackofficeInitialOperatorBootstrapService.INITIAL_OPERATION_ID))
                .thenReturn(java.util.Optional.empty());
        when(repository.hasAnyOperator()).thenReturn(false);
        when(passwordEncoder.encode(any(char[].class))).thenReturn("encoded-password");
        when(repository.bootstrapOperator(
                eq("initial-operator"), eq("Initial Operator"), eq("encoded-password"), eq("MBW_MANAGER"),
                eq(BackofficeInitialOperatorBootstrapService.INITIAL_OPERATION_ID), any(Instant.class)))
                .thenReturn(new BackofficeAuthRepository.BootstrapResult(
                        7L, "initial-operator", BackofficeInitialOperatorBootstrapService.INITIAL_OPERATION_ID, true));

        BackofficeInitialOperatorBootstrapService.Result result = service.bootstrap(command(password));

        assertThat(result.status()).isEqualTo(BackofficeInitialOperatorBootstrapService.Status.CREATED);
        assertThat(password).containsOnly('\0');
        verify(audit).record(
                "CPF_BOOTSTRAP", "INITIAL_OPERATOR_BOOTSTRAP", "MBW_OPERATOR", "initial-operator",
                "initial operator bootstrap", null,
                java.util.Map.of(
                        "result", "CREATED",
                        "source", "CPF_BOOTSTRAP",
                        "environment", "test",
                        "profiles", List.of("test"),
                        "instanceId", "mbw-test-01",
                        "bootstrapSecretProvided", true));
    }

    private static BackofficeInitialOperatorBootstrapService.Command command(char[] password) {
        return new BackofficeInitialOperatorBootstrapService.Command(
                "initial-operator", "Initial Operator", "MBW_MANAGER", 90, password,
                "test", List.of("test"), "mbw-test-01");
    }
}
