package com.cpf.gateway.transport;

import com.cpf.core.api.servicecall.CpfServiceCallTransportException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** JDK HttpClient 기반 blocking-stream 전송 Adapter입니다. */
public final class JdkCpfGatewayHttpExchangeAdapter implements CpfGatewayHttpExchangePort {
    private static final Set<String> RESTRICTED = Set.of("host", "content-length", "connection", "expect", "upgrade");
    private final HttpClient client;

    public JdkCpfGatewayHttpExchangeAdapter(long connectTimeoutMillis) {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMillis))
                .followRedirects(HttpClient.Redirect.NEVER)
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    @Override
    public CpfGatewayProxyResponse exchange(
            URI uri,
            HttpMethod method,
            HttpHeaders headers,
            CpfGatewayReplayableBody body,
            long timeoutMillis) {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri).timeout(Duration.ofMillis(timeoutMillis));
        headers.forEach((name, values) -> copyHeader(request, name, values));
        HttpRequest.BodyPublisher publisher;
        if (body.length() == 0L) {
            publisher = HttpRequest.BodyPublishers.noBody();
        } else {
            HttpRequest.BodyPublisher replayable = HttpRequest.BodyPublishers.ofInputStream(body::openStream);
            publisher = HttpRequest.BodyPublishers.fromPublisher(replayable, body.length());
        }
        request.method(method.name(), publisher);
        try {
            HttpResponse<InputStream> response = client.send(request.build(), HttpResponse.BodyHandlers.ofInputStream());
            int status = response.statusCode();
            if (retryableStatus(status)) {
                try { response.body().close(); } catch (IOException ignored) { }
                throw new CpfServiceCallTransportException(
                        "Gateway downstream retryable HTTP status=" + status, status, true, false);
            }
            HttpHeaders responseHeaders = new HttpHeaders();
            response.headers().map().forEach(responseHeaders::put);
            return new CpfGatewayProxyResponse(status, responseHeaders, response.body());
        } catch (HttpConnectTimeoutException | ConnectException ex) {
            throw new CpfServiceCallTransportException("Gateway downstream 연결에 실패했습니다.", null, true, false, ex);
        } catch (HttpTimeoutException ex) {
            throw new CpfServiceCallTransportException("Gateway downstream 응답 timeout입니다.", null, true, true, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new CpfServiceCallTransportException("Gateway downstream 호출이 중단되었습니다.", null, false, false, ex);
        } catch (IOException ex) {
            throw new CpfServiceCallTransportException("Gateway downstream I/O 결과를 확정할 수 없습니다.", null, true, true, ex);
        }
    }

    private void copyHeader(HttpRequest.Builder request, String name, List<String> values) {
        if (name == null || RESTRICTED.contains(name.toLowerCase(Locale.ROOT))) return;
        for (String value : values) if (value != null) request.header(name, value);
    }

    private boolean retryableStatus(int status) {
        return status == 408 || status == 425 || status == 429 || status >= 500;
    }
}
