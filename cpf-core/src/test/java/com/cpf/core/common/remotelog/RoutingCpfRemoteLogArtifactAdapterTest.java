package com.cpf.core.common.remotelog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;

/** 다중 인스턴스 라우팅 ID, service token, 부분 실패 ZIP manifest 계약을 검증합니다. */
class RoutingCpfRemoteLogArtifactAdapterTest {
    @TempDir
    Path tempDir;

    @Test
    void searchAggregatesNodesAndRoutesPreviewWithServiceToken() throws Exception {
        CpfRemoteLogNode local = node("local-node", true);
        CpfRemoteLogNode remote = node("remote-node", false);
        FakeRegistry registry = new FakeRegistry(List.of(local, remote));
        FakeClient client = new FakeClient(tempDir);
        client.artifacts.put(local.nodeId(), List.of(artifact("local-id", "local.log", local.instance())));
        client.artifacts.put(remote.nodeId(), List.of(artifact("remote-id", "remote.log", remote.instance())));
        MockEnvironment environment = environment();

        try (RoutingCpfRemoteLogArtifactAdapter adapter = new RoutingCpfRemoteLogArtifactAdapter(
                registry, client, ignored -> "short-lived-service-token", environment)) {
            List<CpfRemoteLogArtifact> result = adapter.search(
                    new CpfRemoteLogArtifactSearch(null, null, null, null, null, null, null, 10));

            assertThat(result).hasSize(2);
            assertThat(result).extracting(CpfRemoteLogArtifact::artifactId)
                    .noneMatch(id -> id.equals("local-id") || id.equals("remote-id"));

            CpfRemoteLogArtifact selected = result.stream()
                    .filter(item -> item.instance().equals(remote.instance()))
                    .findFirst()
                    .orElseThrow();
            CpfRemoteLogPreview preview = adapter.preview(selected.artifactId(), 20, "ERROR");

            assertThat(preview.lines()).containsExactly("ERROR remote preview");
            assertThat(client.lastRemoteToken).isEqualTo("short-lived-service-token");
            assertThat(adapter.lastFailures()).isEmpty();
        }
    }

    @Test
    void selectedBundleContainsChecksumManifestAndReportsPartialFailure() throws Exception {
        CpfRemoteLogNode local = node("local-node", true);
        FakeRegistry registry = new FakeRegistry(List.of(local));
        FakeClient client = new FakeClient(tempDir);
        client.artifacts.put(local.nodeId(), List.of(
                artifact("ok-id", "application.log", local.instance()),
                artifact("missing-id", "missing.log", local.instance())));

        try (RoutingCpfRemoteLogArtifactAdapter adapter = new RoutingCpfRemoteLogArtifactAdapter(
                registry, client, ignored -> "", environment())) {
            List<CpfRemoteLogArtifact> result = adapter.search(
                    new CpfRemoteLogArtifactSearch(null, null, null, null, null, null, null, 10));
            List<String> ids = result.stream().map(CpfRemoteLogArtifact::artifactId).toList();

            CpfRemoteLogBundle bundle = adapter.createBundle(ids);

            assertThat(bundle.includedCount()).isEqualTo(1);
            assertThat(bundle.failedArtifactIds()).hasSize(1);
            assertThat(bundle.path()).exists();
            try (ZipFile zip = new ZipFile(bundle.path().toFile(), StandardCharsets.UTF_8)) {
                assertThat(zip.getEntry("checksum-manifest.txt")).isNotNull();
                String manifest = new String(
                        zip.getInputStream(zip.getEntry("checksum-manifest.txt")).readAllBytes(), StandardCharsets.UTF_8);
                assertThat(manifest).contains("SHA-256=", "ARTIFACT=");
                assertThat(zip.stream().filter(entry -> !entry.isDirectory()).count()).isEqualTo(2);
            }
        }
    }

    private MockEnvironment environment() {
        return new MockEnvironment()
                .withProperty("cpf.remote-log.bundle-root", tempDir.resolve("bundles").toString())
                .withProperty("cpf.remote-log.request-timeout-ms", "2000")
                .withProperty("cpf.remote-log.max-concurrency", "2");
    }

    private CpfRemoteLogNode node(String nodeId, boolean local) {
        return new CpfRemoteLogNode(
                nodeId, "local", "ADM", "ADM", nodeId + "-instance",
                local ? null : URI.create("https://127.0.0.1:9443"), local, true,
                local ? "LOCAL" : "CPF-MTLS", local ? "LOCAL" : "cpf-log-artifact");
    }

    private CpfRemoteLogArtifact artifact(String artifactId, String fileName, String instance) {
        return new CpfRemoteLogArtifact(
                artifactId, "local", "ADM", "ADM", instance, "application", fileName,
                "local/ADM/" + instance + "/application/" + fileName,
                16, Instant.now(), false, "checksum", true, "CPF_SENSITIVE_DATA_MASKER", true);
    }

    private static final class FakeRegistry implements CpfRemoteLogNodeRegistryPort {
        private final Map<String, CpfRemoteLogNode> nodes;

        private FakeRegistry(List<CpfRemoteLogNode> nodes) {
            this.nodes = nodes.stream().collect(java.util.stream.Collectors.toMap(
                    CpfRemoteLogNode::nodeId, node -> node));
        }

        @Override
        public List<CpfRemoteLogNode> findOnlineNodes(CpfRemoteLogArtifactSearch search) {
            return List.copyOf(nodes.values());
        }

        @Override
        public Optional<CpfRemoteLogNode> findById(String nodeId) {
            return Optional.ofNullable(nodes.get(nodeId));
        }
    }

    private static final class FakeClient implements CpfRemoteLogNodeClientPort {
        private final Path tempDir;
        private final Map<String, List<CpfRemoteLogArtifact>> artifacts = new ConcurrentHashMap<>();
        private volatile String lastRemoteToken;

        private FakeClient(Path tempDir) {
            this.tempDir = tempDir;
        }

        @Override
        public List<CpfRemoteLogArtifact> search(
                CpfRemoteLogNode node,
                CpfRemoteLogArtifactSearch search,
                CpfRemoteLogAccessContext context) {
            rememberToken(node, context);
            return artifacts.getOrDefault(node.nodeId(), List.of());
        }

        @Override
        public CpfRemoteLogPreview preview(
                CpfRemoteLogNode node,
                String artifactId,
                int lastLines,
                String keyword,
                CpfRemoteLogAccessContext context) {
            rememberToken(node, context);
            CpfRemoteLogArtifact artifact = artifacts.get(node.nodeId()).stream()
                    .filter(item -> item.artifactId().equals(artifactId))
                    .findFirst()
                    .orElseThrow();
            return new CpfRemoteLogPreview(artifact, List.of("ERROR remote preview"), 1, false, keyword);
        }

        @Override
        public Path stageDownload(
                CpfRemoteLogNode node,
                String artifactId,
                CpfRemoteLogAccessContext context) {
            rememberToken(node, context);
            if ("missing-id".equals(artifactId)) {
                throw new IllegalStateException("교육용 부분 실패");
            }
            try {
                Path path = tempDir.resolve(node.nodeId() + "-" + artifactId + ".log");
                Files.writeString(path, "CPF remote log\n", StandardCharsets.UTF_8);
                return path;
            } catch (java.io.IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

        private void rememberToken(CpfRemoteLogNode node, CpfRemoteLogAccessContext context) {
            if (!node.local()) {
                lastRemoteToken = context.serviceToken();
            }
        }
    }
}
