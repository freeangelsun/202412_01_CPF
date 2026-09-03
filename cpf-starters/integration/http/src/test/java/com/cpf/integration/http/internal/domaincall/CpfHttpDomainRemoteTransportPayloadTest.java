package com.cpf.integration.http.internal.domaincall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.core.api.domain.CpfDomainBinding;
import com.cpf.core.api.domain.CpfDomainBindingMode;
import com.cpf.core.api.result.CpfResultStatus;
import com.cpf.integration.api.domaincall.CpfDomainPayload;
import com.cpf.integration.http.internal.CpfWebClient;
import com.cpf.integration.http.internal.servicecall.ServiceCallRequest;
import com.cpf.integration.http.internal.servicecall.ServiceCallResult;
import com.cpf.web.context.CpfHeaderPolicyRegistry;
import com.cpf.web.context.CpfHttpHeaderNames;
import com.cpf.web.context.CpfHttpOutboundContextAdapter;
import com.cpf.web.context.CpfRuntimeIdentity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CpfHttpDomainRemoteTransportPayloadTest {
    @Test
    void sendsDynamicPayloadAsTheActualBusinessJsonObjectAndWrapsResponse() throws Exception {
        CpfWebClient webClient = mock(CpfWebClient.class);
        CpfHttpOutboundContextAdapter outbound = new CpfHttpOutboundContextAdapter(
                new CpfRuntimeIdentity("BAT", "cpf-batch-center-cut", "center-cut-1"),
                new CpfHeaderPolicyRegistry(null));
        when(webClient.postResult(any(ServiceCallRequest.class), any(), eq(CpfDomainRemoteEnvelope.class)))
                .thenReturn(ServiceCallResult.success(null,
                        new CpfDomainRemoteEnvelope(CpfResultStatus.SUCCESS,
                                Map.of("sampleKey", "member-1", "persisted", true),
                                null, null, null, null), 200, 1L));
        var transport = new CpfHttpDomainRemoteTransport(webClient, new ObjectMapper(), outbound);

        CpfDomainPayload response;
        try (AutoCloseable _ = CpfContexts.bind(rootContext())) {
            response = transport.invoke("MBR", "MBR_SAMPLE_TX_CREATE",
                    new CpfDomainBinding(CpfDomainBindingMode.REMOTE, "MBR-SERVICE"),
                    new CpfDomainPayload(Map.of("sampleKey", "member-1")),
                    CpfDomainPayload.class).requireData();
        }

        ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<ServiceCallRequest> call = ArgumentCaptor.forClass(ServiceCallRequest.class);
        verify(webClient).postResult(call.capture(), body.capture(),
                eq(CpfDomainRemoteEnvelope.class));
        assertThat(body.getValue()).isEqualTo(Map.of("sampleKey", "member-1"));
        assertThat(call.getValue().headers())
                .containsEntry(CpfHttpHeaderNames.TRANSACTION_ID, "20260824144500000BATCENTER00000001")
                .containsEntry(CpfHttpHeaderNames.ORIGINAL_SYSTEM_CODE, "BAT")
                .containsEntry(CpfHttpHeaderNames.SYSTEM_CODE, "MBR")
                .containsEntry(CpfHttpHeaderNames.CALLER_SYSTEM_CODE, "BAT")
                .containsEntry(CpfHttpHeaderNames.TARGET_SYSTEM_CODE, "MBR")
                .containsEntry(CpfHttpHeaderNames.TARGET_OPERATION_ID, "MBR_SAMPLE_TX_CREATE");
        // The HTTP protocol and caller-side Service Call Segment must use one immediate
        // caller identity.  Without this attribute the engine silently falls back to "CPF",
        // breaking the persisted BAT -> MBR transaction lineage.
        assertThat(call.getValue().attributes()).containsEntry("sourceModuleCode", "BAT");
        assertThat(response.values()).containsEntry("sampleKey", "member-1")
                .containsEntry("persisted", true);
    }

    private static CpfContextSnapshot rootContext() {
        Instant now = Instant.parse("2026-08-24T00:00:00Z");
        return CpfContextSnapshot.capture(new CpfContext(
                new CpfContext.CpfTransactionContext(
                        "20260824144500000BATCENTER00000001", "20260824144500000BATCENTER00000001", null, null, null,
                        "BAT", "BAT", null, "BAT",
                        null, null, null, null, LocalDate.of(2026, 8, 24), now,
                        CpfContext.CpfTransactionOriginKind.INTERNAL, "BAT", null),
                new CpfContext.CpfExecutionContext(
                        null, "EX-CENTER-CUT-1", "EX-CENTER-CUT-1", null,
                        "SG-CENTER-CUT-1", null, CpfContext.CpfExecutionType.BATCH,
                        1, 0, now, null, CpfContext.CpfCancellationMode.DEADLINE_ENFORCED),
                null, null, null), now);
    }
}
