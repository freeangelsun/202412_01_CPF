package com.cpf.batch.worker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/** 검증된 IP로만 연결하는 Batch 외부 HTTP 전송기입니다. */
final class PinnedBatchHttpTransport {
    private final WorkerOperationalProperties.OutboundHttp policy;

    PinnedBatchHttpTransport(WorkerOperationalProperties.OutboundHttp policy) {
        this.policy = policy;
    }

    Response exchange(BatchOutboundHttpPolicy.ApprovedTarget target, String method, byte[] body,
                      Map<String, String> headers) throws IOException {
        validateHeaders(headers);
        Socket base = new Socket();
        base.connect(new InetSocketAddress(target.address(), target.port()), policy.getConnectTimeoutSeconds() * 1000);
        base.setSoTimeout(policy.getReadTimeoutSeconds() * 1000);
        final Socket connected;
        if ("https".equalsIgnoreCase(target.originalUri().getScheme())) {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket ssl = (SSLSocket) factory.createSocket(base, target.host(), target.port(), true);
            SSLParameters parameters = ssl.getSSLParameters();
            parameters.setEndpointIdentificationAlgorithm("HTTPS");
            parameters.setServerNames(List.of(new SNIHostName(target.host())));
            ssl.setSSLParameters(parameters);
            ssl.startHandshake();
            connected = ssl;
        } else {
            connected = base;
        }
        try (Socket socket = connected;
             InputStream in = new BufferedInputStream(socket.getInputStream());
             OutputStream out = new BufferedOutputStream(socket.getOutputStream())) {
            writeRequest(out, target, method, body, headers);
            ResponseHead head = readHead(in);
            if (head.status >= 300 && head.status < 400 || head.headers.containsKey("location")) {
                throw new SecurityException("BATCH_OUTBOUND_REDIRECT_DENIED");
            }
            if (head.headers.containsKey("transfer-encoding")) {
                throw new SecurityException("BATCH_OUTBOUND_TRANSFER_ENCODING_DENIED");
            }
            String lengthText = head.headers.get("content-length");
            if (lengthText == null || !lengthText.matches("[0-9]+")) {
                throw new SecurityException("BATCH_OUTBOUND_CONTENT_LENGTH_REQUIRED");
            }
            validateResponseContentType(head.headers);
            long length = Long.parseLong(lengthText);
            if (length > policy.getMaxResponseBytes()) throw new SecurityException("BATCH_OUTBOUND_RESPONSE_SIZE_EXCEEDED");
            if (length > Integer.MAX_VALUE) throw new SecurityException("BATCH_OUTBOUND_RESPONSE_SIZE_EXCEEDED");
            ByteArrayOutputStream response = new ByteArrayOutputStream((int) length);
            byte[] buffer = new byte[8192];
            long total = 0;
            while (total < length) {
                int read = in.read(buffer, 0, (int) Math.min(buffer.length, length - total));
                if (read < 0) throw new EOFException("BATCH_OUTBOUND_RESPONSE_TRUNCATED");
                if (read == 0) continue;
                total += read;
                if (total > policy.getMaxResponseBytes()) throw new SecurityException("BATCH_OUTBOUND_RESPONSE_SIZE_EXCEEDED");
                response.write(buffer, 0, read);
            }
            return new Response(head.status, response.toString(StandardCharsets.UTF_8), head.headers);
        }
    }

    private void writeRequest(OutputStream out, BatchOutboundHttpPolicy.ApprovedTarget target, String method,
                              byte[] body, Map<String, String> headers) throws IOException {
        URI uri = target.originalUri();
        String path = uri.getRawPath() == null || uri.getRawPath().isBlank() ? "/" : uri.getRawPath();
        if (uri.getRawQuery() != null) path += "?" + uri.getRawQuery();
        String authorityHost = target.host().contains(":") ? "[" + target.host() + "]" : target.host();
        String host = authorityHost + (isDefaultPort(uri) ? "" : ":" + target.port());
        StringBuilder request = new StringBuilder()
                .append(method).append(' ').append(path).append(" HTTP/1.1\r\n")
                .append("Host: ").append(host).append("\r\n")
                .append("Connection: close\r\n")
                .append("Accept: application/json, application/problem+json\r\n")
                .append("User-Agent: CPF-Batch-Protocol/1\r\n");
        headers.forEach((name, value) -> request.append(name).append(": ").append(safeHeader(value)).append("\r\n"));
        request.append("Content-Length: ").append(body.length).append("\r\n\r\n");
        out.write(request.toString().getBytes(StandardCharsets.US_ASCII));
        out.write(body);
        out.flush();
    }

    private void validateHeaders(Map<String, String> headers) {
        Set<String> allowed = policy.getAllowedRequestHeaders().stream()
                .map(v -> v.toLowerCase(Locale.ROOT)).collect(java.util.stream.Collectors.toUnmodifiableSet());
        headers.keySet().forEach(name -> {
            if (name == null || !name.matches("[!#$%&'*+.^_`|~0-9A-Za-z-]+")) {
                throw new SecurityException("BATCH_OUTBOUND_HEADER_NAME_INVALID");
            }
            String normalized = name.toLowerCase(Locale.ROOT);
            if (!allowed.contains(normalized)) throw new SecurityException("BATCH_OUTBOUND_HEADER_DENIED:" + name);
            if (Set.of("host", "connection", "content-length", "transfer-encoding").contains(normalized)) {
                throw new SecurityException("BATCH_OUTBOUND_MANAGED_HEADER_DENIED:" + name);
            }
        });
    }

    private static String safeHeader(String value) {
        if (value == null || value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) {
            throw new SecurityException("BATCH_OUTBOUND_HEADER_VALUE_INVALID");
        }
        return value;
    }

    private void validateResponseContentType(Map<String, String> headers) {
        String contentType = headers.getOrDefault("content-type", "").toLowerCase(Locale.ROOT);
        int semi = contentType.indexOf(';');
        String mediaType = (semi >= 0 ? contentType.substring(0, semi) : contentType).trim();
        if (mediaType.isEmpty() || policy.getAllowedResponseContentTypes().stream()
                .map(v -> v.toLowerCase(Locale.ROOT)).noneMatch(mediaType::equals)) {
            throw new SecurityException("BATCH_OUTBOUND_RESPONSE_CONTENT_TYPE_DENIED:" + mediaType);
        }
    }

    private ResponseHead readHead(InputStream in) throws IOException {
        String statusLine = readLine(in);
        if (statusLine == null || !statusLine.matches("HTTP/1\\.[01] [0-9]{3}.*")) throw new IOException("BATCH_OUTBOUND_STATUS_INVALID");
        int status = Integer.parseInt(statusLine.substring(9, 12));
        Map<String, String> headers = new LinkedHashMap<>();
        for (;;) {
            String line = readLine(in);
            if (line == null) throw new EOFException("BATCH_OUTBOUND_HEADERS_TRUNCATED");
            if (line.isEmpty()) break;
            int colon = line.indexOf(':');
            if (colon <= 0) throw new IOException("BATCH_OUTBOUND_HEADER_INVALID");
            String name = line.substring(0, colon).trim().toLowerCase(Locale.ROOT);
            String value = line.substring(colon + 1).trim();
            if (name.length() > 128 || value.length() > 8192 || headers.size() >= policy.getMaxResponseHeaderCount()) {
                throw new IOException("BATCH_OUTBOUND_HEADER_LIMIT_EXCEEDED");
            }
            int totalHeaderBytes = headers.entrySet().stream().mapToInt(e -> e.getKey().length() + e.getValue().length()).sum()
                    + name.length() + value.length();
            if (totalHeaderBytes > policy.getMaxResponseHeaderBytes()) {
                throw new IOException("BATCH_OUTBOUND_HEADER_BYTES_EXCEEDED");
            }
            headers.merge(name, value, (a, b) -> a + "," + b);
        }
        return new ResponseHead(status, Map.copyOf(headers));
    }

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        int previous = -1;
        for (int current; (current = in.read()) >= 0;) {
            if (previous == '\r' && current == '\n') {
                byte[] value = bytes.toByteArray();
                return new String(value, 0, Math.max(0, value.length - 1), StandardCharsets.US_ASCII);
            }
            bytes.write(current);
            previous = current;
            if (bytes.size() > 16_384) throw new IOException("BATCH_OUTBOUND_LINE_LIMIT_EXCEEDED");
        }
        return bytes.size() == 0 ? null : bytes.toString(StandardCharsets.US_ASCII);
    }

    private static boolean isDefaultPort(URI uri) {
        return uri.getPort() < 0 || "https".equalsIgnoreCase(uri.getScheme()) && uri.getPort() == 443
                || "http".equalsIgnoreCase(uri.getScheme()) && uri.getPort() == 80;
    }

    record Response(int statusCode, String body, Map<String, String> headers) {}
    private record ResponseHead(int status, Map<String, String> headers) {}
}
