package com.cpf.platform.operations.health;

import com.cpf.platform.operations.api.health.CpfHealthSnapshotProvider;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/** Periodically reports a masked health snapshot to ADM. Enabled only when reportUrl+reportToken are configured. */
public final class CpfHealthReporter implements AutoCloseable {
    private final CpfHealthSnapshotProvider provider;
    private final CpfHealthProperties properties;
    private final RestClient client;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().name("cpf-health-report-",0).factory());
    private final AtomicReference<String> lastFailure = new AtomicReference<>();

    public CpfHealthReporter(CpfHealthSnapshotProvider provider, CpfHealthProperties properties) {
        this.provider = provider; this.properties = properties; this.client = RestClient.builder().build();
        long interval = properties.getReportInterval().toMillis();
        scheduler.scheduleWithFixedDelay(this::reportSafe, 0, interval, TimeUnit.MILLISECONDS);
    }

    void reportSafe() {
        try {
            client.post().uri(properties.getReportUrl()).contentType(MediaType.APPLICATION_JSON)
                    .header("X-Cpf-Runtime-Agent-Token", properties.getReportToken())
                    .body(provider.snapshot()).retrieve().toBodilessEntity();
            lastFailure.set(null);
        } catch (RuntimeException failure) {
            lastFailure.set(failure.getClass().getSimpleName());
        }
    }
    public String lastFailureCode() { return lastFailure.get(); }
    @Override public void close() { scheduler.shutdownNow(); }
}
