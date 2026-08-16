package com.cpf.education.integration.external;

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

class EducationExternalIntegrationEducationSampleTest {
    @Test
    @SuppressWarnings("unchecked")
    void 공개HttpApi에업무입력만전달한다() {
        CpfHttpClient httpClient = mock(CpfHttpClient.class);
        ArgumentCaptor<Function<UriBuilder, URI>> uriFunction = ArgumentCaptor.forClass(Function.class);
        when(httpClient.get(eq("EDU-EXTERNAL-SIMULATOR"), any(Function.class), eq(Map.class)))
                .thenReturn(Map.of("status", 200));

        Map<String, Object> result = new EducationExternalIntegrationEducationSample(httpClient)
                .call("ORDER-1", 200, 0);

        verify(httpClient).get(eq("EDU-EXTERNAL-SIMULATOR"), uriFunction.capture(), eq(Map.class));
        URI uri = uriFunction.getValue().apply(UriComponentsBuilder.newInstance());
        assertThat(result).containsEntry("status", 200);
        assertThat(uri.toString()).contains("status=200", "delayMillis=0", "externalKey=ORDER-1");
    }
}
