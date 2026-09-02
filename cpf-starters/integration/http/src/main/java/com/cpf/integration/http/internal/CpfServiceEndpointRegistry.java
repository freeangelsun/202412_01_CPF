package com.cpf.integration.http.internal;

import com.cpf.security.api.network.CpfNetworkEndpointPolicy;
import com.cpf.core.api.error.CpfFrameworkErrorCode;
import com.cpf.core.api.error.CpfFrameworkException;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/** application 설정과 Runtime 기관 endpoint snapshot을 함께 해석하고 연결 주소를 고정합니다. */
public class CpfServiceEndpointRegistry {
    private final Map<String, CpfServiceEndpointProperties.ServiceEndpoint> configured;
    private final AtomicReference<Snapshot> runtime = new AtomicReference<>(Snapshot.empty());
    private final CpfNetworkEndpointPolicy endpointPolicy;
    private final AddressResolver addressResolver;

    public CpfServiceEndpointRegistry(CpfServiceEndpointProperties properties) {
        this(properties, CpfNetworkEndpointPolicy.secureDefault(), InetAddress::getAllByName);
    }

    public CpfServiceEndpointRegistry(
            CpfServiceEndpointProperties properties,
            CpfNetworkEndpointPolicy endpointPolicy) {
        this(properties, endpointPolicy, InetAddress::getAllByName);
    }

    CpfServiceEndpointRegistry(
            CpfServiceEndpointProperties properties,
            CpfNetworkEndpointPolicy endpointPolicy,
            AddressResolver addressResolver) {
        LinkedHashMap<String, CpfServiceEndpointProperties.ServiceEndpoint> normalizedConfigured = new LinkedHashMap<>();
        if (properties != null && properties.getServices() != null) {
            properties.getServices().forEach((key, value) -> {
                if (value == null) throw new IllegalArgumentException("null service endpoint 금지");
                String normalizedKey = normalizeConfiguredId(key);
                if (normalizedConfigured.putIfAbsent(normalizedKey, value) != null) {
                    throw new IllegalArgumentException("service endpoint key 정규화 중복: " + normalizedKey);
                }
            });
        }
        this.configured = Map.copyOf(normalizedConfigured);
        this.endpointPolicy = Objects.requireNonNull(endpointPolicy, "endpointPolicy");
        this.addressResolver = Objects.requireNonNull(addressResolver, "addressResolver");
    }

    /**
     * Source compatibility API. 신규 transport는 {@link #resolvedEndpoint(String)}의 pinned address를
     * 실제 connector에 사용해야 합니다.
     */
    public String baseUrl(String serviceId) {
        return resolvedEndpoint(serviceId).baseUrl();
    }

    /** DNS 검증 결과와 실제 Socket 연결에 사용할 pinned address를 함께 반환합니다. */
    public ResolvedEndpoint resolvedEndpoint(String serviceId) {
        String id = normalize(serviceId);
        RuntimeEndpoint dynamic = runtime.get().endpoints().get(id);
        if (dynamic != null) {
            if (!dynamic.active() || dynamic.maintenance()) {
                throw missing(id, "Runtime endpoint가 비활성/점검 상태입니다.");
            }
            return resolve(id, dynamic.baseUrl(), dynamic.attributes());
        }
        CpfServiceEndpointProperties.ServiceEndpoint endpoint = configured.get(id);
        if (endpoint == null || !hasText(endpoint.getBaseUrl())) {
            throw missing(id, "Service endpoint is not configured.");
        }
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("allowDns", Boolean.toString(endpoint.isAllowDns()));
        attributes.put("allowPrivate", Boolean.toString(endpoint.isAllowPrivate()));
        attributes.put("allowPublic", Boolean.toString(endpoint.isAllowPublic()));
        attributes.put("requireTls", Boolean.toString(endpoint.isRequireTls()));
        attributes.put("allowedCidrs", join(endpoint.getAllowedCidrs()));
        attributes.put("allowedPorts", joinIntegers(endpoint.getAllowedPorts()));
        attributes.put("pinnedAddresses", join(endpoint.getPinnedAddresses()));
        return resolve(id, endpoint.getBaseUrl(), attributes);
    }

    /** Service Call Engine이 반환한 URL도 동일한 DNS/pinning 정책으로 검증합니다. */
    public ResolvedEndpoint resolvedEndpoint(String serviceId, String baseUrl, Map<String, ?> attributes) {
        Map<String, String> normalized = new LinkedHashMap<>();
        if (attributes != null) {
            attributes.forEach((key, value) -> {
                if (key != null && value != null) normalized.put(key, String.valueOf(value));
            });
        }
        RuntimeEndpoint runtimeEndpoint = runtimeEndpoint(serviceId);
        if (runtimeEndpoint != null) {
            runtimeEndpoint.attributes().forEach(normalized::putIfAbsent);
        }
        // 운영자가 선언한 cpf.services.<id> 네트워크 정책은 어느 해석 경로로 들어오든 같게 적용해야
        // 한다. 이 경로(Service Call/Domain Call 이 Registry 에서 baseUrl 을 얻어오는 경로)만 그 선언을
        // 무시하고 secureDefault(requireTls=true) 로 판정해, 선언한 정책이 조용히 사라졌다.
        // 실제로 loopback http Domain 을 호출하는 Batch->Domain 검증이 "endpoint scheme은 TLS 정책에
        // 맞는 http(s)만 허용" 으로 막혔고, 이를 완화할 수 있는 지원 수단이 없었다.
        // 우선순위는 caller attributes > runtime endpoint > 선언된 service 설정 순으로 유지한다.
        CpfServiceEndpointProperties.ServiceEndpoint declared = configured.get(normalize(serviceId));
        if (declared != null) {
            normalized.putIfAbsent("allowDns", Boolean.toString(declared.isAllowDns()));
            normalized.putIfAbsent("allowPrivate", Boolean.toString(declared.isAllowPrivate()));
            normalized.putIfAbsent("allowPublic", Boolean.toString(declared.isAllowPublic()));
            normalized.putIfAbsent("requireTls", Boolean.toString(declared.isRequireTls()));
            normalized.putIfAbsent("allowedCidrs", join(declared.getAllowedCidrs()));
            normalized.putIfAbsent("allowedPorts", joinIntegers(declared.getAllowedPorts()));
            normalized.putIfAbsent("pinnedAddresses", join(declared.getPinnedAddresses()));
        }
        return resolve(normalize(serviceId), baseUrl, normalized);
    }

    public RuntimeEndpoint runtimeEndpoint(String serviceId) {
        return runtime.get().endpoints().get(normalize(serviceId));
    }

    public Snapshot runtimeSnapshot() {
        return runtime.get();
    }

    public Snapshot replaceRuntime(long version, Map<String, RuntimeEndpoint> endpoints) {
        if (version < 0) throw new IllegalArgumentException("endpoint registry version 범위 오류");
        LinkedHashMap<String, RuntimeEndpoint> normalized = new LinkedHashMap<>();
        if (endpoints != null) {
            endpoints.forEach((key, value) -> {
                if (value == null) throw new IllegalArgumentException("null endpoint 금지");
                String id = normalize(key == null || key.isBlank() ? value.serviceId() : key);
                RuntimeEndpoint candidate = value.normalize(id);
                // Snapshot 반영 전에 URL 문법과 정책 속성을 검증하되 DNS는 조회 시점에 다시 수행합니다.
                policy(candidate.attributes()).validateEndpoint(normalizedBaseUri(candidate.baseUrl()).toString());
                if (normalized.putIfAbsent(id, candidate) != null) {
                    throw new IllegalArgumentException("service endpoint 중복");
                }
            });
        }
        Map<String, RuntimeEndpoint> immutable = Map.copyOf(normalized);
        while (true) {
            Snapshot old = runtime.get();
            if (version < old.version()) throw new IllegalArgumentException("endpoint registry version 역행 금지");
            if (version == old.version()) {
                if (old.endpoints().equals(immutable)) return old;
                throw new IllegalArgumentException("endpoint registry 동일 version snapshot 충돌");
            }
            Snapshot next = new Snapshot(version, immutable);
            if (runtime.compareAndSet(old, next)) return next;
        }
    }

    private ResolvedEndpoint resolve(String serviceId, String rawBaseUrl, Map<String, String> attributes) {
        URI base = normalizedBaseUri(rawBaseUrl);
        CpfNetworkEndpointPolicy policy = policy(attributes);
        CpfNetworkEndpointPolicy.EndpointDecision decision = policy.validateEndpoint(base.toString());
        List<InetAddress> addresses;
        try {
            if (decision.literalAddress()) {
                CpfNetworkEndpointPolicy.Address parsed = CpfNetworkEndpointPolicy.Address.parse(base.getHost());
                addresses = List.of(InetAddress.getByAddress(parsed.bytes()));
            } else {
                InetAddress[] resolved = addressResolver.resolve(base.getHost());
                if (resolved == null || resolved.length == 0) {
                    throw new IllegalArgumentException("DNS resolved address가 없습니다: " + base.getHost());
                }
                addresses = java.util.Arrays.stream(resolved)
                        .distinct()
                        .sorted(Comparator.comparing(value -> value.getHostAddress()))
                        .toList();
                policy.validateResolvedAddresses(
                        base.getHost(), addresses.stream().map(value -> value.getHostAddress()).toList());
            }
        } catch (UnknownHostException failure) {
            throw new IllegalArgumentException("Service endpoint DNS 조회 실패: " + serviceId, failure);
        }
        rejectMixedAddressClasses(addresses);
        requireConfiguredPins(attributes, addresses);
        InetAddress pinned = addresses.getFirst();
        return new ResolvedEndpoint(
                serviceId,
                trimTrailingSlash(base.toString()),
                base,
                pinned,
                effectivePort(base),
                authority(base),
                addresses.stream().map(value -> value.getHostAddress()).toList());
    }

    private CpfNetworkEndpointPolicy policy(Map<String, String> attributes) {
        Map<String, String> safeAttributes = attributes == null ? Map.of() : attributes;
        boolean custom = safeAttributes.keySet().stream().anyMatch(key -> Set.of(
                "allowDns", "allowPrivate", "allowPublic", "requireTls", "allowedCidrs", "allowedPorts").contains(key));
        if (!custom) return endpointPolicy;
        boolean allowDns = bool(safeAttributes, "allowDns", false);
        boolean allowPrivate = bool(safeAttributes, "allowPrivate", false);
        boolean allowPublic = bool(safeAttributes, "allowPublic", true);
        boolean requireTls = bool(safeAttributes, "requireTls", true);
        List<String> cidrs = csv(safeAttributes.get("allowedCidrs"));
        List<Integer> ports = csv(safeAttributes.get("allowedPorts")).stream().map(value -> {
            try { return Integer.parseInt(value); }
            catch (NumberFormatException failure) { throw new IllegalArgumentException("allowedPorts 형식 오류: " + value, failure); }
        }).toList();
        return new CpfNetworkEndpointPolicy(cidrs, ports, allowPrivate, allowPublic, allowDns, requireTls);
    }

    private void requireConfiguredPins(Map<String, String> attributes, List<InetAddress> addresses) {
        List<String> configuredPins = csv(attributes == null ? null : attributes.get("pinnedAddresses"));
        if (configuredPins.isEmpty()) return;
        Set<String> actual = addresses.stream().map(value -> value.getHostAddress()).map(CpfServiceEndpointRegistry::canonicalAddress)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        Set<String> expected = configuredPins.stream().map(CpfServiceEndpointRegistry::canonicalAddress)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (!expected.containsAll(actual)) {
            throw new IllegalArgumentException("Service endpoint DNS pin 불일치: resolved=" + actual + " configured=" + expected);
        }
    }

    private void rejectMixedAddressClasses(List<InetAddress> addresses) {
        boolean privateSeen = false;
        boolean publicSeen = false;
        for (InetAddress address : addresses) {
            CpfNetworkEndpointPolicy.Address parsed = CpfNetworkEndpointPolicy.Address.parse(address.getHostAddress());
            if (parsed.privateAddress()) privateSeen = true; else publicSeen = true;
        }
        if (privateSeen && publicSeen) {
            throw new IllegalArgumentException("Service endpoint DNS mixed private/public response 금지");
        }
    }

    private URI normalizedBaseUri(String value) {
        if (!hasText(value)) throw new IllegalArgumentException("baseUrl 필수");
        URI uri;
        try { uri = URI.create(value.trim()).normalize(); }
        catch (RuntimeException failure) { throw new IllegalArgumentException("baseUrl 형식 오류", failure); }
        if (uri.getHost() == null || uri.getUserInfo() != null || uri.getFragment() != null || uri.getQuery() != null) {
            throw new IllegalArgumentException("baseUrl authority/path 형식 오류");
        }
        return URI.create(trimTrailingSlash(uri.toString()));
    }

    private CpfFrameworkException missing(String id, String message) {
        return new CpfFrameworkException(CpfFrameworkErrorCode.SERVICE_ENDPOINT_NOT_FOUND, message, Map.of("serviceId", id));
    }

    private String normalize(String value) {
        if (!hasText(value)) throw missing("EMPTY", "Service id is required.");
        return normalizeConfiguredId(value);
    }

    private static String normalizeConfiguredId(String value) {
        if (!hasText(value)) throw new IllegalArgumentException("Service endpoint key 필수");
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean bool(Map<String, String> attributes, String key, boolean fallback) {
        if (attributes == null || !attributes.containsKey(key)) return fallback;
        return Boolean.parseBoolean(attributes.get(key));
    }

    private static List<String> csv(String value) {
        if (value == null || value.isBlank()) return List.of();
        List<String> result = new ArrayList<>();
        for (String item : value.split(",")) if (!item.isBlank()) result.add(item.trim());
        return List.copyOf(result);
    }

    private static String join(Collection<String> values) {
        return values == null ? "" : String.join(",", values);
    }

    private static String joinIntegers(Collection<Integer> values) {
        return values == null ? "" : values.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
    }

    private static String canonicalAddress(String value) {
        return CpfNetworkEndpointPolicy.Address.parse(value).canonical();
    }

    private static int effectivePort(URI uri) {
        return uri.getPort() > 0 ? uri.getPort() : ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80);
    }

    private static String authority(URI uri) {
        int port = effectivePort(uri);
        boolean defaultPort = ("https".equalsIgnoreCase(uri.getScheme()) && port == 443)
                || ("http".equalsIgnoreCase(uri.getScheme()) && port == 80);
        return defaultPort ? uri.getHost() : uri.getHost() + ":" + port;
    }

    private static String trimTrailingSlash(String value) {
        String result = value;
        while (result.endsWith("/")) result = result.substring(0, result.length() - 1);
        return result;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @FunctionalInterface
    interface AddressResolver {
        InetAddress[] resolve(String host) throws UnknownHostException;
    }

    public record ResolvedEndpoint(
            String serviceId,
            String baseUrl,
            URI baseUri,
            InetAddress pinnedAddress,
            int port,
            String authority,
            List<String> validatedAddresses) {
        public ResolvedEndpoint {
            validatedAddresses = validatedAddresses == null ? List.of() : List.copyOf(validatedAddresses);
            Objects.requireNonNull(pinnedAddress, "pinnedAddress");
        }
    }

    public record RuntimeEndpoint(
            String serviceId,
            String endpointType,
            String baseUrl,
            String credentialRef,
            String layoutId,
            String layoutVersion,
            int timeoutMillis,
            boolean active,
            boolean maintenance,
            Map<String, String> attributes) {
        public RuntimeEndpoint {
            attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
            if (timeoutMillis < 1 || timeoutMillis > 300000) throw new IllegalArgumentException("external timeout 범위 오류");
        }

        private RuntimeEndpoint normalize(String id) {
            return new RuntimeEndpoint(
                    id,
                    Objects.toString(endpointType, "HTTP").trim().toUpperCase(Locale.ROOT),
                    baseUrl == null ? "" : baseUrl.trim(),
                    credentialRef == null ? "" : credentialRef.trim(),
                    layoutId == null ? "" : layoutId.trim(),
                    layoutVersion == null ? "" : layoutVersion.trim(),
                    timeoutMillis,
                    active,
                    maintenance,
                    attributes);
        }
    }

    public record Snapshot(long version, Map<String, RuntimeEndpoint> endpoints) {
        public Snapshot {
            endpoints = endpoints == null ? Map.of() : Map.copyOf(endpoints);
        }
        private static Snapshot empty() { return new Snapshot(0, Map.of()); }
    }
}
