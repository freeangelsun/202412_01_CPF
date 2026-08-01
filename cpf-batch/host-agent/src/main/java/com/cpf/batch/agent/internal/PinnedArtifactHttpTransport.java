package com.cpf.batch.agent.internal;

import com.cpf.core.api.security.network.CpfNetworkEndpointPolicy;

import com.cpf.batch.agent.AgentProperties;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * DNS에서 검증한 IP와 실제 Socket 연결 주소를 동일하게 고정하는 Artifact 전송기입니다.
 * TLS는 pinned IP로 연결하되 SNI와 hostname verification은 Repository hostname을 사용합니다.
 */
final class PinnedArtifactHttpTransport {
    private final AgentProperties properties;
    private final AddressResolver resolver;

    PinnedArtifactHttpTransport(AgentProperties properties) {
        this(properties, host -> List.of(InetAddress.getAllByName(host)));
    }

    PinnedArtifactHttpTransport(AgentProperties properties, AddressResolver resolver) {
        this.properties = properties;
        this.resolver = resolver;
    }

    long download(URI uri, Path target, String expectedContentType, String expectedSha256) throws Exception {
        ResolvedTarget targetIdentity = resolveAndValidate(uri);
        ResolvedTarget proxyIdentity = resolveProxy();
        final Socket connected = openSocket(targetIdentity, proxyIdentity, uri);
        try (Socket socket = connected;
                InputStream rawInput = new BufferedInputStream(socket.getInputStream());
                OutputStream rawOutput = new BufferedOutputStream(socket.getOutputStream())) {
            writeRequest(rawOutput, uri, targetIdentity, proxyIdentity != null);
            ResponseHead head = readHead(rawInput);
            validateHead(head, expectedContentType, expectedSha256);
            long declared = Long.parseLong(head.header("content-length"));
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (OutputStream file = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW)) {
                byte[] buffer = new byte[8192];
                long total = 0;
                while (total < declared) {
                    int limit = (int) Math.min(buffer.length, declared - total);
                    int read = rawInput.read(buffer, 0, limit);
                    if (read < 0) throw new EOFException("ARTIFACT_CONTENT_LENGTH_MISMATCH");
                    if (read == 0) continue;
                    total += read;
                    if (total > properties.getMaxArtifactBytes()) {
                        throw new SecurityException("ARTIFACT_STREAM_SIZE_EXCEEDED");
                    }
                    digest.update(buffer, 0, read);
                    file.write(buffer, 0, read);
                }
                String actual = HexFormat.of().formatHex(digest.digest());
                if (!actual.equalsIgnoreCase(expectedSha256)) {
                    throw new SecurityException("ARTIFACT_DIGEST_MISMATCH");
                }
                return total;
            }
        } catch (Exception failure) {
            Files.deleteIfExists(target);
            throw failure;
        }
    }

    ResolvedTarget resolveAndValidate(URI uri) throws Exception {
        if (uri == null || !uri.isAbsolute() || uri.getHost() == null || uri.getUserInfo() != null
                || uri.getFragment() != null) {
            throw new SecurityException("ARTIFACT_REPOSITORY_URL_INVALID");
        }
        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        if (!Set.of("https", "http").contains(scheme)) {
            throw new SecurityException("ARTIFACT_REPOSITORY_SCHEME_DENIED");
        }
        String host = canonicalHost(uri.getHost());
        if (!properties.getArtifactAllowedHosts().isEmpty()
                && properties.getArtifactAllowedHosts().stream()
                .map(PinnedArtifactHttpTransport::canonicalHost).noneMatch(host::equals)) {
            throw new SecurityException("ARTIFACT_REPOSITORY_HOST_DENIED");
        }
        int port = effectivePort(uri);
        if (!properties.getArtifactAllowedPorts().contains(port)) {
            throw new SecurityException("ARTIFACT_REPOSITORY_PORT_DENIED:" + port);
        }
        boolean localHttp = "http".equals(scheme) && properties.isAllowHttpLoopback() && literalLoopback(host);
        CpfNetworkEndpointPolicy policy = new CpfNetworkEndpointPolicy(
                properties.getArtifactAllowedCidrs(), properties.getArtifactAllowedPorts(),
                properties.isAllowPrivateRepositoryAddresses(), true, true, !localHttp);
        if (!localHttp) {
            try { policy.validateEndpoint(uri.toString()); }
            catch (IllegalArgumentException denied) { throw new SecurityException("ARTIFACT_REPOSITORY_NETWORK_POLICY_DENIED", denied); }
        }
        ResolvedTarget resolved = resolveIdentity(
                "ARTIFACT_REPOSITORY", host, port, properties.getArtifactPinnedAddresses(), policy, localHttp);
        if (localHttp && !resolved.address().isLoopbackAddress()) {
            throw new SecurityException("ARTIFACT_REPOSITORY_LOOPBACK_PIN_REQUIRED");
        }
        if ("http".equals(scheme) && !localHttp) throw new SecurityException("ARTIFACT_REPOSITORY_TLS_REQUIRED");
        return resolved;
    }

    private ResolvedTarget resolveProxy() throws Exception {
        String configured = properties.getArtifactProxyHost();
        if (configured == null || configured.isBlank()) return null;
        String host = canonicalHost(configured);
        int port = properties.getArtifactProxyPort();
        if (port < 1 || port > 65535) throw new SecurityException("ARTIFACT_PROXY_PORT_INVALID");
        CpfNetworkEndpointPolicy policy = new CpfNetworkEndpointPolicy(
                properties.getArtifactProxyAllowedCidrs(), Set.of(port),
                properties.isAllowPrivateProxyAddresses(), true, true, false);
        return resolveIdentity(
                "ARTIFACT_PROXY", host, port, properties.getArtifactProxyPinnedAddresses(), policy, false);
    }

    private ResolvedTarget resolveIdentity(
            String prefix,
            String host,
            int port,
            List<String> configuredPins,
            CpfNetworkEndpointPolicy policy,
            boolean allowLoopback) throws Exception {
        List<InetAddress> resolved = new ArrayList<>(resolver.resolve(host));
        if (resolved.isEmpty()) throw new SecurityException(prefix + "_DNS_EMPTY");
        resolved = resolved.stream().distinct().sorted(Comparator.comparing(InetAddress::getHostAddress)).toList();
        Set<String> pins = new LinkedHashSet<>();
        configuredPins.forEach(value -> pins.add(normalizeAddress(value)));
        boolean privateSeen = false;
        boolean publicSeen = false;
        boolean allLoopback = true;
        for (InetAddress address : resolved) {
            allLoopback &= address.isLoopbackAddress();
            String normalized = normalizeAddress(address.getHostAddress());
            if (!pins.isEmpty() && !pins.contains(normalized)) {
                throw new SecurityException(prefix + "_PIN_MISMATCH:" + normalized);
            }
            if (!(allowLoopback && address.isLoopbackAddress())) {
                try {
                    CpfNetworkEndpointPolicy.Address parsed = CpfNetworkEndpointPolicy.Address.parse(normalized);
                    if (parsed.privateAddress()) privateSeen = true; else publicSeen = true;
                    policy.validateResolvedAddresses(host, List.of(normalized));
                } catch (IllegalArgumentException denied) {
                    throw new SecurityException(prefix + "_NETWORK_POLICY_DENIED:" + normalized, denied);
                }
            } else {
                privateSeen = true;
            }
        }
        if (privateSeen && publicSeen) throw new SecurityException(prefix + "_MIXED_DNS_RESPONSE_DENIED");
        if (pins.isEmpty() && !isLiteralIp(host) && !allLoopback) {
            throw new SecurityException(prefix + "_PIN_REQUIRED");
        }
        return new ResolvedTarget(host, resolved.getFirst(), port);
    }

    private Socket openSocket(ResolvedTarget target, ResolvedTarget proxy, URI uri) throws Exception {
        int connectTimeout = positiveSeconds(properties.getArtifactConnectTimeoutSeconds(), "ARTIFACT_CONNECT_TIMEOUT_INVALID") * 1000;
        int readTimeout = positiveSeconds(properties.getArtifactReadTimeoutSeconds(), "ARTIFACT_READ_TIMEOUT_INVALID") * 1000;
        Socket base = new Socket();
        if (proxy != null) {
            base.connect(new InetSocketAddress(proxy.address(), proxy.port()), connectTimeout);
            base.setSoTimeout(readTimeout);
            if ("https".equalsIgnoreCase(uri.getScheme())) establishTunnel(base, target);
        } else {
            base.connect(new InetSocketAddress(target.address(), target.port()), connectTimeout);
            base.setSoTimeout(readTimeout);
        }
        if (!"https".equalsIgnoreCase(uri.getScheme())) return base;
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket ssl = (SSLSocket) factory.createSocket(base, target.host(), target.port(), true);
        SSLParameters parameters = ssl.getSSLParameters();
        parameters.setEndpointIdentificationAlgorithm("HTTPS");
        if (!isLiteralIp(target.host())) parameters.setServerNames(List.of(new SNIHostName(target.host())));
        ssl.setSSLParameters(parameters);
        ssl.startHandshake();
        return ssl;
    }

    private static int positiveSeconds(int value, String code) {
        if (value < 1 || value > 3600) throw new SecurityException(code);
        return value;
    }

    private static void establishTunnel(Socket proxySocket, ResolvedTarget target) throws IOException {
        OutputStream out = new BufferedOutputStream(proxySocket.getOutputStream());
        InputStream in = new BufferedInputStream(proxySocket.getInputStream());
        String authority = hostLiteral(target.address()) + ":" + target.port();
        String hostHeader = authorityHeader(target.host(), target.port());
        out.write(("CONNECT " + authority + " HTTP/1.1\r\nHost: " + hostHeader
                + "\r\nProxy-Connection: keep-alive\r\nConnection: keep-alive\r\n\r\n")
                .getBytes(StandardCharsets.US_ASCII));
        out.flush();
        ResponseHead response = readHead(in);
        if (response.statusCode() != 200) {
            throw new IOException("ARTIFACT_PROXY_CONNECT_STATUS:" + response.statusCode());
        }
    }

    private void writeRequest(OutputStream out, URI uri, ResolvedTarget target, boolean proxy) throws IOException {
        String path = uri.getRawPath() == null || uri.getRawPath().isBlank() ? "/" : uri.getRawPath();
        if (uri.getRawQuery() != null) path += "?" + uri.getRawQuery();
        boolean plainProxy = proxy && "http".equalsIgnoreCase(uri.getScheme());
        String requestTarget = plainProxy
                ? "http://" + hostLiteral(target.address()) + ":" + target.port() + path
                : path;
        String hostHeader = authorityHeader(target.host(), target.port());
        String request = "GET " + requestTarget + " HTTP/1.1\r\n"
                + "Host: " + hostHeader + "\r\n"
                + "Accept: application/java-archive, application/zip, application/octet-stream\r\n"
                + "Connection: close\r\n"
                + "User-Agent: CPF-Artifact-Agent/1\r\n\r\n";
        out.write(request.getBytes(StandardCharsets.US_ASCII));
        out.flush();
    }

    private void validateHead(ResponseHead head, String expectedContentType, String expectedSha256) {
        if (head.statusCode() != 200) throw new SecurityException("ARTIFACT_REPOSITORY_STATUS:" + head.statusCode());
        if (head.headers().containsKey("location")) throw new SecurityException("ARTIFACT_REDIRECT_DENIED");
        if (head.headers().containsKey("transfer-encoding")) throw new SecurityException("ARTIFACT_TRANSFER_ENCODING_DENIED");
        String length = head.header("content-length");
        if (length == null || !length.matches("[0-9]+")) throw new SecurityException("ARTIFACT_CONTENT_LENGTH_INVALID");
        long declared = Long.parseLong(length);
        if (declared < 0 || declared > properties.getMaxArtifactBytes()) {
            throw new SecurityException("ARTIFACT_CONTENT_LENGTH_INVALID");
        }
        String type = head.header("content-type");
        if (type == null) throw new SecurityException("ARTIFACT_CONTENT_TYPE_MISSING");
        type = type.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        if (properties.getArtifactAllowedContentTypes().stream()
                .map(v -> v.toLowerCase(Locale.ROOT)).noneMatch(type::equals)) {
            throw new SecurityException("ARTIFACT_CONTENT_TYPE_DENIED:" + type);
        }
        if (expectedContentType != null && !expectedContentType.isBlank()) {
            String expected = expectedContentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
            if (!expected.equals(type)) throw new SecurityException("ARTIFACT_CONTENT_TYPE_MISMATCH:" + type);
        }
        String checksum = checksum(head);
        if (checksum == null && properties.isRequireRepositoryDigestHeader()) {
            throw new SecurityException("ARTIFACT_DIGEST_HEADER_REQUIRED");
        }
        if (checksum != null && !checksum.equalsIgnoreCase(expectedSha256)) {
            throw new SecurityException("ARTIFACT_DIGEST_HEADER_MISMATCH");
        }
    }

    private static String checksum(ResponseHead head) {
        String value = head.header("x-checksum-sha256");
        if (value != null) return value.trim();
        String digest = head.header("digest");
        if (digest == null) return null;
        for (String part : digest.split(",")) {
            String token = part.trim();
            if (token.regionMatches(true, 0, "sha-256=", 0, 8)) {
                try {
                    return HexFormat.of().formatHex(Base64.getDecoder().decode(token.substring(8)));
                } catch (IllegalArgumentException failure) {
                    throw new SecurityException("ARTIFACT_DIGEST_HEADER_INVALID", failure);
                }
            }
        }
        return null;
    }

    /** Source-compatible test helper delegated to the common CIDR parser. */
    static boolean inCidr(InetAddress address, String cidr) {
        try {
            return new CpfNetworkEndpointPolicy(List.of(cidr), Set.of(443), true, true, false, false)
                    .contains(cidr, address.getHostAddress());
        } catch (IllegalArgumentException invalid) {
            throw new SecurityException("ARTIFACT_CIDR_INVALID:" + cidr, invalid);
        }
    }

    private static ResponseHead readHead(InputStream in) throws IOException {
        String status = readLine(in);
        if (status == null || !status.matches("HTTP/1\\.[01] [0-9]{3}.*")) {
            throw new IOException("ARTIFACT_HTTP_STATUS_INVALID");
        }
        int code = Integer.parseInt(status.substring(9, 12));
        Map<String, String> headers = new LinkedHashMap<>();
        for (;;) {
            String line = readLine(in);
            if (line == null) throw new EOFException("ARTIFACT_HTTP_HEADERS_TRUNCATED");
            if (line.isEmpty()) break;
            int colon = line.indexOf(':');
            if (colon <= 0) throw new IOException("ARTIFACT_HTTP_HEADER_INVALID");
            String name = line.substring(0, colon).trim().toLowerCase(Locale.ROOT);
            String value = line.substring(colon + 1).trim();
            if (!name.matches("[!#$%&'*+.^_`|~0-9a-z-]+") || name.length() > 128 || value.length() > 8192) {
                throw new IOException("ARTIFACT_HTTP_HEADER_INVALID");
            }
            headers.merge(name, value, (a, b) -> a + "," + b);
            if (headers.size() > 100) throw new IOException("ARTIFACT_HTTP_HEADER_COUNT_EXCEEDED");
        }
        return new ResponseHead(code, Map.copyOf(headers));
    }

    private static String readLine(InputStream in) throws IOException {
        StringBuilder line = new StringBuilder();
        int previous = -1;
        for (int current; (current = in.read()) >= 0;) {
            if (previous == '\r' && current == '\n') {
                line.setLength(Math.max(0, line.length() - 1));
                return line.toString();
            }
            line.append((char) current);
            previous = current;
            if (line.length() > 16_384) throw new IOException("ARTIFACT_HTTP_LINE_OVERSIZE");
        }
        return line.isEmpty() ? null : line.toString();
    }

    private static String canonicalHost(String value) {
        String host = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        while (host.endsWith(".")) host = host.substring(0, host.length() - 1);
        return host;
    }
    private static boolean literalLoopback(String host) {
        String value = canonicalHost(host);
        return "localhost".equals(value) || "127.0.0.1".equals(value) || "::1".equals(value)
                || "0:0:0:0:0:0:0:1".equals(value);
    }
    private static boolean isLiteralIp(String host) { return host != null && (host.matches("[0-9.]+") || host.contains(":")); }
    private static String normalizeAddress(String value) { return value.trim().toLowerCase(Locale.ROOT).replace("%25", "%"); }
    private static int effectivePort(URI uri) { return uri.getPort() > 0 ? uri.getPort() : ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80); }
    private static String authorityHeader(String host, int port) {
        String literal = host.contains(":") ? "[" + host + "]" : host;
        return literal + ":" + port;
    }
    private static String hostLiteral(InetAddress address) {
        return address.getHostAddress().contains(":") ? "[" + address.getHostAddress() + "]" : address.getHostAddress();
    }

    @FunctionalInterface
    interface AddressResolver { List<InetAddress> resolve(String host) throws Exception; }
    record ResolvedTarget(String host, InetAddress address, int port) {}
    private record ResponseHead(int statusCode, Map<String, String> headers) {
        String header(String name) { return headers.get(name); }
    }
}
