package com.cpf.batch.centercut.runner;

import com.cpf.batch.api.ActualState;
import com.cpf.batch.runtime.RuntimeStateProvider;
import com.cpf.batch.spi.CenterCutTargetProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Center-Cut Runner의 실제 BAT 저장소 및 Target Provider 준비 상태를 보고합니다.
 */
@Component
public final class CenterCutRuntimeStateProvider implements RuntimeStateProvider {
    private static final long CACHE_NANOS = TimeUnit.SECONDS.toNanos(1);

    private final DataSource dataSource;
    private final int targetProviderCount;
    private volatile Probe probe;

    public CenterCutRuntimeStateProvider(
            @Qualifier("batDataSource") DataSource dataSource,
            List<CenterCutTargetProvider> targetProviders) {
        this.dataSource = dataSource;
        this.targetProviderCount = targetProviders == null ? 0 : targetProviders.size();
    }

    @Override
    public ActualState actualState() {
        return probe().ready() ? ActualState.READY : ActualState.DEGRADED;
    }

    @Override
    public boolean ready() {
        return probe().ready();
    }

    @Override
    public int availableCapacity() {
        return probe().ready() ? 1 : 0;
    }

    @Override
    public Map<String, String> dependencyHealth() {
        Probe current = probe();
        return Map.of(
                "batDataSource", current.dataSourceReady() ? "UP" : "DOWN",
                "centerCutTargetProviders", targetProviderCount > 0 ? "UP" : "DOWN");
    }

    @Override
    public String lastErrorCode() {
        Probe current = probe();
        if (targetProviderCount == 0) {
            return "BAT_CENTER_CUT_TARGET_PROVIDER_NOT_CONFIGURED";
        }
        return current.dataSourceReady()
                ? null
                : "BAT_CENTER_CUT_DATASOURCE_" + current.detail();
    }

    @Override
    public Map<String, Number> metrics() {
        return Map.of("targetProviderCount", targetProviderCount);
    }

    private Probe probe() {
        long now = System.nanoTime();
        Probe current = probe;
        if (current != null && now - current.checkedAtNanos() < CACHE_NANOS) {
            return current;
        }
        synchronized (this) {
            current = probe;
            if (current != null && now - current.checkedAtNanos() < CACHE_NANOS) {
                return current;
            }
            try (Connection connection = dataSource.getConnection()) {
                boolean valid = connection.isValid(2);
                probe = new Probe(valid && targetProviderCount > 0, valid,
                        valid ? "UP" : "INVALID", now);
            } catch (Exception failure) {
                probe = new Probe(false, false,
                        failure.getClass().getSimpleName().toUpperCase(), now);
            }
            return probe;
        }
    }

    private record Probe(boolean ready, boolean dataSourceReady, String detail, long checkedAtNanos) {
    }
}
