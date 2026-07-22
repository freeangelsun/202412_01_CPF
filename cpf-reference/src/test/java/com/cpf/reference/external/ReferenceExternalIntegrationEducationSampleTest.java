package com.cpf.reference.external;

import com.cpf.core.common.http.CpfWebClient;
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

class ReferenceExternalIntegrationEducationSampleTest {
    @Test
    @SuppressWarnings("unchecked")
    void 업무입력만전달하고복원력정책은중앙기본값을사용한다() {
        CpfWebClient webClient = mock(CpfWebClient.class);
        ArgumentCaptor<Function<UriBuilder, URI>> uriFunction = ArgumentCaptor.forClass(Function.class);
        when(webClient.get(eq("REF-EXTERNAL-SIMULATOR"), any(Function.class), eq(Map.class)))
                .thenReturn(Map.of("status", 200));

        Map<String, Object> result = new ReferenceExternalIntegrationEducationSample(webClient)
                .call("ORDER-1", 200, 0);

        verify(webClient).get(eq("REF-EXTERNAL-SIMULATOR"), uriFunction.capture(), eq(Map.class));
        URI uri = uriFunction.getValue().apply(UriComponentsBuilder.newInstance());
        assertThat(result).containsEntry("status", 200);
        assertThat(uri.toString()).contains("status=200", "delayMillis=0", "externalKey=ORDER-1");
    }
}
