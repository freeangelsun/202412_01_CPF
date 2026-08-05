package com.cpf.starter.platform.operations.observability;

import com.cpf.core.api.remotelog.CpfRemoteLogArtifactSearch;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.function.Supplier;

public final class CpfRemoteLogLocalAutoConfigurationHarness {
    private CpfRemoteLogLocalAutoConfigurationHarness() { }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("cpf-remote-log-auto-");
        Instant now = Instant.parse("2026-08-05T00:00:00Z");
        try {
            Path file = root.resolve("audit-history.log");
            Files.writeString(file, "audit token=raw-secret\n", StandardCharsets.UTF_8);
            Files.setLastModifiedTime(file, FileTime.from(now.minus(Duration.ofHours(1))));
            Map<String, Object> values = Map.ofEntries(
                    Map.entry("cpf.remote-log.local.root", root.toString()),
                    Map.entry("cpf.environment", "prod"),
                    Map.entry("cpf.remote-log.local.module", "CPF"),
                    Map.entry("cpf.remote-log.local.service", "cpf-core"),
                    Map.entry("cpf.remote-log.local.instance", "app-01"),
                    Map.entry("cpf.remote-log.local.maximum-scanned-files", 10),
                    Map.entry("cpf.remote-log.local.maximum-preview-lines", 2),
                    Map.entry("cpf.remote-log.local.maximum-download-bytes", 1_000_000L));
            CpfRemoteLogLocalAutoConfiguration configuration = new CpfRemoteLogLocalAutoConfiguration();
            LocalCpfRemoteLogArtifactAdapter adapter = configuration.cpfLocalRemoteLogArtifactPort(
                    new MapEnvironment(values), new FixedProvider<>(Clock.fixed(now, ZoneOffset.UTC)));
            check(adapter.search(new CpfRemoteLogArtifactSearch(
                    "prod", "CPF", null, null, "audit-history.log", null, null, 10)).size() == 1,
                    "auto-configuration must create an actual local artifact consumer");
            boolean lineLimitApplied = false;
            String artifactId = adapter.search(new CpfRemoteLogArtifactSearch(
                    null, null, null, null, "audit-history.log", null, null, 10)).get(0).artifactId();
            try { adapter.preview(artifactId, 3, null); }
            catch (IllegalArgumentException expected) { lineLimitApplied = true; }
            check(lineLimitApplied, "configured preview limit must be applied");

            boolean missingRootRejected = false;
            try {
                configuration.cpfLocalRemoteLogArtifactPort(
                        new MapEnvironment(Map.of()), new FixedProvider<>(Clock.fixed(now, ZoneOffset.UTC)));
            } catch (IllegalStateException expected) {
                missingRootRejected = true;
            }
            check(missingRootRejected, "missing root must fail fast");
            System.out.println("CPF_REMOTE_LOG_LOCAL_AUTOCONFIG_HARNESS_PASS");
        } finally {
            try (var paths = Files.walk(root)) {
                paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                    try { Files.deleteIfExists(path); } catch (Exception ignored) { }
                });
            }
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private record FixedProvider<T>(T value) implements ObjectProvider<T> {
        @Override public T getIfUnique(Supplier<T> defaultSupplier) {
            return value == null ? defaultSupplier.get() : value;
        }
    }

    private record MapEnvironment(Map<String, Object> values) implements Environment {
        @Override public String getProperty(String key) {
            Object value = values.get(key);
            return value == null ? null : String.valueOf(value);
        }
        @Override public String getProperty(String key, String defaultValue) {
            String value = getProperty(key);
            return value == null ? defaultValue : value;
        }
        @Override public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
            Object value = values.get(key);
            return value == null ? defaultValue : targetType.cast(value);
        }
    }
}
