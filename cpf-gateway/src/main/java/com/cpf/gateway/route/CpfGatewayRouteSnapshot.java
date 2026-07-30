package com.cpf.gateway.route;

import com.cpf.core.api.gateway.CpfGatewayRoute;
import com.cpf.core.api.gateway.CpfGatewayRouteProvider;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 매 요청마다 ADM 또는 DB를 조회하지 않도록 마지막 정상 route snapshot을 메모리에 유지합니다.
 */
@Component
public class CpfGatewayRouteSnapshot {
    private static final Logger log = LoggerFactory.getLogger(CpfGatewayRouteSnapshot.class);

    private final CpfGatewayRouteProvider provider;
    private final AtomicReference<Snapshot> current = new AtomicReference<>(new Snapshot(Map.of(), Instant.EPOCH));
    @Value("${cpf.gateway.allow-empty-routes:false}")
    private boolean allowEmptyRoutes;

    public CpfGatewayRouteSnapshot(CpfGatewayRouteProvider provider) {
        this.provider = provider;
    }

    @PostConstruct
    public void initialize() {
        Map<String, CpfGatewayRoute> routes = provider.loadPublicRoutes();
        if (routes == null) throw new IllegalStateException("Gateway public route snapshot 결과가 null입니다.");
        if (routes.isEmpty() && !allowEmptyRoutes) {
            Map<String, CpfGatewayRoute> candidate = provider.loadCandidateRoutes();
            if (candidate == null || candidate.isEmpty()) {
                throw new IllegalStateException("Gateway route snapshot이 비어 있어 기동을 중단합니다. 운영에서는 빈 route로 기동할 수 없습니다.");
            }
            log.warn("Apply ACK 이전 ACTIVE Gateway Candidate Snapshot으로 기동합니다. Route Synchronizer가 즉시 ACK를 기록합니다.");
            routes = candidate;
        }
        current.set(new Snapshot(Map.copyOf(routes), Instant.now()));
    }

    @Scheduled(fixedDelayString = "${cpf.gateway.route-refresh-millis:30000}")
    public void refresh() {
        try { refreshNow(); }
        catch (RuntimeException ex) { log.error("Gateway route snapshot 갱신에 실패해 마지막 정상본을 유지합니다.", ex); }
    }

    /** ACK 완료 Route 정본을 다시 읽어 Snapshot을 교체합니다. DB 오류 시 마지막 정상본을 유지합니다. */
    public Snapshot refreshNow() {
        Map<String, CpfGatewayRoute> routes = provider.loadPublicRoutes();
        if (routes == null) throw new IllegalStateException("Gateway route refresh 결과가 null입니다.");
        return activate(new Snapshot(Map.copyOf(routes), Instant.now()));
    }

    /** ACK 기록 전 ACTIVE Binding 전체를 검증한 Candidate Snapshot을 생성합니다. */
    public Snapshot prepareCandidate() {
        Map<String, CpfGatewayRoute> routes = provider.loadCandidateRoutes();
        if (routes == null) throw new IllegalStateException("Gateway candidate route 결과가 null입니다.");
        return new Snapshot(Map.copyOf(routes), Instant.now());
    }

    /** 검증된 Candidate를 단일 AtomicReference 교체로 활성화합니다. */
    public Snapshot activate(Snapshot replacement) {
        if (replacement == null || replacement.routes() == null) {
            throw new IllegalArgumentException("Gateway replacement snapshot is required");
        }
        current.set(new Snapshot(Map.copyOf(replacement.routes()), replacement.loadedAt()));
        return current.get();
    }

    public Snapshot current() {
        return current.get();
    }

    public CpfGatewayRoute resolve(String executionId) {
        return provider.resolve(current.get().routes(), executionId);
    }


    /** Host·Path·Method·API Version으로 승인·적용 완료 Route를 결정합니다. 일치 항목이 없으면 Default Deny입니다. */
    public CpfGatewayRoute resolveRequest(
            String environmentCode, String host, String path, String method, String apiVersion) {
        String environment = blankTo(environmentCode, "DEFAULT");
        String requestHost = blankTo(host, "");
        String requestPath = path == null || path.isBlank() ? "/" : path;
        String requestMethod = blankTo(method, "GET");
        String version = blankTo(apiVersion, "v1");
        return current.get().routes().values().stream()
                .filter(CpfGatewayRoute::enabled)
                .filter(route -> route.environmentCode().equalsIgnoreCase(environment))
                .filter(route -> matchesHost(route.hostPattern(), requestHost))
                .filter(route -> matchesPath(route.pathPattern(), requestPath))
                .filter(route -> "*".equals(route.httpMethod()) || route.httpMethod().equalsIgnoreCase(requestMethod))
                .filter(route -> "*".equals(route.apiVersion()) || route.apiVersion().equalsIgnoreCase(version))
                .sorted(java.util.Comparator
                        .comparingInt((CpfGatewayRoute route) -> specificity(route.hostPattern(), route.pathPattern()))
                        .reversed()
                        .thenComparing(CpfGatewayRoute::routeId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "승인·적용 완료된 Gateway Route가 없습니다. host=" + requestHost
                                + ", path=" + requestPath + ", method=" + requestMethod));
    }

    private static boolean matchesHost(String pattern, String host) {
        if (pattern == null || pattern.isBlank() || "*".equals(pattern)) return true;
        if (pattern.startsWith("*.")) return host.endsWith(pattern.substring(1));
        return pattern.equalsIgnoreCase(host);
    }

    private static boolean matchesPath(String pattern, String path) {
        if (pattern == null || pattern.isBlank() || "/**".equals(pattern) || "*".equals(pattern)) return true;
        if (pattern.endsWith("/**")) return path.startsWith(pattern.substring(0, pattern.length() - 3));
        if (pattern.endsWith("/*")) {
            String prefix = pattern.substring(0, pattern.length() - 1);
            if (!path.startsWith(prefix)) return false;
            return path.indexOf('/', prefix.length()) < 0;
        }
        return pattern.equals(path);
    }

    private static int specificity(String host, String path) {
        int score = (host == null || "*".equals(host)) ? 0 : host.length() * 10;
        score += path == null ? 0 : path.replace("*", "").length();
        return score;
    }

    private static String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    public record Snapshot(Map<String, CpfGatewayRoute> routes, Instant loadedAt) {
    }
}
