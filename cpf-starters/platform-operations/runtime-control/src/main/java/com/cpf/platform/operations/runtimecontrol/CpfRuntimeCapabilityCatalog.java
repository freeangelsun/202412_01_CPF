package com.cpf.platform.operations.runtimecontrol;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * ADM 실시간 운영·제어에서 독립적으로 관리해야 하는 Runtime Capability 정본입니다.
 *
 * <p>화면이나 Controller가 문자열 목록을 중복 보유하지 않고 이 Public API를 사용해
 * 대상, 위험도, 승인 필요 여부와 필수 실행 결과를 일관되게 표현합니다.</p>
 */
public final class CpfRuntimeCapabilityCatalog {
    private static final List<Capability> CAPABILITIES = List.of(
            capability("COMMON_CODE", "공통코드", false, "CODE", "COMMON_CODE", "REFERENCE"),
            capability("MESSAGE_RESPONSE_CODE", "메시지·응답코드", false, "MESSAGE", "RESPONSE_CODE"),
            capability("CONFIG_PARAMETER_FEATURE_FLAG", "설정·Parameter·Feature Flag", true,
                    "CONFIG", "PARAMETER", "FEATURE_FLAG"),
            capability("CACHE", "Cache", true, "CACHE", "REFRESH", "INVALIDATE"),
            capability("ONLINE_TRANSACTION", "온라인 거래", true,
                    "TRANSACTION", "MAINTENANCE", "DRAIN", "TRACE_BOOST"),
            capability("SERVICE_CALL", "Service Call·Remote Call", true,
                    "SERVICE_CALL", "REMOTE_CALL", "ENDPOINT", "FAILOVER"),
            capability("GATEWAY", "Gateway", true, "GATEWAY", "ROUTE", "CANARY"),
            capability("LOG_TRACE", "로그·추적", true, "LOG", "TRACE", "MASKING"),
            capability("BATCH_RUNTIME", "Batch·Scheduler·Worker·Center-Cut·Agent", true,
                    "BATCH", "SCHEDULER", "WORKER", "CENTER_CUT", "AGENT"),
            capability("SECURITY_RUNTIME_POLICY", "Security Runtime Policy", true,
                    "SECURITY", "PASSWORD", "PERMISSION", "JWT", "CERTIFICATE", "SECRET"),
            capability("EXTERNAL_INTEGRATION", "외부연계·Messaging·File", true,
                    "EXTERNAL", "BROKER", "MESSAGING", "FILE", "SFTP", "WEBHOOK"),
            capability("INSTANCE_MANAGEMENT", "인스턴스 관리", true,
                    "INSTANCE", "GROUP", "MEMBERSHIP", "LEASE", "FENCING"),
            capability("INSTANCE_LOG_DOWNLOAD", "인스턴스별 로그 다운로드", true,
                    "LOG_DOWNLOAD", "DOWNLOAD", "ARCHIVE"),
            capability("OBSERVABILITY_NOTIFICATION", "관제·알림", true,
                    "NOTIFICATION", "ALERT", "SILENCE", "ESCALATION"));

    private CpfRuntimeCapabilityCatalog() {
    }

    public static List<Capability> capabilities() {
        return CAPABILITIES;
    }

    public static Optional<Capability> resolve(String changeType) {
        if (changeType == null || changeType.isBlank()) {
            return Optional.empty();
        }
        String normalized = normalize(changeType);
        Optional<Capability> exact = CAPABILITIES.stream()
                .filter(capability -> capability.code().equals(normalized))
                .findFirst();
        if (exact.isPresent()) {
            return exact;
        }
        return CAPABILITIES.stream()
                .flatMap(capability -> capability.aliases().stream()
                        .filter(normalized::contains)
                        .map(alias -> new Match(capability, alias.length())))
                .max(java.util.Comparator.comparingInt(value -> value.aliasLength()))
                .map(value -> value.capability());
    }

    public static boolean requiresApproval(String changeType) {
        return resolve(changeType).map(value -> value.approvalRequired()).orElse(true);
    }

    public static Map<String, Object> describe(String changeType) {
        return resolve(changeType)
                .<Map<String, Object>>map(capability -> Map.of(
                        "code", capability.code(),
                        "displayName", capability.displayName(),
                        "approvalRequired", capability.approvalRequired(),
                        "aliases", capability.aliases()))
                .orElseGet(() -> Map.of(
                        "code", "CUSTOM",
                        "displayName", "등록되지 않은 Runtime 변경",
                        "approvalRequired", true,
                        "aliases", Set.of()));
    }

    private static Capability capability(
            String code,
            String displayName,
            boolean approvalRequired,
            String... aliases) {
        return new Capability(code, displayName, approvalRequired, Set.of(aliases));
    }

    private static String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }

    private record Match(Capability capability, int aliasLength) {
    }

    /** ADM과 Runtime Agent가 공유하는 Capability 설명입니다. */
    public record Capability(
            String code,
            String displayName,
            boolean approvalRequired,
            Set<String> aliases) {
        public Capability {
            aliases = Set.copyOf(aliases);
        }
    }
}
