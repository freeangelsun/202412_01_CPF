package cpf.pfw.common.http;

import cpf.pfw.common.header.CpfHeaderPropagator;
import cpf.pfw.common.workflow.CpfWorkflowContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 * RestClient 또는 RestTemplate 기반 외부 호출에 CPF 거래/워크플로 헤더를 자동 전파합니다.
 */
public class CpfRestClientInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {
        applyHeaders(request.getHeaders());
        return execution.execute(request, body);
    }

    public static void applyHeaders(HttpHeaders headers) {
        for (Map.Entry<String, String> header : CpfHeaderPropagator.outboundHeaders().entrySet()) {
            if (hasText(header.getValue()) && !headers.containsKey(header.getKey())) {
                headers.add(header.getKey(), header.getValue());
            }
        }
        for (Map.Entry<String, String> header : CpfWorkflowContext.propagationHeaders().entrySet()) {
            if (hasText(header.getValue()) && !headers.containsKey(header.getKey())) {
                headers.add(header.getKey(), header.getValue());
            }
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
