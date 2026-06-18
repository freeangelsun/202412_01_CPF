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
 * ?뱀젙 嫄곕옒留??꾩떆 濡쒓렇?덈꺼???щ━湲??꾪븳 PFW ?쒕퉬?ㅼ엯?덈떎.
 *
 * <p>?꾩옱 援ы쁽? WAS 硫붾え由?湲곕컲?낅땲?? 異뷀썑 ADM ?붾㈃??遺숈쑝硫????쒕퉬?ㅼ쓽 硫붿꽌?쒕뒗 ?좎??섍퀬,
 * ?대? ??μ냼留?DB ?먮뒗 CMN ?ㅼ젙/罹먯떆 湲곕컲 援ы쁽?쇰줈 諛붽씀硫??⑸땲?? ?대젃寃??먮㈃ ?낅Т 媛쒕컻?먮뒗
 * 濡쒓렇?덈꺼 ?쒖뼱 諛⑹떇??諛붾뚯뼱??媛숈? API瑜??ъ슜?????덉뒿?덈떎.</p>
 */
@Service
public class DynamicTransactionLogLevelService {
    private final ConcurrentMap<String, DynamicLogLevelRule> rules = new ConcurrentHashMap<>();

    /**
     * ?숈쟻 濡쒓렇?덈꺼 洹쒖튃???깅줉?⑸땲??
     *
     * @param request ?깅줉 ?붿껌
     * @return ?깅줉??洹쒖튃
     */
    public DynamicLogLevelRule register(DynamicLogLevelRequest request) {
        if (!hasText(request.getTransactionId()) && !hasText(request.getBusinessTransactionId())) {
            throw new CpfFrameworkException(
                    CpfFrameworkErrorCode.DYNAMIC_LOG_RULE_INVALID,
                    "transactionId ?먮뒗 businessTransactionId 以??섎굹???꾩닔?낅땲??",
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
     * ?꾩옱 嫄곕옒???곸슜?섎뒗 ?숈쟻 濡쒓렇?덈꺼 洹쒖튃??李얠뒿?덈떎.
     *
     * @param transactionId         湲濡쒕쾶 嫄곕옒ID
     * @param businessTransactionId ?낅Т 嫄곕옒ID
     * @param moduleId              二쇱젣?곸뿭 肄붾뱶
     * @return ?곸슜 洹쒖튃
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
     * ?꾩옱 ?깅줉??洹쒖튃??議고쉶?⑸땲??
     *
     * @return 留뚮즺?섏? ?딆? 洹쒖튃 ⑸줉
     */
    public List<DynamicLogLevelRule> findActiveRules() {
        cleanupExpired();
        return rules.values().stream()
                .sorted(Comparator.comparing(DynamicLogLevelRule::createdAt).reversed())
                .toList();
    }

    /**
     * 洹쒖튃 ID 湲곗??쇰줈 ??젣?⑸땲??
     *
     * @param ruleId 洹쒖튃 ID
     * @return ??젣?섏뿀?쇰㈃ true
     */
    public boolean remove(String ruleId) {
        return rules.remove(ruleId) != null;
    }

    /**
     * ⑤뱺 洹쒖튃???쒓굅?⑸땲??
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

