package com.cpf.batch.control.centercut;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cpf.batch.api.CenterCutExecutionRequest;
import com.cpf.batch.runtime.CenterCutParameterProtector;
import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;
import com.cpf.testkit.context.CpfTestContextRuntime;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class CenterCutExecutionServiceContextTest {
    private static final String CURRENT_TX = "20260824144200000BATCONTROL0000001";
    private static final String GENERATED_TX = "20260824144201000BATCONTROL0000002";
    private static CpfTestContextRuntime contextRuntime;

    @BeforeAll static void installContextRuntime() { contextRuntime = CpfTestContextRuntime.install(); }
    @AfterAll static void closeContextRuntime() { contextRuntime.close(); }
    @AfterEach void assertContextClear() { assertThat(CpfContexts.current()).isNull(); }

    @Test
    void derivesLaunchLineageFromCurrentCpfContextWithoutDeveloperAssembly() throws Exception {
        CenterCutExecutionService service = service();
        try (AutoCloseable ignored = CpfContexts.bind(CpfContextSnapshot.capture(currentContext()))) {
            CenterCutExecutionService.LaunchContext resolved = service.resolveLaunchContext(request(null, null));
            assertThat(resolved.transactionId()).isEqualTo(CURRENT_TX);
            assertThat(resolved.parentSegmentId()).isEqualTo("parent-segment-current");
        }
    }

    @Test
    void generatesCanonicalLineageWhenManagementBoundaryHasNoBusinessContext() {
        CenterCutExecutionService.LaunchContext resolved = service().resolveLaunchContext(request(null, null));
        assertThat(resolved.transactionId()).isEqualTo(GENERATED_TX);
        assertThat(resolved.parentSegmentId()).isEqualTo("generated-parent-segment");
    }

    @Test
    void rejectsMalformedOrContextConflictingCallerValues() throws Exception {
        CenterCutExecutionService service = service();
        assertThatThrownBy(() -> service.resolveLaunchContext(request("BATCH", null)))
                .isInstanceOf(IllegalArgumentException.class);
        try (AutoCloseable ignored = CpfContexts.bind(CpfContextSnapshot.capture(currentContext()))) {
            assertThatThrownBy(() -> service.resolveLaunchContext(request(GENERATED_TX, null)))
                    .isInstanceOf(SecurityException.class)
                    .hasMessage("CENTER_CUT_TRANSACTION_CONTEXT_MISMATCH");
            assertThatThrownBy(() -> service.resolveLaunchContext(request(CURRENT_TX, "forged-segment")))
                    .isInstanceOf(SecurityException.class)
                    .hasMessage("CENTER_CUT_PARENT_SEGMENT_CONTEXT_MISMATCH");
        }
    }

    private static CenterCutExecutionService service() {
        CpfVendorSqlCatalogProvider catalogs = mock(CpfVendorSqlCatalogProvider.class);
        when(catalogs.forModule("bat")).thenReturn(mock(CpfVendorSqlCatalog.class));
        CpfTransactionIdGenerator transactionIds = () -> GENERATED_TX;
        CpfExecutionIdGenerator executionIds = new CpfExecutionIdGenerator() {
            @Override public String newExecutionId() { return "generated-execution"; }
            @Override public String newSegmentId() { return "generated-parent-segment"; }
        };
        return new CenterCutExecutionService(mock(JdbcTemplate.class), new ObjectMapper(),
                mock(CenterCutParameterProtector.class), catalogs, transactionIds, executionIds);
    }

    private static CenterCutExecutionRequest request(String transactionId, String parentSegmentId) {
        return new CenterCutExecutionRequest("job", "idem", Map.of(), "1", 1, 1,
                "operator", "reason", transactionId, parentSegmentId);
    }

    private static CpfContext currentContext() {
        Instant now = Instant.parse("2026-08-24T05:42:00Z");
        return new CpfContext(
                new CpfContext.CpfTransactionContext(
                        CURRENT_TX, CURRENT_TX, null, "correlation", "trace", "BAT", "BAT", null, "BAT",
                        null, null, null, null, LocalDate.of(2026, 8, 24), now,
                        CpfContext.CpfTransactionOriginKind.BATCH, "BAT", null),
                new CpfContext.CpfExecutionContext(
                        "center-cut", "execution-current", "execution-current", null,
                        "parent-segment-current", null, CpfContext.CpfExecutionType.BATCH,
                        1, 0, now, null, CpfContext.CpfCancellationMode.DEADLINE_ENFORCED),
                null, null, null);
    }
}
