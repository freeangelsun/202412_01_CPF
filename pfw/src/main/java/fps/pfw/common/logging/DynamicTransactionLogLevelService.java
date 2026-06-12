package fps.pfw.common.logging;

import fps.pfw.common.exception.FpsFrameworkErrorCode;
import fps.pfw.common.exception.FpsFrameworkException;
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
 * 특정 거래만 임시 로그레벨을 올리기 위한 PFW 서비스입니다.
 *
 * <p>현재 구현은 WAS 메모리 기반입니다. 추후 ADM 화면이 붙으면 이 서비스의 메서드는 유지하고,
 * 내부 저장소만 DB 또는 CMN 설정/캐시 기반 구현으로 바꾸면 됩니다. 이렇게 두면 업무 개발자는
 * 로그레벨 제어 방식이 바뀌어도 같은 API를 사용할 수 있습니다.</p>
 */
@Service
public class DynamicTransactionLogLevelService {
    private final ConcurrentMap<String, DynamicLogLevelRule> rules = new ConcurrentHashMap<>();

    /**
     * 동적 로그레벨 규칙을 등록합니다.
     *
     * @param request 등록 요청
     * @return 등록된 규칙
     */
    public DynamicLogLevelRule register(DynamicLogLevelRequest request) {
        if (!hasText(request.getTransactionId()) && !hasText(request.getBusinessTransactionId())) {
            throw new FpsFrameworkException(
                    FpsFrameworkErrorCode.DYNAMIC_LOG_RULE_INVALID,
                    "transactionId 또는 businessTransactionId 중 하나는 필수입니다.",
                    Map.of("requiredFields", "transactionId,businessTransactionId"));
        }

        FpsLogLevel logLevel = request.getLogLevel() == null ? FpsLogLevel.DEBUG : request.getLogLevel();
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

    /**
     * 현재 거래에 적용되는 동적 로그레벨 규칙을 찾습니다.
     *
     * @param transactionId         글로벌 거래ID
     * @param businessTransactionId 업무 거래ID
     * @param moduleId              주제영역 코드
     * @return 적용 규칙
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
     * 현재 등록된 규칙을 조회합니다.
     *
     * @return 만료되지 않은 규칙 목록
     */
    public List<DynamicLogLevelRule> findActiveRules() {
        cleanupExpired();
        return rules.values().stream()
                .sorted(Comparator.comparing(DynamicLogLevelRule::createdAt).reversed())
                .toList();
    }

    /**
     * 규칙 ID 기준으로 삭제합니다.
     *
     * @param ruleId 규칙 ID
     * @return 삭제되었으면 true
     */
    public boolean remove(String ruleId) {
        return rules.remove(ruleId) != null;
    }

    /**
     * 모든 규칙을 제거합니다.
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
