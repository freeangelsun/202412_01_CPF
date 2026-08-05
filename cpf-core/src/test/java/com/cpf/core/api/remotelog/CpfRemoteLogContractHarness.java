package com.cpf.core.api.remotelog;

import com.cpf.core.api.security.CpfSensitiveDataAccessRequest;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class CpfRemoteLogContractHarness {
    private CpfRemoteLogContractHarness() { }

    public static void main(String[] args) {
        Instant now = Instant.parse("2026-08-05T00:00:00Z");
        String digest = "a".repeat(64);
        CpfRemoteLogArtifact artifact = new CpfRemoteLogArtifact(
                "a", "prod", "core", "svc", "i", "app",
                "app.log.gz", "prod/core/app.log.gz", 10, now, true, digest,
                false, "mask-v2", true, now.plusSeconds(60), "online");
        check(artifact.relativePath().equals("prod/core/app.log.gz"), "safe relative artifact path");

        CpfRemoteLogBundle bundle = new CpfRemoteLogBundle(
                "b", "logs.zip", Path.of("bundles/logs.zip"), 1,
                List.of("x", "x"), now.plusSeconds(60));
        check(bundle.failedArtifactIds().size() == 1, "failed ids immutable and deduplicated");
        CpfRemoteLogDownloadGrant grant = new CpfRemoteLogDownloadGrant(
                "job", "x".repeat(32), now.plusSeconds(60));
        check(!grant.toString().contains("x".repeat(32)), "opaque download token must not appear in toString");

        CpfSensitiveDataAccessRequest accessRequest = new CpfSensitiveDataAccessRequest(
                "incident token=raw-secret investigation 2026-08-05");
        check(!accessRequest.reason().contains("raw-secret"), "raw-view reason must be sanitized");
        check(!accessRequest.toString().contains("raw-secret"), "raw-view request toString must redact reason");

        searchContractIsBoundedAndNormalized();
        previewContractIsSanitizedAndImmutable(artifact);

        boolean traversal = false;
        try {
            new CpfRemoteLogArtifact("a", "p", "m", "s", "i", "t", "a.log", "../a.log",
                    1, now, false, digest, false, "v", true, now, "ONLINE");
        } catch (IllegalArgumentException expected) {
            traversal = true;
        }
        check(traversal, "artifact path traversal rejected");

        boolean absolute = false;
        try {
            new CpfRemoteLogBundle("b", "x.zip", Path.of("/tmp/x.zip"), 1, List.of(), now);
        } catch (IllegalArgumentException expected) {
            absolute = true;
        }
        check(absolute, "server absolute path is not exposed by public bundle contract");

        boolean active = false;
        try {
            new CpfRemoteLogArtifact("a", "p", "m", "s", "i", "t", "a.log", "a.log",
                    1, now, false, digest, true, "v", true, now, "ONLINE");
        } catch (IllegalArgumentException expected) {
            active = true;
        }
        check(active, "active file cannot be downloaded directly");

        boolean artifactMismatch = false;
        try {
            new CpfRemoteLogArtifact("a", "p", "m", "s", "i", "t", "shown.log", "actual.log",
                    1, now, false, digest, false, "v", true, now, "ONLINE");
        } catch (IllegalArgumentException expected) {
            artifactMismatch = true;
        }
        check(artifactMismatch, "artifact display name cannot diverge from the authorized path");

        boolean bundleMismatch = false;
        try {
            new CpfRemoteLogBundle("b", "shown.zip", Path.of("bundles/actual.zip"), 1, List.of(),
                    now.plusSeconds(60));
        } catch (IllegalArgumentException expected) {
            bundleMismatch = true;
        }
        check(bundleMismatch, "bundle display name cannot diverge from the authorized path");

        boolean dotPath = false;
        try {
            new CpfRemoteLogArtifact("a", "p", "m", "s", "i", "t", "a.log", ".",
                    1, now, false, digest, false, "v", true, now, "ONLINE");
        } catch (IllegalArgumentException expected) {
            dotPath = true;
        }
        check(dotPath, "dot path is not an artifact path");
        System.out.println("CPF_REMOTE_LOG_CONTRACT_HARNESS_PASS");
    }

    private static void searchContractIsBoundedAndNormalized() {
        CpfRemoteLogArtifactSearch search = new CpfRemoteLogArtifactSearch(
                " prod ", " core ", " svc ", " instance-1 ", " app ", " app.log ",
                " tx-standard ", null, " tx-local ", null,
                null, null, null, null,
                null, null, -1L, 100L, null, null, 10_000);
        check("prod".equals(search.environment()), "search selectors must be trimmed");
        check(search.limit() == 500, "search result limit must be bounded");
        check(search.minSize() == 0L, "negative minimum size must normalize to zero");
        check(search.contentIdentifiers().equals(List.of("tx-standard", "tx-local")),
                "content identifiers must be normalized and stable");

        boolean controlRejected = false;
        try {
            new CpfRemoteLogArtifactSearch("prod\nforged", null, null, null, null, null,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, 10);
        } catch (IllegalArgumentException expected) {
            controlRejected = true;
        }
        check(controlRejected, "search control characters must be rejected");

        boolean pathRejected = false;
        try {
            new CpfRemoteLogArtifactSearch(null, null, null, null, "../app.log", null, null, 10);
        } catch (IllegalArgumentException expected) {
            pathRejected = true;
        }
        check(pathRejected, "file-name search must not become a path selector");
    }

    private static void previewContractIsSanitizedAndImmutable(CpfRemoteLogArtifact artifact) {
        List<String> source = new ArrayList<>();
        source.add("password=raw-secret Bearer abc.def user@example.com 010-1234-5678");
        source.add("normal-line");
        CpfRemoteLogPreview preview = new CpfRemoteLogPreview(
                artifact, source, 2, false, " token=keyword-secret ");
        source.clear();
        check(preview.lines().size() == 2, "preview must defensively copy source lines");
        String exposed = String.join("\n", preview.lines()) + " " + preview.keyword();
        check(!exposed.contains("raw-secret"), "preview lines must redact secrets");
        check(!exposed.contains("abc.def"), "preview lines must redact bearer tokens");
        check(!exposed.contains("user@example.com"), "preview lines must redact email PII");
        check(!exposed.contains("010-1234-5678"), "preview lines must redact phone PII");
        check(!exposed.contains("keyword-secret"), "preview keyword must be sanitized");

        boolean immutable = false;
        try {
            preview.lines().add("forged");
        } catch (UnsupportedOperationException expected) {
            immutable = true;
        }
        check(immutable, "preview lines must be immutable");

        String oversized = "x".repeat(20_000) + " token=must-not-leak";
        CpfRemoteLogPreview bounded = new CpfRemoteLogPreview(
                artifact, List.of(oversized), 1, true, null);
        check(bounded.lines().get(0).length() <= 16_384, "preview lines must be bounded");
        check(!bounded.lines().get(0).contains("must-not-leak"), "truncated preview must not retain tail secrets");

        boolean countRejected = false;
        try {
            new CpfRemoteLogPreview(artifact, List.of("one"), 2, false, null);
        } catch (IllegalArgumentException expected) {
            countRejected = true;
        }
        check(countRejected, "preview count must match returned lines");

        boolean keywordControlRejected = false;
        try {
            new CpfRemoteLogPreview(artifact, List.of(), 0, false, "a\nforged");
        } catch (IllegalArgumentException expected) {
            keywordControlRejected = true;
        }
        check(keywordControlRejected, "preview keyword control characters must be rejected");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
