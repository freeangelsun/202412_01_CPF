package com.cpf.batch.control;

import com.cpf.batch.api.ActualState;
import com.cpf.batch.runtime.RuntimeStateProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Control Server의 실제 BAT DataSource 연결 상태를 readiness 근거로 사용합니다.
 */
@Component
public final class ControlServerRuntimeStateProvider implements RuntimeStateProvider {
    private static final long CACHE_NANOS = TimeUnit.SECONDS.toNanos(1);

    private final DataSource dataSource;
    private volatile Probe probe;

    public ControlServerRuntimeStateProvider(
            @Qualifier("batDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
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
        return Map.of("batDataSource", probe().ready() ? "UP" : "DOWN");
    }

    @Override
    public String lastErrorCode() {
        Probe current = probe();
        return current.ready() ? null : "BAT_CONTROL_DATASOURCE_" + current.detail();
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
                probe = new Probe(valid, valid ? "UP" : "INVALID", now);
            } catch (Exception failure) {
                probe = new Probe(false, failure.getClass().getSimpleName().toUpperCase(java.util.Locale.ROOT), now);
            }
            return probe;
        }
    }

    private record Probe(boolean ready, String detail, long checkedAtNanos) {
    }
}
