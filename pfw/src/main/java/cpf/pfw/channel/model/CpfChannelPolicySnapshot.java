package cpf.pfw.channel.model;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 요청 처리 중 잠금 없이 읽을 수 있는 불변 채널 정책 스냅샷입니다. */
public record CpfChannelPolicySnapshot(
        long version,
        Instant loadedAt,
        Map<String, CpfChannelDefinition> channels,
        List<CpfChannelExecutionPolicy> policies) {

    public CpfChannelPolicySnapshot {
        if (version < 0) {
            throw new IllegalArgumentException("스냅샷 버전은 0 이상이어야 합니다.");
        }
        loadedAt = loadedAt == null ? Instant.now() : loadedAt;
        channels = Map.copyOf(channels == null ? Map.of() : new LinkedHashMap<>(channels));
        policies = List.copyOf(policies == null ? List.of() : policies);
    }

    public Optional<CpfChannelExecutionPolicy> resolve(
            String standardExecutionId,
            String originalChannelCode,
            String callerChannelCode,
            String requestType,
            Instant evaluatedAt) {
        return policies.stream()
                .filter(policy -> policy.isEffectiveAt(evaluatedAt))
                .filter(policy -> matches(policy.standardExecutionId(), standardExecutionId))
                .filter(policy -> matches(policy.originalChannelCode(), originalChannelCode))
                .filter(policy -> matches(policy.callerChannelCode(), callerChannelCode))
                .filter(policy -> matches(policy.requestType(), requestType))
                .max(Comparator.comparingInt(policy -> specificity(
                        policy, standardExecutionId, originalChannelCode, callerChannelCode, requestType)));
    }

    public static CpfChannelPolicySnapshot localDefault() {
        Map<String, CpfChannelDefinition> definitions = new LinkedHashMap<>();
        definitions.put("ANY", new CpfChannelDefinition("ANY", "전체 채널", "SYSTEM", "INTERNAL",
                false, true, false, false, true, "정책 와일드카드", 0));
        definitions.put("WEB", new CpfChannelDefinition("WEB", "웹", "CLIENT", "EXTERNAL",
                true, false, true, false, true, "웹 브라우저 채널", 0));
        definitions.put("MOBILE", new CpfChannelDefinition("MOBILE", "모바일", "CLIENT", "EXTERNAL",
                true, false, true, false, true, "모바일 애플리케이션 채널", 0));
        definitions.put("ADM", new CpfChannelDefinition("ADM", "관리자", "OPERATOR", "INTERNAL",
                true, true, true, false, true, "ADM 운영 채널", 0));
        definitions.put("BATCH", new CpfChannelDefinition("BATCH", "배치", "SYSTEM", "INTERNAL",
                false, true, false, false, true, "배치 실행 채널", 0));
        CpfChannelExecutionPolicy fallback = new CpfChannelExecutionPolicy(
                "LOCAL.DEFAULT", "*", "ANY", "ANY", "*", true,
                false, false, 0, null, null, true, 0);
        return new CpfChannelPolicySnapshot(0, Instant.now(), definitions, List.of(fallback));
    }

    private boolean matches(String configured, String actual) {
        return "*".equals(configured) || "ANY".equals(configured) || configured.equalsIgnoreCase(actual);
    }

    private int specificity(
            CpfChannelExecutionPolicy policy,
            String executionId,
            String originalChannel,
            String callerChannel,
            String requestType) {
        int score = 0;
        score += policy.standardExecutionId().equalsIgnoreCase(executionId) ? 8 : 0;
        score += policy.originalChannelCode().equalsIgnoreCase(originalChannel) ? 4 : 0;
        score += policy.callerChannelCode().equalsIgnoreCase(callerChannel) ? 2 : 0;
        score += policy.requestType().equalsIgnoreCase(requestType) ? 1 : 0;
        return score;
    }
}
