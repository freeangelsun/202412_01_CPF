package com.cpf.core.common.remotelog;

import org.springframework.core.env.Environment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 서비스 registry의 여러 실행 인스턴스를 병렬 조회하고 artifact ID를 대상 node로 라우팅합니다.
 *
 * <p>실제 HTTP·mTLS 구현은 {@link CpfRemoteLogNodeClientPort}에 위임하며, 이 adapter는 timeout,
 * 부분 실패, 선택 ZIP, checksum manifest와 만료 파일 정리를 공통 처리합니다.</p>
 */
public final class RoutingCpfRemoteLogArtifactAdapter implements CpfRemoteLogArtifactPort, AutoCloseable {
    private static final int MAX_BUNDLE_FILES = 100;

    private final CpfRemoteLogNodeRegistryPort registryPort;
    private final CpfRemoteLogNodeClientPort clientPort;
    private final CpfRemoteLogServiceCredentialPort credentialPort;
    private final Duration timeout;
    private final Path bundleRoot;
    private final ExecutorService executor;
    private volatile Map<String, String> lastFailures = Map.of();

    public RoutingCpfRemoteLogArtifactAdapter(
            CpfRemoteLogNodeRegistryPort registryPort,
            CpfRemoteLogNodeClientPort clientPort,
            CpfRemoteLogServiceCredentialPort credentialPort,
            Environment environment) {
        this.registryPort = registryPort;
        this.clientPort = clientPort;
        this.credentialPort = credentialPort;
        long timeoutMillis = environment.getProperty("cpf.remote-log.request-timeout-ms", Long.class, 5_000L);
        this.timeout = Duration.ofMillis(Math.max(500L, Math.min(timeoutMillis, 60_000L)));
        String configuredRoot = environment.getProperty(
                "cpf.remote-log.bundle-root",
                Path.of(System.getProperty("java.io.tmpdir"), "cpf-remote-log-bundles").toString());
        this.bundleRoot = Path.of(configuredRoot).toAbsolutePath().normalize();
        int concurrency = environment.getProperty("cpf.remote-log.max-concurrency", Integer.class, 4);
        this.executor = Executors.newFixedThreadPool(Math.max(1, Math.min(concurrency, 16)));
    }

    @Override
    public List<CpfRemoteLogArtifact> search(CpfRemoteLogArtifactSearch search) {
        CpfRemoteLogArtifactSearch condition = search == null
                ? new CpfRemoteLogArtifactSearch(null, null, null, null, null, null, null, 100)
                : search;
        Map<String, String> failures = new LinkedHashMap<>();
        List<CompletableFuture<List<CpfRemoteLogArtifact>>> futures = registryPort.findOnlineNodes(condition).stream()
                .filter(CpfRemoteLogNode::online)
                .map(node -> CompletableFuture.supplyAsync(() -> searchNode(node, condition), executor)
                        .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                        .exceptionally(ex -> {
                            synchronized (failures) {
                                failures.put(node.nodeId(), rootMessage(ex));
                            }
                            return List.of();
                        }))
                .toList();
        List<CpfRemoteLogArtifact> result = futures.stream()
                .flatMap(future -> future.join().stream())
                .sorted(Comparator.comparing(CpfRemoteLogArtifact::modifiedAt).reversed())
                .limit(condition.limit())
                .toList();
        lastFailures = Map.copyOf(failures);
        return result;
    }

    @Override
    public CpfRemoteLogPreview preview(String artifactId, int lastLines, String keyword) {
        RoutedArtifact routed = decode(artifactId);
        CpfRemoteLogNode node = requireNode(routed.nodeId());
        CpfRemoteLogPreview preview = clientPort.preview(
                node, routed.delegateArtifactId(), lastLines, keyword, context(node));
        return new CpfRemoteLogPreview(
                route(node.nodeId(), preview.artifact()),
                preview.lines(), preview.returnedLineCount(), preview.truncated(), preview.keyword());
    }

    @Override
    public Path resolveDownload(String artifactId) {
        RoutedArtifact routed = decode(artifactId);
        CpfRemoteLogNode node = requireNode(routed.nodeId());
        Path staged = clientPort.stageDownload(node, routed.delegateArtifactId(), context(node));
        if (!Files.isRegularFile(staged, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(staged)) {
            throw new IllegalArgumentException("다운로드 staging 결과가 안전한 일반 파일이 아닙니다.");
        }
        return staged;
    }

    @Override
    public CpfRemoteLogBundle createBundle(List<String> artifactIds) {
        if (artifactIds == null || artifactIds.isEmpty()) {
            throw new IllegalArgumentException("묶음 다운로드 artifact ID는 한 건 이상이어야 합니다.");
        }
        List<String> selected = artifactIds.stream().distinct().limit(MAX_BUNDLE_FILES + 1L).toList();
        if (selected.size() > MAX_BUNDLE_FILES) {
            throw new IllegalArgumentException("묶음 다운로드는 최대 " + MAX_BUNDLE_FILES + "건까지 허용합니다.");
        }
        try {
            Files.createDirectories(bundleRoot);
            cleanupExpiredBundles();
            String bundleId = UUID.randomUUID().toString();
            String fileName = "cpf-remote-logs-" + bundleId + ".zip";
            Path target = bundleRoot.resolve(fileName).normalize();
            if (!target.startsWith(bundleRoot)) {
                throw new IllegalStateException("로그 묶음 경로가 허용 root를 벗어났습니다.");
            }
            List<String> failed = new ArrayList<>();
            List<String> manifest = new ArrayList<>();
            int included = 0;
            try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(
                    target, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE), StandardCharsets.UTF_8)) {
                for (String artifactId : selected) {
                    try {
                        Path source = resolveDownload(artifactId);
                        String entryName = String.format("%03d-%s", included + 1, safeFileName(source.getFileName().toString()));
                        zip.putNextEntry(new ZipEntry(entryName));
                        Files.copy(source, zip);
                        zip.closeEntry();
                        manifest.add(entryName + "\tSHA-256=" + checksum(source) + "\tARTIFACT=" + artifactId);
                        included++;
                    } catch (RuntimeException | IOException ex) {
                        failed.add(artifactId);
                    }
                }
                if (included == 0) {
                    throw new IllegalStateException("묶음에 포함할 수 있는 로그 파일이 없습니다.");
                }
                zip.putNextEntry(new ZipEntry("checksum-manifest.txt"));
                zip.write(String.join("\n", manifest).getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            } catch (RuntimeException | IOException ex) {
                Files.deleteIfExists(target);
                throw ex;
            }
            return new CpfRemoteLogBundle(
                    bundleId, fileName, target, included, failed, Instant.now().plus(1, ChronoUnit.HOURS));
        } catch (IOException ex) {
            throw new IllegalStateException("로그 묶음 파일을 생성할 수 없습니다.", ex);
        }
    }

    public Map<String, String> lastFailures() {
        return lastFailures;
    }

    @Override
    public Map<String, Object> diagnostics() {
        return Map.of(
                "adapter", getClass().getSimpleName(),
                "timeoutMillis", timeout.toMillis(),
                "bundleRoot", bundleRoot.toString(),
                "lastFailures", lastFailures);
    }

    private List<CpfRemoteLogArtifact> searchNode(CpfRemoteLogNode node, CpfRemoteLogArtifactSearch search) {
        return clientPort.search(node, search, context(node)).stream()
                .map(artifact -> route(node.nodeId(), artifact))
                .toList();
    }

    private CpfRemoteLogNodeClientPort.CpfRemoteLogAccessContext context(CpfRemoteLogNode node) {
        String token = node.local() ? "" : credentialPort.issueServiceToken(node);
        if (!node.local() && (token == null || token.isBlank())) {
            throw new IllegalStateException("원격 로그 service token을 발급할 수 없습니다. node=" + node.nodeId());
        }
        return new CpfRemoteLogNodeClientPort.CpfRemoteLogAccessContext(token, timeout);
    }

    private CpfRemoteLogNode requireNode(String nodeId) {
        return registryPort.findById(nodeId)
                .filter(CpfRemoteLogNode::online)
                .orElseThrow(() -> new IllegalArgumentException("온라인 로그 인스턴스를 찾을 수 없습니다. node=" + nodeId));
    }

    private CpfRemoteLogArtifact route(String nodeId, CpfRemoteLogArtifact artifact) {
        return new CpfRemoteLogArtifact(
                encode(nodeId, artifact.artifactId()), artifact.environment(), artifact.module(), artifact.service(),
                artifact.instance(), artifact.logType(), artifact.fileName(), artifact.relativePath(), artifact.size(),
                artifact.modifiedAt(), artifact.compressed(), artifact.checksumSha256(), artifact.active(),
                artifact.maskingPolicy(), artifact.downloadable(), artifact.retentionExpiresAt(),
                "ONLINE");
    }

    private String encode(String nodeId, String delegateArtifactId) {
        String value = nodeId + "\n" + delegateArtifactId;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private RoutedArtifact decode(String artifactId) {
        if (artifactId == null || artifactId.isBlank()) {
            throw new IllegalArgumentException("로그 artifact ID는 필수입니다.");
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(artifactId), StandardCharsets.UTF_8);
            int separator = decoded.indexOf('\n');
            if (separator < 1 || separator == decoded.length() - 1) {
                throw new IllegalArgumentException("라우팅 로그 artifact ID 구성이 올바르지 않습니다.");
            }
            return new RoutedArtifact(decoded.substring(0, separator), decoded.substring(separator + 1));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("라우팅 로그 artifact ID 형식이 올바르지 않습니다.", ex);
        }
    }

    private String checksum(Path path) {
        try (InputStream input = Files.newInputStream(path)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8_192];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception ex) {
            throw new IllegalStateException("로그 묶음 checksum 계산에 실패했습니다.", ex);
        }
    }

    private String safeFileName(String value) {
        String safe = value.replaceAll("[^A-Za-z0-9._-]", "_");
        return safe.isBlank() ? "cpf-log-artifact.log" : safe;
    }

    private void cleanupExpiredBundles() throws IOException {
        if (!Files.isDirectory(bundleRoot, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        Instant expiry = Instant.now().minus(1, ChronoUnit.DAYS);
        try (var paths = Files.list(bundleRoot)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                if (Files.getLastModifiedTime(path).toInstant().isBefore(expiry)) {
                    Files.deleteIfExists(path);
                }
            }
        }
    }

    private String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }

    private record RoutedArtifact(String nodeId, String delegateArtifactId) {
    }
}
