package com.cpf.common.runtime.control;

import com.cpf.common.runtime.cache.CpfCommonCacheRefresher;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;

import java.util.Objects;

/** Common DB 정본 변경을 해당 local cache에 즉시 반영하는 Runtime Control adapter입니다. */
public final class CpfCommonCacheRuntimeApplier implements CpfRuntimeChangeApplier {
    private final String changeType;
    private final String cacheName;
    private final CpfCommonCacheRefresher refresher;

    public CpfCommonCacheRuntimeApplier(String changeType, String cacheName, CpfCommonCacheRefresher refresher) {
        this.changeType=Objects.requireNonNull(changeType,"changeType").trim().toUpperCase(java.util.Locale.ROOT);
        this.cacheName=Objects.requireNonNull(cacheName,"cacheName").trim();
        this.refresher=Objects.requireNonNull(refresher,"refresher");
    }
    @Override public String changeType(){return changeType;}
    @Override public boolean supportsIdempotentReplay(){return true;}
    @Override public boolean snapshotCapable(){return false;}
    @Override public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery){
        try {
            refresher.refresh(cacheName);
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException failure) {
            return CpfRuntimeApplyResult.failure("COMMON_CACHE_REFRESH_FAILED", "Common cache refresh could not be confirmed.");
        }
    }
}
