package com.cpf.education.integration.servicecall;

import com.cpf.integration.http.api.CpfHttpClient;
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

class EducationServiceEchoRemoteClientTest {
    @Test
    @SuppressWarnings("unchecked")
    void publicHttpApi로중립시뮬레이터를호출한다() {
        CpfHttpClient httpClient = mock(CpfHttpClient.class);
        ArgumentCaptor<Function<UriBuilder, URI>> uriFunction = ArgumentCaptor.forClass(Function.class);
        when(httpClient.get(eq("EDU-EXTERNAL-SIMULATOR"), any(Function.class), eq(Map.class)))
                .thenReturn(Map.of("externalKey", "EDU-REQUEST-1", "status", 200,
                        "processedAt", "2026-07-27T00:00:00Z"));

        EducationServiceEchoResponse response = new EducationServiceEchoRemoteClient(httpClient)
                .execute(new EducationServiceEchoRequest("EDU-REQUEST-1"));

        verify(httpClient).get(eq("EDU-EXTERNAL-SIMULATOR"), uriFunction.capture(), eq(Map.class));
        URI uri = uriFunction.getValue().apply(UriComponentsBuilder.newInstance());
        assertThat(uri.toString()).contains("/api/education/external-simulator/response",
                "externalKey=EDU-REQUEST-1", "status=200", "delayMillis=0");
        assertThat(response)
                .extracting(EducationServiceEchoResponse::requestKey, EducationServiceEchoResponse::statusCode)
                .containsExactly("EDU-REQUEST-1", "200");
    }
}
