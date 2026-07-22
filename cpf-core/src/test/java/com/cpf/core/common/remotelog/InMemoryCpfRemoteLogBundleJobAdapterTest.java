package cpf.pfw.common.remotelog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryCpfRemoteLogBundleJobAdapterTest {

    @TempDir
    Path tempDir;

    @Test
    void 비동기묶음을완료하고일회성token으로한번만다운로드한다() throws Exception {
        Path bundlePath = tempDir.resolve("cpf-remote-logs.zip");
        Files.writeString(bundlePath, "zip-content", StandardCharsets.UTF_8);
        CpfRemoteLogArtifactPort artifactPort = bundlePort(bundlePath);

        try (InMemoryCpfRemoteLogBundleJobAdapter adapter = new InMemoryCpfRemoteLogBundleJobAdapter(
                artifactPort, 10, 2, Duration.ofMinutes(5), Clock.systemUTC())) {
            CpfRemoteLogBundleJob submitted = adapter.submit("operator-01", List.of("artifact-01"));
            CpfRemoteLogBundleJob completed = awaitCompleted(adapter, submitted.jobId(), "operator-01");

            assertThat(completed.status()).isEqualTo("COMPLETED");
            assertThat(completed.includedArtifactCount()).isEqualTo(1);
            CpfRemoteLogDownloadGrant firstGrant = adapter.issueDownloadGrant(
                    submitted.jobId(), "operator-01");
            assertThat(adapter.resolveDownload(
                    submitted.jobId(), "operator-01", firstGrant.token()).path()).isEqualTo(bundlePath);
            assertThatThrownBy(() -> adapter.resolveDownload(
                    submitted.jobId(), "operator-01", firstGrant.token()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이미 사용");

            CpfRemoteLogDownloadGrant reissued = adapter.issueDownloadGrant(
                    submitted.jobId(), "operator-01");
            assertThat(reissued.token()).isNotEqualTo(firstGrant.token());
            assertThat(adapter.resolveDownload(
                    submitted.jobId(), "operator-01", reissued.token()).path()).isEqualTo(bundlePath);
        }
    }

    @Test
    void 작업소유자를분리하고소유자별요청한도를적용한다() throws Exception {
        Path bundlePath = tempDir.resolve("rate-limit.zip");
        Files.writeString(bundlePath, "zip-content", StandardCharsets.UTF_8);
        Clock clock = Clock.fixed(Instant.parse("2026-07-15T00:00:00Z"), ZoneOffset.UTC);

        try (InMemoryCpfRemoteLogBundleJobAdapter adapter = new InMemoryCpfRemoteLogBundleJobAdapter(
                bundlePort(bundlePath), 1, 2, Duration.ofMinutes(5), clock)) {
            CpfRemoteLogBundleJob submitted = adapter.submit("operator-01", List.of("artifact-01"));

            assertThatThrownBy(() -> adapter.find(submitted.jobId(), "operator-02"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("찾을 수 없습니다");
            assertThatThrownBy(() -> adapter.submit("operator-01", List.of("artifact-02")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("요청 한도");
            assertThat(adapter.submit("operator-02", List.of("artifact-02")).ownerId())
                    .isEqualTo("operator-02");
        }
    }

    private CpfRemoteLogBundleJob awaitCompleted(
            InMemoryCpfRemoteLogBundleJobAdapter adapter,
            String jobId,
            String ownerId) throws InterruptedException {
        for (int attempt = 0; attempt < 100; attempt++) {
            CpfRemoteLogBundleJob current = adapter.find(jobId, ownerId);
            if ("COMPLETED".equals(current.status()) || "FAILED".equals(current.status())) {
                return current;
            }
            Thread.sleep(10L);
        }
        throw new AssertionError("비동기 로그 묶음 작업이 제한 시간 안에 종료되지 않았습니다.");
    }

    private CpfRemoteLogArtifactPort bundlePort(Path bundlePath) {
        return new CpfRemoteLogArtifactPort() {
            @Override
            public List<CpfRemoteLogArtifact> search(CpfRemoteLogArtifactSearch search) {
                return List.of();
            }

            @Override
            public CpfRemoteLogPreview preview(String artifactId, int lastLines, String keyword) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Path resolveDownload(String artifactId) {
                throw new UnsupportedOperationException();
            }

            @Override
            public CpfRemoteLogBundle createBundle(List<String> artifactIds) {
                return new CpfRemoteLogBundle(
                        "bundle-01",
                        bundlePath.getFileName().toString(),
                        bundlePath,
                        artifactIds.size(),
                        List.of(),
                        Instant.now().plus(Duration.ofHours(1)));
            }

            @Override
            public Map<String, Object> diagnostics() {
                return Map.of("adapter", "test");
            }
        };
    }
}
