package com.cpf.starter.observability.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.starter.runtimecontrol.spi.CpfRuntimePayloadReader;
import com.cpf.core.api.logging.CpfLogLevel;
import com.cpf.core.api.logging.DynamicLogLevelRule;
import com.cpf.core.common.logging.DynamicTransactionLogLevelService;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 거래별 동적 로그 레벨 전체 snapshot을 실제 Runtime 서비스에 교체합니다. */
public final class CpfDynamicLogLevelRuntimeApplier implements CpfRuntimeChangeApplier {
    public static final String CHANGE_TYPE = "LOG_LEVEL";
    private final DynamicTransactionLogLevelService service;

    public CpfDynamicLogLevelRuntimeApplier(DynamicTransactionLogLevelService service) {
        this.service = service;
    }

    @Override

    public String changeType() { return CHANGE_TYPE; }
    @Override
    public boolean supportsIdempotentReplay() { return true; }
    @Override
    public boolean snapshotCapable() { return true; }

    @Override
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        JsonNode rawRules = CpfRuntimePayloadReader.field(delivery.payload(),"rules");
        if (!rawRules.isArray()) {
            return CpfRuntimeApplyResult.failure("LOG_LEVEL_RULES_REQUIRED", "로그 레벨 snapshot rules가 필요합니다.");
        }
        List<DynamicLogLevelRule> rules = new ArrayList<>();
        try {
            for (JsonNode item : rawRules) {
                if (!item.isObject()) throw new IllegalArgumentException("rule object required");
                String ruleId = required(item, "ruleId");
                String level = required(item, "logLevel");
                String reason = required(item, "reason");
                String createdBy = required(item, "createdBy");
                LocalDateTime createdAt = LocalDateTime.parse(required(item, "createdAt"));
                LocalDateTime expiresAt = LocalDateTime.parse(required(item, "expiresAt"));
                if (!expiresAt.isAfter(createdAt) || !expiresAt.isAfter(LocalDateTime.now())) continue;
                String transactionId = optional(item, "transactionId");
                String businessTransactionId = optional(item, "businessTransactionId");
                if ((transactionId == null || transactionId.isBlank())
                        && (businessTransactionId == null || businessTransactionId.isBlank())) {
                    throw new IllegalArgumentException("transactionId or businessTransactionId required");
                }
                rules.add(new DynamicLogLevelRule(ruleId, normalize(transactionId), normalize(businessTransactionId),
                        normalize(optional(item, "moduleId")), CpfLogLevel.valueOf(level.trim().toUpperCase()),
                        reason.trim(), createdBy.trim(), createdAt, expiresAt));
            }
            List<DynamicLogLevelRule> previous = service.findActiveRules();
            Map<String, DynamicLogLevelRule> expected = byId(rules);
            try {
                service.replaceAll(List.copyOf(rules));
                if (!byId(service.findActiveRules()).equals(expected)) {
                    return rollback(previous, "LOG_LEVEL_SNAPSHOT_NOT_CONFIRMED",
                            "동적 로그 레벨 snapshot 교체를 확인하지 못했습니다.");
                }
                return CpfRuntimeApplyResult.success(delivery.payloadHash());
            } catch (RuntimeException applyFailure) {
                return rollback(previous, "LOG_LEVEL_APPLY_FAILED", "동적 로그 레벨 적용 중 오류가 발생했습니다.");
            }
        } catch (IllegalArgumentException ex) {
            return CpfRuntimeApplyResult.failure("LOG_LEVEL_PAYLOAD_INVALID", "동적 로그 레벨 payload가 유효하지 않습니다.");
        }
    }

    private CpfRuntimeApplyResult rollback(List<DynamicLogLevelRule> previous, String code, String message) {
        try {
            service.replaceAll(previous);
            if (!byId(service.findActiveRules()).equals(byId(previous))) {
                return CpfRuntimeApplyResult.unknown(code + "_ROLLBACK_UNKNOWN",
                        message + " 이전 snapshot 복원 결과를 확인할 수 없습니다.");
            }
            return CpfRuntimeApplyResult.failure(code, message + " 이전 snapshot으로 복원했습니다.");
        } catch (RuntimeException rollbackFailure) {
            return CpfRuntimeApplyResult.unknown(code + "_ROLLBACK_UNKNOWN",
                    message + " 이전 snapshot 복원 결과를 확인할 수 없습니다.");
        }
    }

    private Map<String, DynamicLogLevelRule> byId(List<DynamicLogLevelRule> values) {
        return values.stream().collect(Collectors.toUnmodifiableMap(
                DynamicLogLevelRule::ruleId, Function.identity(), (left, right) -> right));
    }

    private String required(JsonNode source, String key) {
        String value = optional(source, key);
        if (value == null || value.isBlank()) throw new IllegalArgumentException(key + " required");
        return value;
    }

    private String optional(JsonNode source, String key) {
        JsonNode value = source.get(key);
        return value == null || value.isNull() || value.isMissingNode() ? null : value.asText();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase();
    }
}
