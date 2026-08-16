package com.cpf.batch.runtime;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Set;

/** 실제 Consumer가 존재하는 Batch Runtime 변경만 공통 정책에 적용합니다. */
public final class BatchRuntimePolicyApplier implements CpfRuntimeChangeApplier {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> PAYLOAD_TYPE = new TypeReference<>() {};

    public static final String SCHEDULE = "BATCH_SCHEDULE";
    public static final String CONCURRENCY = "BATCH_CONCURRENCY";
    public static final String CALENDAR = "BATCH_CALENDAR";
    public static final String CENTER_CUT = "BATCH_CENTER_CUT";
    public static final String AGENT_POLICY = "BATCH_AGENT_POLICY";

    private static final Set<String> SUPPORTED = Set.of(
            SCHEDULE, CONCURRENCY, CALENDAR, CENTER_CUT, AGENT_POLICY);

    private final String changeType;
    private final BatchRuntimePolicy policy;

    public BatchRuntimePolicyApplier(String changeType, BatchRuntimePolicy policy) {
        if (!SUPPORTED.contains(changeType)) {
            throw new IllegalArgumentException("지원하지 않는 Batch runtime changeType: " + changeType);
        }
        this.changeType = changeType;
        this.policy = policy;
    }

    @Override
    public String changeType() {
        return changeType;
    }

    @Override
    public boolean supportsIdempotentReplay() {
        return true;
    }

    @Override
    public boolean snapshotCapable() {
        return true;
    }

    @Override
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        try {
            Map<String, Object> payload = payload(delivery);
            BatchRuntimePolicy.Snapshot applied = switch (changeType) {
                case SCHEDULE -> policy.replaceSchedule(
                        delivery.desiredVersion(),
                        bool(payload.get("enabled"), policy.current().schedulerEnabled()));
                case CONCURRENCY -> policy.replaceConcurrency(
                        delivery.desiredVersion(),
                        bool(payload.get("enabled"), policy.current().workerEnabled()),
                        integer(payload.get("maxConcurrency"),
                                policy.current().workerConcurrencyLimit()));
                case CALENDAR -> policy.replaceCalendar(
                        delivery.desiredVersion(),
                        bool(payload.get("enabled"), policy.current().calendarEnabled()));
                case CENTER_CUT -> policy.replaceCenterCut(
                        delivery.desiredVersion(),
                        bool(payload.get("enabled"), policy.current().centerCutEnabled()));
                case AGENT_POLICY -> policy.replaceAgentPolicy(
                        delivery.desiredVersion(),
                        bool(payload.get("commandsEnabled"),
                                policy.current().agentCommandsEnabled()),
                        bool(payload.get("logCollectionEnabled"),
                                policy.current().agentLogCollectionEnabled()));
                default -> throw new IllegalStateException(
                        "지원하지 않는 Batch runtime changeType: " + changeType);
            };

            if (applied.version() != delivery.desiredVersion()) {
                return CpfRuntimeApplyResult.failure(
                        "BATCH_RUNTIME_VERSION_NOT_CONFIRMED",
                        "Batch runtime version 적용을 확인하지 못했습니다.");
            }
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure("BATCH_RUNTIME_POLICY_INVALID", ex.getMessage());
        }
    }

    private Map<String, Object> payload(CpfRuntimeDelivery delivery) {
        try {
            return OBJECT_MAPPER.readValue(delivery.payload().canonicalJson(), PAYLOAD_TYPE);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("검증된 Batch runtime payload를 다시 읽을 수 없습니다.", ex);
        }
    }

    private int integer(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return value == null ? fallback : Integer.parseInt(String.valueOf(value).trim());
    }

    private boolean bool(Object value, boolean fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = String.valueOf(value).trim();
        if ("true".equalsIgnoreCase(text)) {
            return true;
        }
        if ("false".equalsIgnoreCase(text)) {
            return false;
        }
        throw new IllegalArgumentException("Boolean 정책 값은 true 또는 false여야 합니다: " + text);
    }
}
