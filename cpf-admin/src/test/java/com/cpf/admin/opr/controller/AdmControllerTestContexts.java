package com.cpf.admin.opr.controller;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.foundation.time.spi.CpfBusinessDateProvider;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/** Canonical managed execution scope for direct ADM controller unit tests. */
final class AdmControllerTestContexts {
    private static final Instant NOW = Instant.parse("2026-08-22T00:00:00Z");

    private AdmControllerTestContexts() {
    }

    static AutoCloseable bind(String actorId) {
        CpfContextExecutionFactory factory = new CpfContextExecutionFactory(
                () -> "TX-ADM-CONTROLLER-TEST",
                new CpfExecutionIdGenerator() {
                    @Override public String newExecutionId() { return "EX-ADM-CONTROLLER-TEST"; }
                    @Override public String newSegmentId() { return "SG-ADM-CONTROLLER-TEST"; }
                },
                (CpfBusinessDateProvider) () -> LocalDate.of(2026, 8, 22),
                Clock.fixed(NOW, ZoneOffset.UTC));
        CpfContext context = factory.newRoot(
                null,
                "adm.controller.test",
                new CpfContext.CpfIdentityContext(
                        actorId, actorId, CpfContext.CpfPrincipalType.OPERATOR),
                null,
                NOW.plusSeconds(300));
        return CpfContexts.bind(CpfContextSnapshot.capture(context, NOW));
    }
}
