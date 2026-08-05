package com.cpf.starter.integration.http;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;

public final class S03HttpClientHarness {
    private static int cases;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/ok", exchange -> {
            byte[] body = "accepted".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(202, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/slow", exchange -> {
            try { Thread.sleep(250L); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
            byte[] body = "late".getBytes(StandardCharsets.UTF_8);
            try {
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            } finally { exchange.close(); }
        });
        server.start();
        try {
            int port = server.getAddress().getPort();
            CpfHttpClientProperties properties = new CpfHttpClientProperties();
            properties.setAllowedHosts(Set.of("127.0.0.1"));
            properties.setRequestTimeout(Duration.ofSeconds(2));
            CpfTypedHttpClient client = new CpfTypedHttpClient(HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(1)).build(), properties);

            var result = client.execute("GET", URI.create("http://127.0.0.1:" + port + "/ok"),
                    null, null, "tx-1", null, Duration.ofSeconds(1));
            check(result.status() == 202, "status");
            check("accepted".equals(new String(result.body(), StandardCharsets.UTF_8)), "body");
            byte[] exposed = result.body(); exposed[0] = 'X';
            check("accepted".equals(new String(result.body(), StandardCharsets.UTF_8)), "immutable body");

            expect(IllegalArgumentException.class, () -> client.execute("POST",
                    URI.create("http://127.0.0.1:" + port + "/ok"), new byte[] {1},
                    "application/octet-stream", "tx-2", null, Duration.ofSeconds(1)));
            expect(IllegalArgumentException.class, () -> client.execute("GET",
                    URI.create("http://127.0.0.1:" + port + "/ok"), null,
                    null, "tx\r\nInjected: x", null, Duration.ofSeconds(1)));
            expect(SecurityException.class, () -> client.execute("GET",
                    URI.create("http://example.com/"), null, null, "tx-3", null, Duration.ofSeconds(1)));
            expect(CpfTypedHttpClient.CpfUnknownHttpResultException.class, () -> client.execute("GET",
                    URI.create("http://127.0.0.1:" + port + "/slow"), null,
                    null, "tx-4", null, Duration.ofMillis(50)));
            expect(CpfTypedHttpClient.CpfHttpPreDispatchException.class, () -> client.execute("GET",
                    URI.create("http://127.0.0.1:1/unreachable"), null,
                    null, "tx-5", null, Duration.ofMillis(300)));
            System.out.println("S03_HTTP_CLIENT_HARNESS PASS cases=" + cases);
        } finally {
            server.stop(0);
        }
    }

    private static void check(boolean condition, String label) {
        cases++;
        if (!condition) throw new AssertionError(label);
    }

    private static void expect(Class<? extends Throwable> type, Runnable action) {
        cases++;
        try { action.run(); throw new AssertionError("expected " + type.getSimpleName()); }
        catch (Throwable actual) {
            if (!type.isInstance(actual)) throw new AssertionError("unexpected " + actual, actual);
        }
    }
}
