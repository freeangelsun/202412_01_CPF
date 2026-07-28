package com.cpf.batch.runtime;

import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;

import java.util.Set;

/** 실제 Consumer가 존재하는 Batch Runtime 변경만 공통 정책에 적용합니다. */
public final class BatchRuntimePolicyApplier implements CpfRuntimeChangeApplier {
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
            BatchRuntimePolicy.Snapshot applied = switch (changeType) {
                case SCHEDULE -> policy.replaceSchedule(
                        delivery.desiredVersion(),
                        bool(delivery.payload().get("enabled"), policy.current().schedulerEnabled()));
                case CONCURRENCY -> policy.replaceConcurrency(
                        delivery.desiredVersion(),
                        bool(delivery.payload().get("enabled"), policy.current().workerEnabled()),
                        integer(delivery.payload().get("maxConcurrency"),
                                policy.current().workerConcurrencyLimit()));
                case CALENDAR -> policy.replaceCalendar(
                        delivery.desiredVersion(),
                        bool(delivery.payload().get("enabled"), policy.current().calendarEnabled()));
                case CENTER_CUT -> policy.replaceCenterCut(
                        delivery.desiredVersion(),
                        bool(delivery.payload().get("enabled"), policy.current().centerCutEnabled()));
                case AGENT_POLICY -> policy.replaceAgentPolicy(
                        delivery.desiredVersion(),
                        bool(delivery.payload().get("commandsEnabled"),
                                policy.current().agentCommandsEnabled()),
                        bool(delivery.payload().get("logCollectionEnabled"),
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
