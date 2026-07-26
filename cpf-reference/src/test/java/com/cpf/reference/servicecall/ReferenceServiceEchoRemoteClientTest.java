package com.cpf.reference.servicecall;

import com.cpf.core.common.http.CpfWebClient;
import com.cpf.core.common.servicecall.CpfServiceCallOptions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReferenceServiceEchoRemoteClientTest {

    @Test
    @SuppressWarnings("unchecked")
    void callsOnlyTheNeutralRefSimulator() {
        CpfWebClient webClient = mock(CpfWebClient.class);
        ArgumentCaptor<Function<UriBuilder, URI>> uriFunction = ArgumentCaptor.forClass(Function.class);
        when(webClient.get(eq("REF-EXTERNAL-SIMULATOR"), any(Function.class), eq(Map.class)))
                .thenReturn(Map.of(
                        "externalKey", "REF-REQUEST-1",
                        "status", 200,
                        "processedAt", "2026-07-27T00:00:00Z"));

        ReferenceServiceEchoResponse response = new ReferenceServiceEchoRemoteClient(webClient).execute(
                new ReferenceServiceEchoRequest("REF-REQUEST-1"),
                CpfServiceCallOptions.defaultQuery());

        verify(webClient).get(
                eq("REF-EXTERNAL-SIMULATOR"),
                uriFunction.capture(),
                eq(Map.class));
        URI uri = uriFunction.getValue().apply(UriComponentsBuilder.newInstance());
        assertThat(uri.toString()).contains(
                "/api/reference/external-simulator/response",
                "externalKey=REF-REQUEST-1",
                "status=200",
                "delayMillis=0");
        assertThat(response)
                .extracting(
                        ReferenceServiceEchoResponse::requestKey,
                        ReferenceServiceEchoResponse::statusCode)
                .containsExactly("REF-REQUEST-1", "200");
    }
}
