package cpf.pfw.common.http;

import cpf.pfw.common.header.CpfHeaderPropagator;
import cpf.pfw.common.logging.file.CpfFileLogWriter;
import cpf.pfw.common.workflow.CpfWorkflowContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/**
 * RestClient 또는 RestTemplate 기반 외부 호출에 CPF 거래/워크플로 헤더를 자동 전파합니다.
 */
public class CpfRestClientInterceptor implements ClientHttpRequestInterceptor {
    private final CpfFileLogWriter fileLogWriter;

    public CpfRestClientInterceptor() {
        this(null);
    }

    public CpfRestClientInterceptor(CpfFileLogWriter fileLogWriter) {
        this.fileLogWriter = fileLogWriter;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {
        applyHeaders(request.getHeaders());
        long started = System.nanoTime();
        writeEvent(request, "OUTBOUND_REQUEST", null, "REQUESTED", null, null, started);
        try {
            ClientHttpResponse response = execution.execute(request, body);
            int statusCode = response.getStatusCode().value();
            writeEvent(
                    request,
                    statusCode >= 400 ? "OUTBOUND_RESPONSE_ERROR" : "OUTBOUND_RESPONSE",
                    statusCode,
                    statusCode >= 400 ? "FAILED" : "SUCCESS",
                    statusCode >= 400 ? "HTTP_" + statusCode : null,
                    statusCode >= 400 ? "하위 서비스 HTTP 오류" : null,
                    started);
            return response;
        } catch (IOException ex) {
            writeEvent(request, "OUTBOUND_EXCEPTION", 0, "FAILED", ex.getClass().getSimpleName(), ex.getMessage(), started);
            throw ex;
        }
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

    private void writeEvent(
            HttpRequest request,
            String eventType,
            Integer httpStatus,
            String status,
            String failureCode,
            String failureMessage,
            long started) {

        if (fileLogWriter == null) {
            return;
        }
        fileLogWriter.writeIntegration(
                null,
                inferTargetModule(request),
                "OUTBOUND",
                request.getMethod().name(),
                request.getURI().getPath(),
                httpStatus,
                status,
                (System.nanoTime() - started) / 1_000_000,
                failureCode,
                failureMessage,
                Map.of(
                        "eventType", eventType,
                        "endpointCode", request.getURI().getHost() + ":" + request.getURI().getPort(),
                        "timeoutYn", "N",
                        "retryCount", 0,
                        "requestHeadersMasked", request.getHeaders().toString()));
    }

    private String inferTargetModule(HttpRequest request) {
        String path = request.getURI().getPath() == null ? "" : request.getURI().getPath().toLowerCase(Locale.ROOT);
        int port = request.getURI().getPort();
        if (path.contains("/mbr/") || port == 8081) {
            return "MBR";
        }
        if (path.contains("/acc/") || port == 8080) {
            return "ACC";
        }
        if (path.contains("/adm/") || port == 8090) {
            return "ADM";
        }
        if (path.contains("/api/exs/") || port == 8092) {
            return "EXS";
        }
        return hasText(request.getURI().getHost()) ? request.getURI().getHost().toUpperCase(Locale.ROOT) : "UNKNOWN";
    }
}
