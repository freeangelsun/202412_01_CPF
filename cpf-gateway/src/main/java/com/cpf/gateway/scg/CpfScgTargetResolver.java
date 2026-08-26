package com.cpf.gateway.scg;

import com.cpf.gateway.api.CpfGatewayRoute;
import com.cpf.integration.api.servicecall.CpfServiceRegistryQueryPort;
import com.cpf.security.api.network.CpfNetworkEndpointPolicy;
import com.cpf.integration.api.servicecall.CpfServiceRegistryView;
import com.cpf.gateway.config.CpfGatewaySafetyProperties;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/** Registry의 신선한 UP 인스턴스만 선택하고 URI·DNS·CIDR SSRF 경계를 강제합니다. */
@Component
public final class CpfScgTargetResolver {
    private final CpfServiceRegistryQueryPort registry;
    private final CpfGatewaySafetyProperties safety;
    private final ConcurrentHashMap<String, AtomicLong> cursors = new ConcurrentHashMap<>();
    private final AddressResolver addressResolver;

    public CpfScgTargetResolver(
            CpfServiceRegistryQueryPort registry,
            CpfGatewaySafetyProperties safety) {
        this(registry, safety, InetAddress::getAllByName);
    }

    CpfScgTargetResolver(
            CpfServiceRegistryQueryPort registry,
            CpfGatewaySafetyProperties safety,
            AddressResolver addressResolver) {
        this.registry = registry;
        this.safety = safety;
        this.addressResolver = addressResolver;
    }

    public Target resolve(CpfGatewayRoute route, String targetPath, String rawQuery) {
        OffsetDateTime freshnessFloor = OffsetDateTime.now().minus(safety.getStaleAfter());
        List<CpfServiceRegistryView.Instance> candidates = registry
                .instances(route.serviceId(), null, "UP", 100)
                .stream()
                .filter(instance -> instance.active()
                        && !instance.maintenance()
                        && !instance.draining()
                        && "UP".equals(instance.status())
                        && instance.lastHeartbeatAt() != null
                        && !instance.lastHeartbeatAt().isBefore(freshnessFloor))
                .toList();
        if (candidates.isEmpty()) {
            throw new IllegalStateException("Gateway fresh UP target가 없습니다: " + route.serviceId());
        }

        if (safety.getZoneCode() != null && !safety.getZoneCode().isBlank()) {
            List<CpfServiceRegistryView.Instance> sameZone = candidates.stream()
                    .filter(instance -> safety.getZoneCode().equalsIgnoreCase(instance.zoneCode()))
                    .toList();
            if (!sameZone.isEmpty()) {
                candidates = sameZone;
            }
        }

        int bestPriority = candidates.stream()
                .mapToInt(value -> value.priority())
                .min()
                .orElseThrow();
        candidates = candidates.stream()
                .filter(instance -> instance.priority() == bestPriority)
                .sorted(Comparator.comparing(value -> value.instanceId()))
                .toList();

        CpfServiceRegistryView.Instance selected = weighted(route.serverGroupId(), candidates);
        URI base = URI.create(selected.baseUrl()).normalize();
        validateBaseUri(base);
        CpfNetworkEndpointPolicy endpointPolicy = new CpfNetworkEndpointPolicy(
                safety.getAllowedTargetCidrs(), safety.getAllowedTargetPorts(),
                safety.isAllowPrivateTargets(), safety.isAllowPublicTargets(),
                safety.isAllowDnsTargets(), safety.isRequireTlsTargets());
        endpointPolicy.validateEndpoint(base.toString());
        List<InetAddress> approved = validateResolvedAddresses(base, endpointPolicy, addressResolver);
        URI canonical = resolveCanonical(base, targetPath, rawQuery);
        InetAddress pinned = approved.getFirst();
        return new Target(
                selected.instanceId(),
                canonical,
                authorityHeader(base),
                pinned);
    }

    private CpfServiceRegistryView.Instance weighted(
            String key,
            List<CpfServiceRegistryView.Instance> candidates) {
        long totalWeight = candidates.stream().mapToLong(value -> Math.max(1, value.weight())).sum();
        long position = Math.floorMod(
                cursors.computeIfAbsent(key, ignored -> new AtomicLong()).getAndIncrement(),
                totalWeight);
        long cursor = 0;
        for (CpfServiceRegistryView.Instance candidate : candidates) {
            cursor += Math.max(1, candidate.weight());
            if (position < cursor) {
                return candidate;
            }
        }
        return candidates.getLast();
    }

    static URI resolveCanonical(URI base, String targetPath, String rawQuery) {
        validateBaseUri(base);
        String path = targetPath == null || targetPath.isBlank() ? "/" : targetPath;
        rejectControl(path, "path");
        if (path.startsWith("//") || path.indexOf('\\') >= 0) {
            throw new SecurityException("Gateway upstream path denied");
        }
        String decoded = URLDecoder.decode(path, StandardCharsets.UTF_8);
        String decodedTwice = URLDecoder.decode(decoded, StandardCharsets.UTF_8);
        if (containsTraversal(decoded) || containsTraversal(decodedTwice)) {
            throw new SecurityException("Gateway upstream path traversal denied");
        }

        String basePath = base.getRawPath() == null ? "/" : base.getRawPath();
        if (!basePath.endsWith("/")) {
            basePath += "/";
        }
        String relative = path.startsWith("/") ? path.substring(1) : path;
        String combined = basePath + relative;
        URI resolved;
        try {
            resolved = new URI(
                    base.getScheme(),
                    null,
                    base.getHost(),
                    base.getPort(),
                    combined,
                    validateQuery(rawQuery),
                    null).normalize();
        } catch (java.net.URISyntaxException failure) {
            throw new SecurityException("Gateway upstream URI canonicalization failed", failure);
        }
        if (!sameAuthority(base, resolved)
                || resolved.getFragment() != null
                || !resolved.getRawPath().startsWith(basePath)) {
            throw new SecurityException("Gateway upstream authority/path escape denied");
        }
        return resolved;
    }

    static void validateBaseUri(URI base) {
        if (base == null
                || !("http".equalsIgnoreCase(base.getScheme())
                        || "https".equalsIgnoreCase(base.getScheme()))
                || base.getUserInfo() != null
                || base.getFragment() != null
                || base.getQuery() != null
                || base.getHost() == null) {
            throw new SecurityException("허용되지 않은 Gateway upstream URI");
        }
        rejectControl(base.toString(), "base URI");
    }

    static List<InetAddress> validateResolvedAddresses(
            URI base,
            CpfNetworkEndpointPolicy endpointPolicy,
            AddressResolver resolver) {
        try {
            InetAddress[] resolved = resolver.resolve(base.getHost());
            if (resolved == null || resolved.length == 0) {
                throw new SecurityException("Gateway upstream DNS returned no addresses");
            }
            List<InetAddress> addresses = Arrays.stream(resolved)
                    .distinct()
                    .sorted(Comparator.comparing(value -> value.getHostAddress()))
                    .toList();
            endpointPolicy.validateResolvedAddresses(
                    base.getHost(), addresses.stream().map(value -> value.getHostAddress()).toList());
            boolean privateSeen = addresses.stream().map(value -> value.getHostAddress())
                    .map(CpfNetworkEndpointPolicy.Address::parse)
                    .anyMatch(value -> value.privateAddress());
            boolean publicSeen = addresses.stream().map(value -> value.getHostAddress())
                    .map(CpfNetworkEndpointPolicy.Address::parse)
                    .anyMatch(address -> !address.privateAddress());
            if (privateSeen && publicSeen) {
                throw new SecurityException("Gateway DNS mixed private/public response denied");
            }
            return addresses;
        } catch (java.net.UnknownHostException failure) {
            throw new SecurityException("Gateway upstream DNS resolution failed", failure);
        } catch (IllegalArgumentException failure) {
            throw new SecurityException("Gateway upstream network policy denied", failure);
        }
    }

    private static String authorityHeader(URI base) {
        int port = effectivePort(base);
        boolean defaultPort = ("https".equalsIgnoreCase(base.getScheme()) && port == 443)
                || ("http".equalsIgnoreCase(base.getScheme()) && port == 80);
        return defaultPort ? base.getHost() : base.getHost() + ":" + port;
    }

    private static String validateQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return null;
        }
        rejectControl(rawQuery, "query");
        if (rawQuery.length() > 8192 || rawQuery.indexOf('#') >= 0) {
            throw new SecurityException("Gateway upstream query denied");
        }
        return rawQuery;
    }

    private static boolean containsTraversal(String path) {
        for (String segment : path.replace('\\', '/').split("/", -1)) {
            if ("..".equals(segment) || ".".equals(segment)) {
                return true;
            }
        }
        return false;
    }

    private static void rejectControl(String value, String field) {
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current <= 0x1f || current == 0x7f) {
                throw new SecurityException("Gateway upstream " + field + " contains control characters");
            }
        }
    }

    private static boolean sameAuthority(URI first, URI second) {
        return first.getScheme().equalsIgnoreCase(second.getScheme())
                && first.getHost().equalsIgnoreCase(second.getHost())
                && effectivePort(first) == effectivePort(second);
    }

    private static int effectivePort(URI uri) {
        return uri.getPort() > 0
                ? uri.getPort()
                : ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80);
    }

    @FunctionalInterface
    interface AddressResolver {
        InetAddress[] resolve(String host) throws java.net.UnknownHostException;
    }

    /**
     * URI는 원 hostname을 유지해 TLS SNI/hostname 검증을 보존하고, pinnedAddress는
     * 전용 DNS resolver가 실제 연결 주소로만 사용합니다.
     */
    public record Target(
            String instanceId,
            URI uri,
            String authorityHeader,
            InetAddress pinnedAddress) {
        public String resolvedAddress() {
            return pinnedAddress.getHostAddress();
        }
    }
}
