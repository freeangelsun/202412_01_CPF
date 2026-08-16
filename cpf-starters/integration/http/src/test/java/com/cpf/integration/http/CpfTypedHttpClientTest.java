package com.cpf.integration.http;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class CpfTypedHttpClientTest {
    @Test
    void mutationRequiresIdempotencyKeyBeforeTransport() {
        CpfTypedHttpClient client = client(1024);
        assertThrows(IllegalArgumentException.class, () -> client.execute(
                "POST", URI.create("http://127.0.0.1:1/test"), new byte[0], null,
                "tx-1", null, Duration.ofSeconds(1)));
    }

    @Test
    void rejectsUnsafeUriAndOversizedRequestBeforeTransport() {
        CpfTypedHttpClient client = client(1024);
        assertThrows(IllegalArgumentException.class, () -> client.execute(
                "POST", URI.create("file:///tmp/x"), new byte[0], null,
                "tx-1", "idem-1", Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> client.execute(
                "POST", URI.create("http://localhost/test"), new byte[1025], null,
                "tx-1", "idem-1", Duration.ofSeconds(1)));
        assertThrows(SecurityException.class, () -> client.execute(
                "GET", URI.create("https://unlisted.example.invalid/test"), null, null,
                "tx-2", null, Duration.ofSeconds(1)));
    }

    @Test
    void enforcesStreamingResponseLimitAndDefensiveBodyCopy() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/large", exchange -> {
            byte[] response = new byte[2048];
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.createContext("/ok", exchange -> {
            byte[] response = new byte[] {1, 2, 3};
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        try {
            CpfTypedHttpClient client = client(1024);
            URI base = URI.create("http://127.0.0.1:" + server.getAddress().getPort());
            assertThrows(CpfTypedHttpClient.CpfHttpResponseTooLargeException.class, () -> client.execute(
                    "GET", base.resolve("/large"), null, null, "tx-1", null, Duration.ofSeconds(2)));
            CpfTypedHttpClient.Result result = client.execute(
                    "GET", base.resolve("/ok"), null, null, "tx-2", null, Duration.ofSeconds(2));
            byte[] returned = result.body();
            returned[0] = 9;
            assertArrayEquals(new byte[] {1, 2, 3}, result.body());
        } finally {
            server.stop(0);
        }
    }


    @Test
    void rejectsOversizedOrControlCharacterHeadersBeforeTransport() {
        CpfTypedHttpClient client = client(1024);
        assertThrows(IllegalArgumentException.class, () -> client.execute(
                "GET", URI.create("http://localhost/test"), null, null,
                "x".repeat(101), null, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> client.execute(
                "POST", URI.create("http://localhost/test"), null, null,
                "tx", "idem\r\nInjected: 1", Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> client.execute(
                "GET", URI.create("http://localhost/test"), null,
                "application/json\nInjected: 1", "tx", null, Duration.ofSeconds(1)));
    }

    @Test
    void supportsExactAndBoundedWildcardHostAllowlist() {
        CpfHttpClientProperties properties = new CpfHttpClientProperties();
        properties.setAllowedHosts(java.util.Set.of("api.example.com", "*.partner.example.com"));
        properties.validate();
        assertTrue(properties.allowsHost("API.EXAMPLE.COM."));
        assertTrue(properties.allowsHost("one.partner.example.com"));
        org.junit.jupiter.api.Assertions.assertFalse(properties.allowsHost("partner.example.com"));
        org.junit.jupiter.api.Assertions.assertFalse(properties.allowsHost("evilpartner.example.com"));
        assertThrows(IllegalArgumentException.class,
                () -> properties.setAllowedHosts(java.util.Set.of("*.com")));
    }

    @Test
    void classifiesKnownConnectionFailuresAsPreDispatch() {
        assertTrue(CpfTypedHttpClient.isDeterministicPreDispatchFailure(new UnknownHostException("missing")));
        assertTrue(CpfTypedHttpClient.isDeterministicPreDispatchFailure(
                new java.io.IOException("wrapper", new java.net.ConnectException("refused"))));
    }

    private static CpfTypedHttpClient client(int maxBytes) {
        CpfHttpClientProperties properties = new CpfHttpClientProperties();
        properties.setMaxRequestBytes(maxBytes);
        properties.setMaxResponseBytes(maxBytes);
        return new CpfTypedHttpClient(HttpClient.newHttpClient(), properties, new CpfExecutionIdGenerator() {
            @Override public String newExecutionId() { return "TEST-EXECUTION-ID"; }
            @Override public String newSegmentId() { return "TEST-SEGMENT-ID"; }
        });
    }
}
