package cpf.pfw.common.logging;

import cpf.pfw.common.exception.CpfFrameworkErrorCode;
import cpf.pfw.common.exception.CpfFrameworkException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 거래별 동적 로그 레벨을 런타임 메모리에 보관하고 조회합니다.
 *
 * <p>ADM에서 등록한 규칙은 DB 또는 broker 동기화를 통해 이 서비스로 반영됩니다.
 * 현재 클래스는 실제 로그 출력 전에 적용할 최종 런타임 규칙을 결정하는 책임만 가집니다.</p>
 */
@Service
public class DynamicTransactionLogLevelService {
    private final ConcurrentMap<String, DynamicLogLevelRule> rules = new ConcurrentHashMap<>();

    /**
     * 운영 요청을 검증한 뒤 TTL이 있는 런타임 규칙으로 등록합니다.
     */
    public DynamicLogLevelRule register(DynamicLogLevelRequest request) {
        if (!hasText(request.getTransactionId()) && !hasText(request.getBusinessTransactionId())) {
            throw new CpfFrameworkException(
                    CpfFrameworkErrorCode.DYNAMIC_LOG_RULE_INVALID,
                    "동적 로그 레벨은 트랜잭션 ID 또는 업무 거래 ID 중 하나가 필요합니다.",
                    Map.of("requiredFields", "transactionId,businessTransactionId"));
        }

        CpfLogLevel logLevel = request.getLogLevel() == null ? CpfLogLevel.DEBUG : request.getLogLevel();
        Duration ttl = request.getTtl() == null || request.getTtl().isNegative() || request.getTtl().isZero()
                ? Duration.ofMinutes(10)
                : request.getTtl();
        LocalDateTime now = LocalDateTime.now();
        DynamicLogLevelRule rule = new DynamicLogLevelRule(
                UUID.randomUUID().toString(),
                normalize(request.getTransactionId()),
                normalize(request.getBusinessTransactionId()),
                normalize(request.getModuleId()),
                logLevel,
                request.getReason(),
                hasText(request.getRequestUser()) ? request.getRequestUser() : "SYSTEM",
                now,
                now.plus(ttl));

        rules.put(rule.ruleId(), rule);
        return rule;
    }

    public void upsert(DynamicLogLevelRule rule) {
        if (rule == null || !hasText(rule.ruleId())) {
            return;
        }
        if (rule.expired(LocalDateTime.now())) {
            rules.remove(rule.ruleId());
            return;
        }
        rules.put(rule.ruleId(), rule);
    }

    public void replaceAll(List<DynamicLogLevelRule> activeRules) {
        rules.clear();
        if (activeRules == null) {
            return;
        }
        activeRules.forEach(this::upsert);
    }

    /**
     * 현재 거래 조건에 가장 최근 등록된 유효 규칙을 찾습니다.
     *
     * <p>트랜잭션 ID 또는 업무 거래 ID가 일치하고, 모듈 조건이 비어 있거나 같은 경우만
     * 적용 대상으로 봅니다.</p>
     */
    public Optional<DynamicLogLevelRule> resolve(String transactionId, String businessTransactionId, String moduleId) {
        cleanupExpired();
        String normalizedTransactionId = normalize(transactionId);
        String normalizedBusinessTransactionId = normalize(businessTransactionId);
        String normalizedModuleId = normalize(moduleId);

        return rules.values().stream()
                .filter(rule -> matches(rule, normalizedTransactionId, normalizedBusinessTransactionId, normalizedModuleId))
                .max(Comparator.comparing(DynamicLogLevelRule::createdAt));
    }

    /**
     * 만료되지 않은 런타임 규칙을 최신 등록 순으로 조회합니다.
     */
    public List<DynamicLogLevelRule> findActiveRules() {
        cleanupExpired();
        return rules.values().stream()
                .sorted(Comparator.comparing(DynamicLogLevelRule::createdAt).reversed())
                .toList();
    }

    /**
     * 규칙 ID로 런타임 규칙을 제거합니다.
     */
    public boolean remove(String ruleId) {
        return rules.remove(ruleId) != null;
    }

    /**
     * 모든 런타임 규칙을 비웁니다.
     */
    public void clear() {
        rules.clear();
    }

    private boolean matches(
            DynamicLogLevelRule rule,
            String transactionId,
            String businessTransactionId,
            String moduleId) {

        boolean transactionMatched = hasText(rule.transactionId()) && rule.transactionId().equals(transactionId);
        boolean businessMatched = hasText(rule.businessTransactionId()) && rule.businessTransactionId().equals(businessTransactionId);
        boolean moduleMatched = !hasText(rule.moduleId()) || rule.moduleId().equals(moduleId);
        return (transactionMatched || businessMatched) && moduleMatched;
    }

    private void cleanupExpired() {
        LocalDateTime now = LocalDateTime.now();
        rules.entrySet().removeIf(entry -> entry.getValue().expired(now));
    }

    private String normalize(String value) {
        return hasText(value) ? value.trim().toUpperCase() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

