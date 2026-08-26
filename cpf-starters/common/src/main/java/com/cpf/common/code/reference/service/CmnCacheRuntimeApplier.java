package com.cpf.common.code.reference.service;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/** DB 정본을 로컬 Cache snapshot으로 실제 교체하고 version 증가를 확인하는 공통 Applier입니다. */
public final class CmnCacheRuntimeApplier implements CpfRuntimeChangeApplier {
    private final String changeType;
    private final Runnable refreshAction;
    private final Supplier<Map<String, Object>> statusSupplier;

    public CmnCacheRuntimeApplier(
            String changeType,
            Runnable refreshAction,
            Supplier<Map<String, Object>> statusSupplier) {
        this.changeType = Objects.requireNonNull(changeType, "changeType").trim().toUpperCase(java.util.Locale.ROOT);
        this.refreshAction = Objects.requireNonNull(refreshAction, "refreshAction");
        this.statusSupplier = Objects.requireNonNull(statusSupplier, "statusSupplier");
    }

    @Override public String changeType() { return changeType; }
    @Override public boolean supportsIdempotentReplay() { return true; }
    @Override public boolean snapshotCapable() { return true; }

    @Override
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        long before = version(statusSupplier.get());
        refreshAction.run();
        Map<String, Object> afterStatus = statusSupplier.get();
        long after = version(afterStatus);
        if (after <= before || afterStatus.get("lastRefreshFailure") != null) {
            return CpfRuntimeApplyResult.failure(
                    "CMN_CACHE_REFRESH_NOT_CONFIRMED",
                    "DB 정본의 로컬 Cache 교체 완료를 version으로 확인하지 못했습니다.");
        }
        return CpfRuntimeApplyResult.success(delivery.payloadHash());
    }

    private long version(Map<String, Object> status) {
        Object value = status == null ? null : status.get("version");
        return value instanceof Number number ? number.longValue() : -1L;
    }
}
