package com.cpf.admin.opr.centercut;

import com.cpf.batch.api.BatControlHeaders;
import com.cpf.admin.opr.context.AdmAuthenticatedOperatorContext;
import com.cpf.integration.api.servicecall.CpfServiceCaller;
import com.cpf.integration.api.servicecall.CpfServiceRequest;
import com.cpf.integration.api.servicecall.CpfServiceResult;
import com.cpf.integration.api.servicecall.CpfServiceTarget;
import com.cpf.integration.api.servicecall.CpfServiceTransport;
import com.cpf.web.api.CpfHttpHeaders;
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
import static org.mockito.Mockito.when;

class RemoteCpfCenterCutOperationsAdapterTest {
    @Test
    void readUsesAuthenticatedOperatorContextAndStandardCallerHeaders() {
        AtomicReference<ClientRequest> httpRequest = new AtomicReference<>();
        AtomicReference<CpfServiceRequest> serviceRequest = new AtomicReference<>();
        AdmAuthenticatedOperatorContext operatorContext =
                mock(AdmAuthenticatedOperatorContext.class);
        when(operatorContext.currentOperatorId()).thenReturn("operator-center-cut");
        RemoteCpfCenterCutOperationsAdapter adapter =
                new RemoteCpfCenterCutOperationsAdapter(
                        successEngine(serviceRequest),
                        webClient(httpRequest),
                        operatorContext,
                        "adm-instance-02");

        assertThat(adapter.findJobs()).singleElement()
                .satisfies(row -> assertThat(row).containsEntry("centerCutJobId", "CC-1"));

        HttpHeaders headers = httpRequest.get().headers();
        assertThat(headers.getFirst(BatControlHeaders.CALLER_SERVICE)).isEqualTo("ADM");
        assertThat(headers.getFirst(BatControlHeaders.CALLER_INSTANCE_ID)).isEqualTo("adm-instance-02");
        assertThat(headers.getFirst(BatControlHeaders.OPERATOR_ID)).isEqualTo("operator-center-cut");
        assertThat(serviceRequest.get().headers())
                .containsEntry(BatControlHeaders.CALLER_SERVICE, "ADM")
                .containsEntry(BatControlHeaders.CALLER_INSTANCE_ID, "adm-instance-02")
                .containsEntry(BatControlHeaders.OPERATOR_ID, "operator-center-cut");
        verify(operatorContext).currentOperatorId();
    }

    private WebClient.Builder webClient(AtomicReference<ClientRequest> captured) {
        return WebClient.builder().exchangeFunction(request -> {
            captured.set(request);
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body("[{\"centerCutJobId\":\"CC-1\"}]")
                    .build());
        });
    }

    private CpfServiceCaller successEngine(
            AtomicReference<CpfServiceRequest> captured) {
        return new CpfServiceCaller() {
            @Override
            public <T> CpfServiceResult<T> invoke(
                    CpfServiceRequest request,
                    CpfServiceTransport<T> remoteCall) {
                captured.set(request);
                CpfServiceTarget target = new CpfServiceTarget(
                        Map.of("serviceId", "BAT"),
                        Map.of("endpointCode", "SBATCT0001"),
                        Map.of("instanceId", "bat-02"),
                        Map.of(),
                        "http://bat.example",
                        "SINGLE");
                return new CpfServiceResult<>(
                        "SUCCESS",
                        target,
                        remoteCall.exchange(target),
                        200,
                        1L,
                        1,
                        null,
                        null);
            }
        };
    }
}
