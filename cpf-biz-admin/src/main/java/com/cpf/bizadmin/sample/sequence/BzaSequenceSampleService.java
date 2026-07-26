package com.cpf.bizadmin.sample.sequence;

import com.cpf.bizadmin.common.base.BzaBaseService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/** 선택형 BZA 업무 채번 Customization Sample. 기본 비활성입니다. */
@Service
@ConditionalOnProperty(prefix = "cpf.bza.sample.sequence", name = "enabled", havingValue = "true")
public class BzaSequenceSampleService extends BzaBaseService {
    private final BzaSequenceSampleRepository repository;

    public BzaSequenceSampleService(BzaSequenceSampleRepository repository) {
        this.repository = repository;
    }

    public List<Map<String, Object>> rules() {
        return repository.rules();
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> save(RuleRequest request, String user) {
        String code = required(request.ruleCode(), "ruleCode");
        if (request.paddingLength() < 1 || request.paddingLength() > 20) {
            throw new IllegalArgumentException("paddingLength는 1~20이어야 합니다.");
        }
        repository.save(
                code,
                required(request.ruleName(), "ruleName"),
                request.prefix() == null ? "" : request.prefix(),
                Math.max(0, request.currentValue()),
                request.paddingLength(),
                "N".equalsIgnoreCase(request.useYn()) ? "N" : "Y",
                required(user, "operatorId"));
        return repository.rule(code);
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> issue(String code, String user, String reason) {
        String ruleCode = required(code, "ruleCode");
        String operatorId = required(user, "operatorId");
        String auditReason = required(reason, "시험 발급 사유");
        Map<String, Object> rule = repository.ruleForUpdate(ruleCode);
        if ("N".equalsIgnoreCase(String.valueOf(rule.get("useYn")))) {
            throw new IllegalStateException("비활성 채번 규칙은 발급할 수 없습니다.");
        }
        long current = ((Number) rule.get("currentValue")).longValue() + 1L;
        int padding = ((Number) rule.get("paddingLength")).intValue();
        String prefix = String.valueOf(rule.getOrDefault("prefix", ""));
        String issued = prefix + String.format(Locale.ROOT, "%0" + padding + "d", current);
        repository.updateCurrentValue(ruleCode, current, operatorId);
        repository.appendIssue(ruleCode, issued, operatorId, auditReason);
        return Map.of("ruleCode", ruleCode, "issuedValue", issued, "currentValue", current);
    }

    public List<Map<String, Object>> history(String code, int limit) {
        return repository.history(code, limit);
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " 필수");
        return value.trim();
    }

    public record RuleRequest(
            String ruleCode,
            String ruleName,
            String prefix,
            long currentValue,
            int paddingLength,
            String useYn) {}
}
