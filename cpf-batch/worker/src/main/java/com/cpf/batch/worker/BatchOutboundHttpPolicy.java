package com.cpf.batch.worker;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Batch PROTOCOL_ADAPTER가 사용할 외부 HTTP 목적지 Identity를 검증합니다.
 * 검증한 IP를 실제 연결 주소로 반환하여 DNS rebinding을 차단합니다.
 */
final class BatchOutboundHttpPolicy {
    private final WorkerOperationalProperties.OutboundHttp policy;
    private final AddressResolver resolver;

    BatchOutboundHttpPolicy(WorkerOperationalProperties.OutboundHttp policy) {
        this(policy, host -> List.of(InetAddress.getAllByName(host)));
    }

    BatchOutboundHttpPolicy(WorkerOperationalProperties.OutboundHttp policy, AddressResolver resolver) {
        this.policy = policy;
        this.resolver = resolver;
    }

    ApprovedTarget approve(URI uri, int requestBytes) throws Exception {
        if (!policy.isEnabled()) throw new SecurityException("BATCH_OUTBOUND_DISABLED");
        if (uri == null || !uri.isAbsolute() || uri.getHost() == null || uri.getUserInfo() != null
                || uri.getFragment() != null) {
            throw new SecurityException("BATCH_OUTBOUND_URI_INVALID");
        }
        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        if (!Set.of("https", "http").contains(scheme)) throw new SecurityException("BATCH_OUTBOUND_SCHEME_DENIED");
        String host = canonicalHost(uri.getHost());
        int port = uri.getPort() > 0 ? uri.getPort() : ("https".equals(scheme) ? 443 : 80);
        if (!policy.getAllowedHosts().stream().map(BatchOutboundHttpPolicy::canonicalHost).anyMatch(host::equals)) {
            throw new SecurityException("BATCH_OUTBOUND_HOST_DENIED:" + host);
        }
        if (!policy.getAllowedPorts().contains(port)) throw new SecurityException("BATCH_OUTBOUND_PORT_DENIED:" + port);
        if (requestBytes < 0 || requestBytes > policy.getMaxRequestBytes()) {
            throw new SecurityException("BATCH_OUTBOUND_REQUEST_SIZE_EXCEEDED");
        }
        List<InetAddress> addresses = new ArrayList<>(resolver.resolve(host));
        if (addresses.isEmpty()) throw new SecurityException("BATCH_OUTBOUND_DNS_EMPTY");
        addresses = addresses.stream().distinct().sorted(Comparator.comparing(InetAddress::getHostAddress)).toList();
        Set<String> pins = normalizedPins(host);
        boolean privateSeen = false;
        boolean publicSeen = false;
        boolean allLoopback = true;
        for (InetAddress address : addresses) {
            validateAddress(address);
            boolean privateAddress = privateAddress(address);
            privateSeen |= privateAddress;
            publicSeen |= !privateAddress;
            allLoopback &= address.isLoopbackAddress();
            if (!pins.isEmpty() && !pins.contains(normalize(address.getHostAddress()))) {
                throw new SecurityException("BATCH_OUTBOUND_DNS_PIN_MISMATCH:" + address.getHostAddress());
            }
            if (!policy.getAllowedCidrs().isEmpty()
                    && policy.getAllowedCidrs().stream().noneMatch(cidr -> inCidr(address, cidr))) {
                throw new SecurityException("BATCH_OUTBOUND_CIDR_DENIED:" + address.getHostAddress());
            }
        }
        if (privateSeen && publicSeen) {
            throw new SecurityException("BATCH_OUTBOUND_MIXED_DNS_RESPONSE_DENIED");
        }
        if ("http".equals(scheme) && !(policy.isAllowHttpLoopback() && allLoopback)) {
            throw new SecurityException("BATCH_OUTBOUND_TLS_REQUIRED");
        }
        if (policy.isRequireDnsPin() && pins.isEmpty() && !allLoopback && !isLiteralIp(host)) {
            throw new SecurityException("BATCH_OUTBOUND_DNS_PIN_REQUIRED");
        }
        return new ApprovedTarget(uri, host, addresses.getFirst(), port);
    }

    private Set<String> normalizedPins(String host) {
        List<String> configured = policy.getHostPins().entrySet().stream()
                .filter(e -> canonicalHost(e.getKey()).equals(host))
                .map(java.util.Map.Entry::getValue)
                .findFirst().orElse(List.of());
        Set<String> pins = new LinkedHashSet<>();
        configured.forEach(value -> pins.add(normalize(value)));
        return Set.copyOf(pins);
    }

    private void validateAddress(InetAddress address) {
        String normalized = normalize(address.getHostAddress());
        if (metadataAddress(address) || address.isAnyLocalAddress() || address.isMulticastAddress()) {
            throw new SecurityException("BATCH_OUTBOUND_METADATA_ADDRESS_DENIED:" + normalized);
        }
        if (!policy.isAllowPrivateAddresses() && privateAddress(address)) {
            throw new SecurityException("BATCH_OUTBOUND_ADDRESS_DENIED:" + normalized);
        }
    }

    private static boolean metadataAddress(InetAddress address) {
        byte[] raw = address.getAddress();
        if (raw.length == 4) {
            int a = Byte.toUnsignedInt(raw[0]);
            int b = Byte.toUnsignedInt(raw[1]);
            int c = Byte.toUnsignedInt(raw[2]);
            int d = Byte.toUnsignedInt(raw[3]);
            return a == 169 && b == 254 && c == 169 && d == 254
                    || a == 100 && b == 100 && c == 100 && d == 200;
        }
        String text = normalize(address.getHostAddress());
        return text.startsWith("fd00:ec2:") || text.equals("::ffff:169.254.169.254")
                || text.equals("::ffff:100.100.100.200");
    }

    private static boolean privateAddress(InetAddress address) {
        if (address.isLoopbackAddress() || address.isLinkLocalAddress() || address.isSiteLocalAddress()) return true;
        byte[] raw = address.getAddress();
        if (address instanceof Inet4Address && raw.length == 4) {
            int first = Byte.toUnsignedInt(raw[0]);
            int second = Byte.toUnsignedInt(raw[1]);
            return first == 100 && second >= 64 && second <= 127;
        }
        return raw.length == 16 && (raw[0] & 0xfe) == 0xfc;
    }

    static boolean inCidr(InetAddress address, String cidr) {
        try {
            String[] parts = cidr == null ? new String[0] : cidr.trim().split("/", 2);
            if (parts.length != 2) throw new IllegalArgumentException("missing prefix");
            InetAddress network = InetAddress.getByName(parts[0]);
            byte[] value = address.getAddress();
            byte[] base = network.getAddress();
            int prefix = Integer.parseInt(parts[1]);
            if (value.length != base.length || prefix < 0 || prefix > value.length * 8) return false;
            int bytes = prefix / 8;
            int bits = prefix % 8;
            for (int i = 0; i < bytes; i++) if (value[i] != base[i]) return false;
            if (bits == 0) return true;
            int mask = 0xff << (8 - bits);
            return (value[bytes] & mask) == (base[bytes] & mask);
        } catch (RuntimeException | java.net.UnknownHostException invalid) {
            throw new SecurityException("BATCH_OUTBOUND_CIDR_INVALID:" + cidr, invalid);
        }
    }

    private static String canonicalHost(String value) {
        String host = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        while (host.endsWith(".")) host = host.substring(0, host.length() - 1);
        return host;
    }
    private static boolean isLiteralIp(String host) { return host.matches("[0-9.]+") || host.contains(":"); }
    private static String normalize(String value) { return value.trim().toLowerCase(Locale.ROOT).replace("%25", "%"); }

    @FunctionalInterface
    interface AddressResolver { List<InetAddress> resolve(String host) throws Exception; }
    record ApprovedTarget(URI originalUri, String host, InetAddress address, int port) {}
}
