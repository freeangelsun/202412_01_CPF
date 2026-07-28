package com.cpf.core.common.runtimecontrol.applier;

import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.common.logging.CpfLogLevel;
import com.cpf.core.common.logging.DynamicLogLevelRule;
import com.cpf.core.common.logging.DynamicTransactionLogLevelService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 거래별 동적 로그 레벨 전체 snapshot을 실제 Runtime 서비스에 교체합니다. */
public final class CpfDynamicLogLevelRuntimeApplier implements CpfRuntimeChangeApplier {
    public static final String CHANGE_TYPE = "LOG_LEVEL";
    private final DynamicTransactionLogLevelService service;

    public CpfDynamicLogLevelRuntimeApplier(DynamicTransactionLogLevelService service) {
        this.service = service;
    }

    @Override public String changeType() { return CHANGE_TYPE; }
    @Override public boolean supportsIdempotentReplay() { return true; }
    @Override public boolean snapshotCapable() { return true; }

    @Override
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        Object rawRules = delivery.payload().get("rules");
        if (!(rawRules instanceof List<?> list)) {
            return CpfRuntimeApplyResult.failure("LOG_LEVEL_RULES_REQUIRED", "로그 레벨 snapshot rules가 필요합니다.");
        }
        List<DynamicLogLevelRule> rules = new ArrayList<>();
        try {
            for (Object item : list) {
                if (!(item instanceof Map<?, ?> map)) {
                    throw new IllegalArgumentException("rule object required");
                }
                String ruleId = required(map, "ruleId");
                String level = required(map, "logLevel");
                String reason = required(map, "reason");
                String createdBy = required(map, "createdBy");
                LocalDateTime createdAt = LocalDateTime.parse(required(map, "createdAt"));
                LocalDateTime expiresAt = LocalDateTime.parse(required(map, "expiresAt"));
                if (!expiresAt.isAfter(createdAt) || !expiresAt.isAfter(LocalDateTime.now())) {
                    continue;
                }
                String transactionId = optional(map, "transactionId");
                String businessTransactionId = optional(map, "businessTransactionId");
                if ((transactionId == null || transactionId.isBlank())
                        && (businessTransactionId == null || businessTransactionId.isBlank())) {
                    throw new IllegalArgumentException("transactionId or businessTransactionId required");
                }
                rules.add(new DynamicLogLevelRule(
                        ruleId,
                        normalize(transactionId),
                        normalize(businessTransactionId),
                        normalize(optional(map, "moduleId")),
                        CpfLogLevel.valueOf(level.trim().toUpperCase()),
                        reason.trim(),
                        createdBy.trim(),
                        createdAt,
                        expiresAt));
            }
            service.replaceAll(List.copyOf(rules));
            if (service.findActiveRules().size() != rules.size()) {
                return CpfRuntimeApplyResult.failure("LOG_LEVEL_SNAPSHOT_NOT_CONFIRMED", "동적 로그 레벨 snapshot 교체를 확인하지 못했습니다.");
            }
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure("LOG_LEVEL_PAYLOAD_INVALID", "동적 로그 레벨 payload가 유효하지 않습니다.");
        }
    }

    private String required(Map<?, ?> map, String key) {
        String value = optional(map, key);
        if (value == null || value.isBlank()) throw new IllegalArgumentException(key + " required");
        return value;
    }

    private String optional(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase();
    }
}
