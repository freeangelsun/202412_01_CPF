package com.cpf.integration.http.internal;

import com.cpf.platform.operations.observability.api.logging.CpfIntegrationLogPort;
import com.cpf.web.context.CpfHttpHeaderLogSanitizer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 * RestClient 또는 RestTemplate 기반 호출을 기록합니다. CPF 내부 거래 Header는 generic client에서 자동 전파하지 않습니다.
 */
public class CpfRestClientInterceptor implements ClientHttpRequestInterceptor {
    private final CpfIntegrationLogPort fileLogWriter;
    private final CpfLocalServiceIdentity localServiceIdentity;
    private final CpfHttpHeaderLogSanitizer headerSanitizer;

    public CpfRestClientInterceptor() { this(null, null, null); }

    public CpfRestClientInterceptor(CpfIntegrationLogPort fileLogWriter) { this(fileLogWriter, null, null); }

    public CpfRestClientInterceptor(
            CpfIntegrationLogPort fileLogWriter,
            CpfLocalServiceIdentity localServiceIdentity) {
        this(fileLogWriter, localServiceIdentity, null);
    }

    public CpfRestClientInterceptor(
            CpfIntegrationLogPort fileLogWriter,
            CpfLocalServiceIdentity localServiceIdentity,
            CpfHttpHeaderLogSanitizer headerSanitizer) {
        this.fileLogWriter = fileLogWriter;
        this.localServiceIdentity = localServiceIdentity;
        this.headerSanitizer = headerSanitizer == null ? new CpfHttpHeaderLogSanitizer(null) : headerSanitizer;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {
        applyHeaders(request.getHeaders(), localServiceIdentity);
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

    /**
     * Generic RestClient/RestTemplate은 외부기관 호출에도 사용되므로 CPF 내부 거래 Header를 자동 복사하지 않습니다.
     * 내부 Domain-to-Domain 호출은 CpfHttpOutboundContextAdapter가 명시적으로 구성한 Header만 사용합니다.
     */
    public static void applyHeaders(HttpHeaders headers, CpfLocalServiceIdentity localServiceIdentity) {
        // Intentionally no-op: trust-boundary aware propagation belongs to the typed internal Domain adapter.
    }

    public static void applyHeaders(HttpHeaders headers) {
        applyHeaders(headers, null);
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
                CpfTargetSystemResolver.resolve(request.getHeaders(), request.getURI()),
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
                        "requestHeadersMasked", headerSanitizer.sanitize(request.getHeaders())));
    }

}
