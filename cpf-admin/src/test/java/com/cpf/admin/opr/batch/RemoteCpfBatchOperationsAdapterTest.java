package com.cpf.admin.opr.batch;

import com.cpf.admin.opr.context.AdmAuthenticatedOperatorContext;
import com.cpf.integration.api.servicecall.CpfServiceCaller;
import com.cpf.integration.api.servicecall.CpfServiceRequest;
import com.cpf.integration.api.servicecall.CpfServiceResult;
import com.cpf.integration.api.servicecall.CpfServiceTarget;
import com.cpf.integration.api.servicecall.CpfServiceTransport;
import com.cpf.web.api.CpfHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RemoteCpfBatchOperationsAdapterTest {
    @Test
    void readUsesAuthenticatedOperatorContextAndStandardCallerHeaders() {
        AtomicReference<ClientRequest> httpRequest = new AtomicReference<>();
        AtomicReference<CpfServiceRequest> serviceRequest = new AtomicReference<>();
        AdmAuthenticatedOperatorContext operatorContext =
                mock(AdmAuthenticatedOperatorContext.class);
        when(operatorContext.currentOperatorId()).thenReturn("operator-read");
        RemoteCpfBatchOperationsAdapter adapter = new RemoteCpfBatchOperationsAdapter(
                successCaller(serviceRequest),
                webClient(httpRequest, "[{\"jobId\":\"JOB-1\"}]"),
                operatorContext,
                "adm-instance-01");

        assertThat(adapter.findJobs()).singleElement()
                .satisfies(row -> assertThat(row).containsEntry("jobId", "JOB-1"));

        assertCallerHeaders(httpRequest.get().headers(), "operator-read");
        assertThat(serviceRequest.get().headers())
                .containsEntry(CpfHeaders.callerService(), "ADM")
                .containsEntry(CpfHeaders.callerInstanceId(), "adm-instance-01")
                .containsEntry(CpfHeaders.operatorId(), "operator-read");
        verify(operatorContext).currentOperatorId();
    }

    @Test
    void mutationUsesExplicitVerifiedRequestUserInsteadOfAmbientReadContext() {
        AtomicReference<ClientRequest> httpRequest = new AtomicReference<>();
        AtomicReference<CpfServiceRequest> serviceRequest = new AtomicReference<>();
        AdmAuthenticatedOperatorContext operatorContext =
                mock(AdmAuthenticatedOperatorContext.class);
        RemoteCpfBatchOperationsAdapter adapter = new RemoteCpfBatchOperationsAdapter(
                successCaller(serviceRequest),
                webClient(httpRequest, "{\"accepted\":true}"),
                operatorContext,
                "adm-instance-01");

        assertThat(adapter.requestRun(
                "JOB-1",
                "{}",
                "operator-mutation",
                "운영 실행"))
                .containsEntry("accepted", true);

        assertCallerHeaders(httpRequest.get().headers(), "operator-mutation");
        assertThat(serviceRequest.get().headers())
                .containsEntry(CpfHeaders.operatorId(), "operator-mutation");
        verifyNoInteractions(operatorContext);
    }

    private void assertCallerHeaders(HttpHeaders headers, String operatorId) {
        assertThat(headers.getFirst(CpfHeaders.callerService())).isEqualTo("ADM");
        assertThat(headers.getFirst(CpfHeaders.callerInstanceId())).isEqualTo("adm-instance-01");
        assertThat(headers.getFirst(CpfHeaders.operatorId())).isEqualTo(operatorId);
    }

    private WebClient.Builder webClient(
            AtomicReference<ClientRequest> captured,
            String responseBody) {
        return WebClient.builder().exchangeFunction(request -> {
            captured.set(request);
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(responseBody)
                    .build());
        });
    }

    private CpfServiceCaller successCaller(
            AtomicReference<CpfServiceRequest> captured) {
        CpfServiceCaller caller = mock(CpfServiceCaller.class);
        when(caller.invoke(
                any(CpfServiceRequest.class),
                org.mockito.ArgumentMatchers.<CpfServiceTransport<Object>>any()))
                .thenAnswer(invocation -> {
                    CpfServiceRequest request = invocation.getArgument(0);
                    CpfServiceTransport<Object> transport = invocation.getArgument(1);
                    captured.set(request);
                    CpfServiceTarget target = new CpfServiceTarget(
                            Map.of("serviceId", "BAT"),
                            Map.of("endpointCode", "SBATOP0001"),
                            Map.of("instanceId", "bat-01"),
                            Map.of(),
                            "http://bat.example",
                            "SINGLE");
                    Object body = transport.exchange(target);
                    return new CpfServiceResult<>(
                            "SUCCESS",
                            target,
                            body,
                            200,
                            1L,
                            1,
                            null,
                            null);
                });
        return caller;
    }
}
