package com.cpf.integration.tcp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ConcurrentModificationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfTcpUnknownResultStoreDurabilityTest {

    @TempDir
    Path tempDir;

    @Test
    void restartsFromJournalWithoutPersistingRawRequestOrSecret() throws Exception {
        Path journal = tempDir.resolve("unknown.journal");
        byte[] request = "account=1234567890&token=raw-secret".getBytes(StandardCharsets.UTF_8);
        CpfTcpUnknownResultStore first = new CpfTcpUnknownResultStore(10, journal);
        first.record(new CpfTcpUnknownResult(
                "CORR-1", Instant.parse("2026-08-05T01:00:00Z"), request,
                "authorization=Bearer-AbCd token=top-secret timeout"));

        String persisted = Files.readString(journal);
        assertThat(persisted)
                .doesNotContain("1234567890", "raw-secret", "top-secret", "Bearer-AbCd")
                .contains("V1|R|");

        CpfTcpUnknownResultStore restarted = new CpfTcpUnknownResultStore(10, journal);
        var restored = restarted.findVersioned("CORR-1").orElseThrow();
        assertThat(restored.value().request()).isEmpty();
        assertThat(restored.requestHash()).hasSize(64);
        assertThat(restored.value().detail()).contains("requestSha256=").contains("token=***");
        assertThat(restored.version()).isZero();
    }

    @Test
    void reconciliationUsesCasAndPersistsAppendOnlyAudit() {
        Path journal = tempDir.resolve("unknown.journal");
        CpfTcpUnknownResultStore store = new CpfTcpUnknownResultStore(10, journal);
        store.record(new CpfTcpUnknownResult(
                "CORR-1", Instant.parse("2026-08-05T01:00:00Z"), new byte[]{1, 2, 3}, "timeout"));

        assertThatThrownBy(() -> store.reconcile("CORR-1", 1L, "OP-1", "confirmed"))
                .isInstanceOf(ConcurrentModificationException.class);
        assertThat(store.reconcile("CORR-1", 0L, "OP-1", "token=secret confirmed")).isTrue();
        assertThat(store.find("CORR-1")).isEmpty();

        CpfTcpUnknownResultStore restarted = new CpfTcpUnknownResultStore(10, journal);
        assertThat(restarted.find("CORR-1")).isEmpty();
        assertThat(restarted.auditSnapshot()).singleElement().satisfies(audit -> {
            assertThat(audit.operator()).isEqualTo("OP-1");
            assertThat(audit.reason()).contains("token=***");
            assertThat(audit.reconciled()).isTrue();
            assertThat(audit.resultingVersion()).isEqualTo(1L);
        });
    }

    @Test
    void twoInstancesReloadUnderFileLockAndRejectConflictingDuplicate() {
        Path journal = tempDir.resolve("unknown.journal");
        CpfTcpUnknownResultStore first = new CpfTcpUnknownResultStore(10, journal);
        CpfTcpUnknownResultStore second = new CpfTcpUnknownResultStore(10, journal);
        Instant at = Instant.parse("2026-08-05T01:00:00Z");
        first.record(new CpfTcpUnknownResult("CORR-1", at, new byte[]{1}, "timeout"));

        second.record(new CpfTcpUnknownResult("CORR-2", at.plusSeconds(1), new byte[]{2}, "timeout"));
        first.refresh();
        assertThat(first.snapshot()).extracting(CpfTcpUnknownResult::correlationId)
                .containsExactly("CORR-1", "CORR-2");

        assertThatThrownBy(() -> second.record(
                new CpfTcpUnknownResult("CORR-1", at, new byte[]{9}, "timeout")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("correlation conflict");
    }

    @Test
    void startupTruncatesOnlyIncompleteTailAfterProcessKill() throws Exception {
        Path journal = tempDir.resolve("unknown.journal");
        CpfTcpUnknownResultStore first = new CpfTcpUnknownResultStore(10, journal);
        first.record(new CpfTcpUnknownResult(
                "CORR-1", Instant.parse("2026-08-05T01:00:00Z"), new byte[]{1}, "timeout"));
        Files.writeString(journal, "V1|R|PARTIAL", StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND);

        CpfTcpUnknownResultStore recovered = new CpfTcpUnknownResultStore(10, journal);

        assertThat(recovered.find("CORR-1")).isPresent();
        assertThat(Files.readString(journal)).doesNotContain("PARTIAL").endsWith("\n");
    }

    @Test
    void reconciledCorrelationCannotBeRemovedByStaleVersionAfterRerecord() {
        Path journal = tempDir.resolve("unknown.journal");
        CpfTcpUnknownResultStore store = new CpfTcpUnknownResultStore(10, journal);
        Instant firstAt = Instant.parse("2026-08-05T01:00:00Z");
        store.record(new CpfTcpUnknownResult("CORR-1", firstAt, new byte[]{1}, "timeout"));
        assertThat(store.reconcile("CORR-1", 0L, "OP-1", "confirmed")).isTrue();
        store.record(new CpfTcpUnknownResult("CORR-1", firstAt.plusSeconds(1), new byte[]{2}, "timeout"));
        assertThat(store.findVersioned("CORR-1").orElseThrow().version()).isEqualTo(2L);
        assertThatThrownBy(() -> store.reconcile("CORR-1", 0L, "STALE", "old command"))
                .isInstanceOf(ConcurrentModificationException.class);
        assertThat(new CpfTcpUnknownResultStore(10, journal)
                .findVersioned("CORR-1").orElseThrow().version()).isEqualTo(2L);
    }

    @Test
    void completeChecksumCorruptionFailsClosed() throws Exception {
        Path journal = tempDir.resolve("unknown.journal");
        Files.writeString(journal, "V1|R|bad|0|hash|detail|0|wrong-checksum\n");

        assertThatThrownBy(() -> new CpfTcpUnknownResultStore(10, journal))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("checksum mismatch");
    }
}
