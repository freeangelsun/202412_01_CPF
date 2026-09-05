package com.cpf.admin.opr.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.admin.config.AdmBootstrapProperties;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

/** 모든 profile에서 동일한 ADM 최초 운영자 계약을 유지한다. */
class AdmBootstrapInitializerTest {

    @Test
    void existingOperatorSkipsBootstrapWithoutOverwritingCredential() {
        AdmOperatorService operatorService = mock(AdmOperatorService.class);
        AdmAuditLogService audit = mock(AdmAuditLogService.class);
        when(operatorService.hasAnyOperator()).thenReturn(true);

        initializer(properties(), operatorService, audit, "Strong!Password2026").run(arguments());

        verify(operatorService, never()).bootstrapOperator(any(), any(), any());
        verify(audit, never()).record(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void freshEnvironmentFailsClosedWithoutSecret() {
        AdmOperatorService operatorService = mock(AdmOperatorService.class);
        AdmAuditLogService audit = mock(AdmAuditLogService.class);
        when(operatorService.hasAnyOperator()).thenReturn(false);

        assertThatThrownBy(() -> initializer(properties(), operatorService, audit, null).run(arguments()))
                .isInstanceOf(CpfValidationException.class)
                .hasMessage("ADM_INITIAL_OPERATOR_BOOTSTRAP_SECRET_REQUIRED");

        verify(operatorService, never()).bootstrapOperator(any(), any(), any());
    }

    @Test
    void freshEnvironmentCreatesAndAuditsInitialOperator() {
        String password = "Strong!Password2026";
        AdmOperatorService operatorService = mock(AdmOperatorService.class);
        AdmAuditLogService audit = mock(AdmAuditLogService.class);
        when(operatorService.hasAnyOperator()).thenReturn(false, true);
        when(operatorService.bootstrapOperator("initial-adm", "Initial ADM Operator", password)).thenReturn(true);

        initializer(properties(), operatorService, audit, password).run(arguments());

        verify(operatorService).bootstrapOperator("initial-adm", "Initial ADM Operator", password);
        verify(audit).record(
                eq(null), eq("CPF_BOOTSTRAP"), eq("INITIAL_OPERATOR_BOOTSTRAP"), eq("ADM_OPERATOR"),
                eq("initial-adm"), eq("initial operator bootstrap"), eq(null),
                eq("{\"result\":\"CREATED\",\"source\":\"CPF_BOOTSTRAP\",\"bootstrapSecretProvided\":true}"),
                eq(null), eq("CPF_BOOTSTRAP"));
    }

    private static AdmBootstrapInitializer initializer(
            AdmBootstrapProperties properties,
            AdmOperatorService operatorService,
            AdmAuditLogService audit,
            String password) {
        return new AdmBootstrapInitializer(properties, operatorService, audit, () -> password,
                bootstrapContextFactory());
    }

    /** 최초 운영자 생성은 감사 기록을 남기므로 관리 실행 Context 안에서 돌아야 한다. */
    private static CpfContextExecutionFactory bootstrapContextFactory() {
        Clock clock = Clock.systemUTC();
        return new CpfContextExecutionFactory(
                () -> "TX-" + UUID.randomUUID(),
                new com.cpf.foundation.id.spi.CpfExecutionIdGenerator() {
                    @Override public String newExecutionId() { return "EX-" + UUID.randomUUID(); }
                    @Override public String newSegmentId() { return "SG-" + UUID.randomUUID(); }
                },
                () -> LocalDate.now(clock),
                clock);
    }

    private static DefaultApplicationArguments arguments() {
        return new DefaultApplicationArguments();
    }

    private static AdmBootstrapProperties properties() {
        AdmBootstrapProperties properties = new AdmBootstrapProperties();
        properties.setOperatorId("initial-adm");
        properties.setOperatorName("Initial ADM Operator");
        return properties;
    }
}
