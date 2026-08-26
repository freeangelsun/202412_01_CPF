package com.cpf.integration.api.domaincall;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.core.api.domain.CpfDomainBinding;
import com.cpf.core.api.domain.CpfDomainBindingMode;
import com.cpf.core.api.domain.CpfDomainPingRequest;
import com.cpf.core.api.domain.CpfDomainPingResponse;
import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.core.api.result.CpfResult;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CpfDomainClientRouterTest {

    @Test
    void autoUsesLocalOperationWithoutChangingBusinessClient() throws Exception {
        var registry = registry(true, "LOCAL");
        var router = new CpfDomainClientRouter(code -> CpfDomainBinding.auto("EXS-SERVICE"), registry,
                remoteSuccess());

        CpfResult<CpfDomainPingResponse> result;
        try (AutoCloseable _ = CpfContexts.bind(rootContext())) {
            result = router.invoke("EXS", "ping", new CpfDomainPingRequest("R1"), CpfDomainPingResponse.class);
        }

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.requireData().systemCode()).isEqualTo("LOCAL");
        assertThat(CpfContexts.current()).isNull();
    }

    @Test
    void localOperationWithoutCanonicalContextFailsClosed() {
        var router = new CpfDomainClientRouter(code -> CpfDomainBinding.auto("EXS-SERVICE"),
                registry(true, "LOCAL"), remoteMustNotRun());

        CpfResult<CpfDomainPingResponse> result = router.invoke(
                "EXS", "ping", new CpfDomainPingRequest("R-NO-CONTEXT"), CpfDomainPingResponse.class);

        assertThat(result.isTechnicalFailure()).isTrue();
        assertThat(result.errorCode()).isEqualTo("CPF-DOMAIN-CONTEXT-MISSING");
    }

    @Test
    void remoteBindingUsesSameTypedClientContract() {
        var registry = registry(true, "LOCAL");
        var router = new CpfDomainClientRouter(code -> new CpfDomainBinding(CpfDomainBindingMode.REMOTE, "EXS-SERVICE"), registry,
                remoteSuccess());

        CpfResult<CpfDomainPingResponse> result = router.invoke("EXS", "ping", new CpfDomainPingRequest("R2"), CpfDomainPingResponse.class);

        assertThat(result.requireData().systemCode()).isEqualTo("EXS");
    }

    @Test
    void localBindingFailsClosedWhenManagedOperationIsMissing() {
        var router = new CpfDomainClientRouter(code -> new CpfDomainBinding(CpfDomainBindingMode.LOCAL, null), registry(false, "LOCAL"),
                remoteMustNotRun());

        CpfResult<CpfDomainPingResponse> result = router.invoke("EXS", "ping", new CpfDomainPingRequest("R3"), CpfDomainPingResponse.class);

        assertThat(result.isTechnicalFailure()).isTrue();
        assertThat(result.errorCode()).isEqualTo("CPF-DOMAIN-LOCAL-NOT-FOUND");
    }

    private static CpfDomainRemoteTransport remoteSuccess() {
        return new CpfDomainRemoteTransport() {
            @Override
            public <I extends com.cpf.core.api.base.CpfRequest, O extends com.cpf.core.api.base.CpfResponse> CpfResult<O> invoke(
                    String systemCode, String operationId, CpfDomainBinding binding, I request, Class<O> responseType) {
                CpfDomainPingRequest ping = (CpfDomainPingRequest) request;
                return CpfResult.success(responseType.cast(new CpfDomainPingResponse(systemCode, ping.requestId(), Instant.EPOCH)));
            }
        };
    }

    private static CpfDomainRemoteTransport remoteMustNotRun() {
        return new CpfDomainRemoteTransport() {
            @Override
            public <I extends com.cpf.core.api.base.CpfRequest, O extends com.cpf.core.api.base.CpfResponse> CpfResult<O> invoke(
                    String systemCode, String operationId, CpfDomainBinding binding, I request, Class<O> responseType) {
                throw new AssertionError("remote must not run");
            }
        };
    }

    private static CpfContextSnapshot rootContext() {
        Instant now = Instant.parse("2026-08-22T00:00:00Z");
        CpfContext root = new CpfContext(
                new CpfContext.CpfTransactionContext(
                        "TX-DOMAIN-ROUTER-1", "TX-DOMAIN-ROUTER-1", null, null, null,
                        "MBR", "MBR", null, null,
                        "INTERNAL", "INTERNAL", null, null,
                        LocalDate.of(2026, 8, 22), now, CpfContext.CpfTransactionOriginKind.INTERNAL,
                        "MBR", null),
                new CpfContext.CpfExecutionContext(
                        null, "EX-DOMAIN-ROUTER-1", "EX-DOMAIN-ROUTER-1", null,
                        "SG-DOMAIN-ROUTER-1", null, CpfContext.CpfExecutionType.INTERNAL,
                        1, 0, now, null, CpfContext.CpfCancellationMode.DEADLINE_ENFORCED),
                null, null, null);
        return CpfContextSnapshot.capture(root, now);
    }

    private static CpfDomainOperationRegistry registry(boolean available, String responseSystemCode) {
        return new CpfDomainOperationRegistry() {
            @Override public boolean has(String systemCode, String operationId) { return available; }
            @Override 
            public <I extends com.cpf.core.api.base.CpfRequest, O extends com.cpf.core.api.base.CpfResponse> CpfResult<O> invoke(
                    CpfDomainOperationRegistry.InvocationMetadata metadata,
                    String systemCode, String operationId, I request, Class<O> responseType) {
                CpfDomainPingRequest ping = (CpfDomainPingRequest) request;
                return CpfResult.success(responseType.cast(new CpfDomainPingResponse(responseSystemCode, ping.requestId(), Instant.EPOCH)));
            }
        };
    }
}
