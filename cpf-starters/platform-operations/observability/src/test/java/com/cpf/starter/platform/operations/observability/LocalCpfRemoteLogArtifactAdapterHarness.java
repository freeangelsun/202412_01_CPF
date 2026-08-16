package com.cpf.starter.platform.operations.observability;

import com.cpf.platform.operations.observability.api.remotelog.CpfRemoteLogArtifact;
import com.cpf.platform.operations.observability.api.remotelog.CpfRemoteLogArtifactSearch;
import com.cpf.platform.operations.observability.api.remotelog.CpfRemoteLogBundle;
import com.cpf.platform.operations.observability.api.remotelog.CpfRemoteLogPreview;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

public final class LocalCpfRemoteLogArtifactAdapterHarness {
    private LocalCpfRemoteLogArtifactAdapterHarness() { }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("cpf-remote-log-");
        Instant now = Instant.parse("2026-08-05T00:00:00Z");
        MutableClock clock = new MutableClock(now);
        try {
            Path archived = root.resolve("transaction-archive.log");
            Files.writeString(archived,
                    "normal\ntransactionId=TX-001 password=raw-secret user@example.com\nlast\n",
                    StandardCharsets.UTF_8);
            Files.setLastModifiedTime(archived, FileTime.from(now.minus(Duration.ofHours(1))));
            Path active = root.resolve("application-current.log");
            Files.writeString(active, "active-line\n", StandardCharsets.UTF_8);
            Files.setLastModifiedTime(active, FileTime.from(now.minusSeconds(10)));

            LocalCpfRemoteLogArtifactAdapter.Settings settings =
                    new LocalCpfRemoteLogArtifactAdapter.Settings(
                            "prod", "CPF", "cpf-core", "app-01",
                            Duration.ofDays(30), Duration.ofMinutes(2), Duration.ofMinutes(15),
                            8, 100, 100, 1_000_000, 1_000_000, 2_000_000,
                            16_384, 1_000_000, 2_000_000, 10, false);
            LocalCpfRemoteLogArtifactAdapter adapter = new LocalCpfRemoteLogArtifactAdapter(
                    root, clock, settings);

            List<CpfRemoteLogArtifact> artifacts = adapter.search(new CpfRemoteLogArtifactSearch(
                    "prod", "CPF", null, null, null, null,
                    null, null, "TX-001", null, null, null, null, null,
                    null, null, null, null, null, null, 10));
            check(artifacts.size() == 1, "content identifier search must find the archived log");
            CpfRemoteLogArtifact artifact = artifacts.get(0);
            check(!artifact.downloadable() && !artifact.active(),
                    "raw source download must be disabled unless the source is explicitly trusted as masked");
            check(artifact.checksumSha256() == null, "blocked raw artifacts must not advertise a download checksum");

            CpfRemoteLogPreview preview = adapter.preview(artifact.artifactId(), 10, "transactionId");
            String previewText = String.join("\n", preview.lines());
            check(preview.returnedLineCount() == 1, "keyword preview line count");
            check(!previewText.contains("raw-secret") && !previewText.contains("user@example.com"),
                    "preview must redact secrets and PII");
            boolean rawRejected = false;
            try { adapter.resolveDownload(artifact.artifactId()); }
            catch (SecurityException expected) { rawRejected = true; }
            check(rawRejected, "untrusted raw source download must fail closed");

            LocalCpfRemoteLogArtifactAdapter.Settings trustedSettings =
                    new LocalCpfRemoteLogArtifactAdapter.Settings(
                            "prod", "CPF", "cpf-core", "app-01",
                            Duration.ofDays(30), Duration.ofMinutes(2), Duration.ofMinutes(15),
                            8, 100, 100, 1_000_000, 1_000_000, 2_000_000,
                            16_384, 1_000_000, 2_000_000, 10, true);
            LocalCpfRemoteLogArtifactAdapter trustedAdapter = new LocalCpfRemoteLogArtifactAdapter(
                    root, clock, trustedSettings);
            CpfRemoteLogArtifact trustedArtifact = trustedAdapter.search(new CpfRemoteLogArtifactSearch(
                    null, null, null, null, "transaction-archive.log", null, false, 10)).get(0);
            check(trustedArtifact.downloadable() && trustedArtifact.checksumSha256() != null,
                    "explicitly trusted masked source may enable direct download with checksum");
            check(trustedAdapter.resolveDownload(trustedArtifact.artifactId()).equals(archived.toRealPath()),
                    "trusted direct download must still resolve only the managed source file");

            CpfRemoteLogArtifact activeArtifact = adapter.search(new CpfRemoteLogArtifactSearch(
                    null, null, null, null, "application-current.log", null, true, 10)).get(0);
            boolean activeRejected = false;
            try { adapter.resolveDownload(activeArtifact.artifactId()); }
            catch (SecurityException expected) { activeRejected = true; }
            check(activeRejected, "active files must not be downloadable");

            CpfRemoteLogBundle bundle = adapter.createBundle(List.of(artifact.artifactId(), "f".repeat(64)));
            check(bundle.includedCount() == 1 && bundle.failedArtifactIds().size() == 1,
                    "bundle must report partial failure without losing valid artifacts");
            Path bundlePath = root.resolve(bundle.path()).normalize();
            check(bundlePath.startsWith(root) && Files.isRegularFile(bundlePath), "bundle path remains under root");
            boolean manifest = false;
            StringBuilder bundledText = new StringBuilder();
            try (InputStream input = Files.newInputStream(bundlePath); ZipInputStream zip = new ZipInputStream(input)) {
                for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                    if ("manifest.sha256".equals(entry.getName())) manifest = true;
                    check(!entry.getName().contains(".."), "zip entries must be traversal-safe");
                    if (entry.getName().endsWith(".masked.log")) {
                        bundledText.append(new String(zip.readAllBytes(), StandardCharsets.UTF_8));
                    }
                }
            }
            check(manifest, "bundle must contain checksum manifest");
            check(!bundledText.toString().contains("raw-secret")
                            && !bundledText.toString().contains("user@example.com"),
                    "bundle must contain only sanitized derivatives");
            clock.advance(Duration.ofMinutes(14));
            adapter.diagnostics();
            check(Files.exists(bundlePath), "bundle must remain before its TTL expires");
            clock.advance(Duration.ofMinutes(2));
            Map<String, Object> afterCleanup = adapter.diagnostics();
            check(!Files.exists(bundlePath), "expired bundle must be deleted from the generated bundle directory");
            check(((Number) afterCleanup.get("expiredBundlesDeleted")).longValue() >= 1L,
                    "expired bundle deletion must be visible in diagnostics");

            Path outside = Files.createTempFile("cpf-outside-", ".log");
            Path link = root.resolve("linked.log");
            boolean symlinkCreated = false;
            try {
                Files.createSymbolicLink(link, outside);
                symlinkCreated = true;
            } catch (UnsupportedOperationException | java.nio.file.FileSystemException ignored) { }
            if (symlinkCreated) {
                check(adapter.search(new CpfRemoteLogArtifactSearch(
                        null, null, null, null, "linked.log", null, null, 10)).isEmpty(),
                        "symbolic links must never become artifacts");
            }
            check(!adapter.diagnostics().toString().contains(root.toString()),
                    "diagnostics must not expose the configured root");
            System.out.println("CPF_LOCAL_REMOTE_LOG_ARTIFACT_ADAPTER_HARNESS_PASS");
        } finally {
            try (var paths = Files.walk(root)) {
                paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                    try { Files.deleteIfExists(path); } catch (Exception ignored) { }
                });
            }
        }
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) {
            if (!ZoneOffset.UTC.equals(zone)) throw new IllegalArgumentException("only UTC is supported");
            return this;
        }
        @Override public Instant instant() { return instant; }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
