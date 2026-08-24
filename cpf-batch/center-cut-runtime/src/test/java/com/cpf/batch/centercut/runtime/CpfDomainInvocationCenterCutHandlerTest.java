package com.cpf.batch.centercut.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.batch.spi.CenterCutHandler;
import com.cpf.core.api.base.CpfRequest;
import com.cpf.core.api.base.CpfResponse;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.core.api.domain.CpfDomainBinding;
import com.cpf.core.api.domain.CpfDomainBindingMode;
import com.cpf.core.api.result.CpfRecoveryInfo;
import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.domaincall.CpfDomainClientRouter;
import com.cpf.integration.api.domaincall.CpfDomainOperationRegistry;
import com.cpf.integration.api.domaincall.CpfDomainPayload;
import com.cpf.integration.api.domaincall.CpfDomainRemoteTransport;
import com.cpf.testkit.context.CpfContextTestSupport;
import com.cpf.testkit.context.CpfTestContextRuntime;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CpfDomainInvocationCenterCutHandlerTest {
    private static CpfTestContextRuntime contextRuntime;
    private final CpfContextTestSupport contexts =
            new CpfContextTestSupport("CENTER-CUT-DOMAIN", LocalDate.of(2026, 8, 24));

    @BeforeAll static void installContextRuntime() { contextRuntime = CpfTestContextRuntime.install(); }
    @AfterAll static void closeContextRuntime() { contextRuntime.close(); }
    @AfterEach void assertContextClear() { contexts.assertClear(); }

    @Test
    void invokesActualWorkThroughOfficialRouterAndPreservesTargetOperationContext() throws Exception {
        AtomicReference<String> targetOperation = new AtomicReference<>();
        CpfDomainRemoteTransport transport = new CpfDomainRemoteTransport() {
            @Override
            @SuppressWarnings("unchecked")
            public <I extends CpfRequest, O extends CpfResponse> CpfResult<O> invoke(
                    String systemCode, String operationId, CpfDomainBinding binding,
                    I request, Class<O> responseType) {
                targetOperation.set(CpfContexts.requireCurrent().operation().targetOperationId());
                CpfDomainPayload payload = (CpfDomainPayload) request;
                return CpfResult.success((O) new CpfDomainPayload(Map.of(
                        "sampleKey", payload.values().get("sampleKey"), "persisted", true)));
            }
        };
        CpfDomainClientRouter router = new CpfDomainClientRouter(
                ignored -> new CpfDomainBinding(CpfDomainBindingMode.REMOTE, "MBR-SERVICE"),
                unavailableRegistry(), transport);
        var handler = new CpfDomainInvocationCenterCutHandler(router, new ObjectMapper());

        CenterCutHandler.Result result;
        try (AutoCloseable ignored = contexts.bindRoot("center-cut-domain", null, null)) {
            result = handler.handle(context("""
                    {"systemCode":"MBR","operationId":"MBR_SAMPLE_TX_CREATE","request":{
                      "sampleKey":"mbr-cc-1","itemName":"Center Cut","idempotencyKey":"idem-cc-1"}}
                    """));
        }

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.payload()).contains("\"persisted\":true").contains("mbr-cc-1");
        assertThat(targetOperation).hasValue("MBR_SAMPLE_TX_CREATE");
    }

    @Test
    void keepsTechnicalAndUnknownResultsDistinctForRecovery() {
        assertThat(handler(CpfResult.technicalFailure("DOWNSTREAM_UNAVAILABLE", "down"))
                .handle(context(validPayload())))
                .extracting(CenterCutHandler.Result::status, CenterCutHandler.Result::retryable)
                .containsExactly("RETRY", true);
        assertThat(handler(CpfResult.unknown("DOMAIN_TIMEOUT", "timeout",
                new CpfRecoveryInfo("recovery-1", "PROBE")))
                .handle(context(validPayload())))
                .extracting(CenterCutHandler.Result::status, CenterCutHandler.Result::compensationRequired)
                .containsExactly("UNKNOWN_RESULT", true);
    }

    private static CpfDomainInvocationCenterCutHandler handler(CpfResult<CpfDomainPayload> result) {
        CpfDomainRemoteTransport transport = new CpfDomainRemoteTransport() {
            @Override @SuppressWarnings("unchecked")
            public <I extends CpfRequest, O extends CpfResponse> CpfResult<O> invoke(
                    String systemCode, String operationId, CpfDomainBinding binding,
                    I request, Class<O> responseType) { return (CpfResult<O>) result; }
        };
        return new CpfDomainInvocationCenterCutHandler(new CpfDomainClientRouter(
                ignored -> new CpfDomainBinding(CpfDomainBindingMode.REMOTE, "MBR-SERVICE"),
                unavailableRegistry(), transport), new ObjectMapper());
    }

    private static CpfDomainOperationRegistry unavailableRegistry() {
        return new CpfDomainOperationRegistry() {
            @Override public boolean has(String systemCode, String operationId) { return false; }
            @Override public <I extends CpfRequest, O extends CpfResponse> CpfResult<O> invoke(
                    InvocationMetadata metadata, String systemCode, String operationId,
                    I request, Class<O> responseType) { throw new AssertionError("local operation must not run"); }
        };
    }

    private static CenterCutHandler.Context context(String payload) {
        return new CenterCutHandler.Context("job", 1L, "business", payload,
                "transaction", "segment", 7L);
    }

    private static String validPayload() {
        return "{\"systemCode\":\"MBR\",\"operationId\":\"MBR_SAMPLE_TX_CREATE\",\"request\":{\"sampleKey\":\"a\"}}";
    }
}
