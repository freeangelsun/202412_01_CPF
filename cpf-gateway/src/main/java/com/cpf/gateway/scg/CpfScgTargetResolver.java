package com.cpf.gateway.scg;

import com.cpf.core.api.gateway.CpfGatewayRoute;
import com.cpf.core.api.servicecall.CpfServiceRegistryQueryPort;
import com.cpf.core.api.servicecall.CpfServiceRegistryView;
import com.cpf.gateway.config.CpfGatewaySafetyProperties;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
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

    public CpfScgTargetResolver(
            CpfServiceRegistryQueryPort registry,
            CpfGatewaySafetyProperties safety) {
        this.registry = registry;
        this.safety = safety;
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
                .mapToInt(CpfServiceRegistryView.Instance::priority)
                .min()
                .orElseThrow();
        candidates = candidates.stream()
                .filter(instance -> instance.priority() == bestPriority)
                .sorted(Comparator.comparing(CpfServiceRegistryView.Instance::instanceId))
                .toList();

        CpfServiceRegistryView.Instance selected = weighted(route.serverGroupId(), candidates);
        URI base = URI.create(selected.baseUrl()).normalize();
        validateBaseUri(base);
        validateResolvedAddresses(base, safety.isAllowPublicTargets());
        URI resolved = resolveCanonical(base, targetPath, rawQuery);
        return new Target(selected.instanceId(), resolved);
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

    private static void validateResolvedAddresses(URI base, boolean allowPublic) {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(base.getHost());
            if (addresses.length == 0) {
                throw new SecurityException("Gateway upstream DNS returned no addresses");
            }
            for (InetAddress address : addresses) {
                if (address.isAnyLocalAddress()
                        || address.isLoopbackAddress()
                        || address.isLinkLocalAddress()
                        || address.isMulticastAddress()) {
                    throw new SecurityException(
                            "Gateway upstream address denied: " + address.getHostAddress());
                }
                if (!allowPublic && !privateAddress(address)) {
                    throw new SecurityException(
                            "Gateway public upstream address denied: " + address.getHostAddress());
                }
            }
        } catch (java.net.UnknownHostException failure) {
            throw new SecurityException("Gateway upstream DNS resolution failed", failure);
        }
    }

    private static boolean privateAddress(InetAddress address) {
        if (address.isSiteLocalAddress()) {
            return true;
        }
        if (address instanceof Inet4Address) {
            byte[] value = address.getAddress();
            int first = Byte.toUnsignedInt(value[0]);
            int second = Byte.toUnsignedInt(value[1]);
            return first == 100 && second >= 64 && second <= 127;
        }
        byte first = address.getAddress()[0];
        return (first & 0xfe) == 0xfc; // IPv6 unique-local fc00::/7
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

    public record Target(String instanceId, URI uri) {}
}
