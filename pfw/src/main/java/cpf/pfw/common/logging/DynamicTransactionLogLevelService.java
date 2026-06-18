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
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
@Service
public class DynamicTransactionLogLevelService {
    private final ConcurrentMap<String, DynamicLogLevelRule> rules = new ConcurrentHashMap<>();

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    public DynamicLogLevelRule register(DynamicLogLevelRequest request) {
        if (!hasText(request.getTransactionId()) && !hasText(request.getBusinessTransactionId())) {
            throw new CpfFrameworkException(
                    CpfFrameworkErrorCode.DYNAMIC_LOG_RULE_INVALID,
                    "CPF 처리 기준입니다.",
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
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
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
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    public List<DynamicLogLevelRule> findActiveRules() {
        cleanupExpired();
        return rules.values().stream()
                .sorted(Comparator.comparing(DynamicLogLevelRule::createdAt).reversed())
                .toList();
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    public boolean remove(String ruleId) {
        return rules.remove(ruleId) != null;
    }

    /**
     * CPF 기능 설명입니다.
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

