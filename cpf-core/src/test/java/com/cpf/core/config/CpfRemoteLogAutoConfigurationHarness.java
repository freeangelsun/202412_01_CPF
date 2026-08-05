package com.cpf.core.config;

import com.cpf.core.api.remotelog.CpfRemoteLogArtifact;
import com.cpf.core.api.remotelog.CpfRemoteLogArtifactPort;
import com.cpf.core.api.remotelog.CpfRemoteLogArtifactSearch;
import com.cpf.core.api.remotelog.CpfRemoteLogBundle;
import com.cpf.core.api.remotelog.CpfRemoteLogPreview;
import com.cpf.core.service.remotelog.DefaultCpfRemoteLogBundleJobManager;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;

/** Conditional wiring settings and injected clock harness. */
public final class CpfRemoteLogAutoConfigurationHarness {
    private CpfRemoteLogAutoConfigurationHarness() { }

    public static void main(String[] args) {
        Clock clock = Clock.fixed(Instant.parse("2026-08-05T00:00:00Z"), ZoneOffset.UTC);
        TestEnvironment environment = new TestEnvironment(Map.of(
                "cpf.remote-log.bundle-job.worker-count", "2",
                "cpf.remote-log.bundle-job.queue-capacity", "8",
                "cpf.remote-log.bundle-job.max-active-jobs", "3",
                "cpf.remote-log.bundle-job.max-retained-jobs", "30",
                "cpf.remote-log.bundle-job.max-requests-per-minute", "7",
                "cpf.remote-log.bundle-job.max-artifacts-per-job", "5",
                "cpf.remote-log.bundle-job.job-ttl-seconds", "600",
                "cpf.remote-log.bundle-job.download-token-ttl-seconds", "60"));
        CpfRemoteLogAutoConfiguration configuration = new CpfRemoteLogAutoConfiguration();
        DefaultCpfRemoteLogBundleJobManager manager = configuration.cpfRemoteLogBundleJobPort(
                new NoopArtifactPort(), new FixedProvider<>(clock), environment);
        try {
            Map<String, Object> diagnostics = manager.diagnostics();
            check(Integer.valueOf(2).equals(diagnostics.get("workerCount")), "worker setting");
            check(Integer.valueOf(3).equals(diagnostics.get("maxActiveJobs")), "active setting");
            check(Integer.valueOf(7).equals(diagnostics.get("maxRequestsPerMinute")), "rate setting");
        } finally {
            manager.close();
        }

        boolean invalidRejected = false;
        try {
            CpfRemoteLogAutoConfiguration.settings(new TestEnvironment(Map.of(
                    "cpf.remote-log.bundle-job.max-active-jobs", "0")));
        } catch (IllegalArgumentException expected) {
            invalidRejected = true;
        }
        check(invalidRejected, "invalid settings must fail at startup");
        System.out.println("CPF_REMOTE_LOG_AUTOCONFIG_HARNESS_PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private record FixedProvider<T>(T value) implements ObjectProvider<T> {
        @Override public T getObject() { return value; }
        @Override public T getIfAvailable() { return value; }
    }

    private static final class TestEnvironment implements Environment {
        private final Map<String, String> values;
        private TestEnvironment(Map<String, String> values) { this.values = values; }
        @Override @SuppressWarnings("unchecked")
        public <T> T getProperty(String key, Class<T> type, T defaultValue) {
            String value = values.get(key);
            if (value == null) return defaultValue;
            if (type == Integer.class) return (T) Integer.valueOf(value);
            if (type == Long.class) return (T) Long.valueOf(value);
            if (type == String.class) return (T) value;
            throw new IllegalArgumentException("unsupported test property type");
        }
    }

    private static final class NoopArtifactPort implements CpfRemoteLogArtifactPort {
        @Override public List<CpfRemoteLogArtifact> search(CpfRemoteLogArtifactSearch search) { return List.of(); }
        @Override public CpfRemoteLogPreview preview(String artifactId, int lastLines, String keyword) {
            throw new UnsupportedOperationException();
        }
        @Override public Path resolveDownload(String artifactId) { throw new UnsupportedOperationException(); }
        @Override public CpfRemoteLogBundle createBundle(List<String> artifactIds) {
            throw new UnsupportedOperationException();
        }
    }
}
