package cpf.xyz.edu.external;

import cpf.pfw.common.http.CpfWebClient;
import cpf.pfw.common.servicecall.ServiceCallRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class XyzExternalIntegrationEducationSampleTest {
    @Test
    void 외부키와복원력정책을PFW표준요청으로전달한다() {
        CpfWebClient webClient = mock(CpfWebClient.class);
        ArgumentCaptor<ServiceCallRequest> request = ArgumentCaptor.forClass(ServiceCallRequest.class);
        when(webClient.get(request.capture(), eq(Map.class))).thenReturn(Map.of("status", 200));

        Map<String, Object> result = new XyzExternalIntegrationEducationSample(webClient)
                .call("ORDER-1", 200, 0);

        assertThat(result).containsEntry("status", 200);
        assertThat(request.getValue().timeoutMillis()).isEqualTo(1000);
        assertThat(request.getValue().retryCount()).isEqualTo(1);
        assertThat(request.getValue().attributes()).containsEntry("externalKey", "ORDER-1");
        verify(webClient).get(request.getValue(), Map.class);
    }
}
