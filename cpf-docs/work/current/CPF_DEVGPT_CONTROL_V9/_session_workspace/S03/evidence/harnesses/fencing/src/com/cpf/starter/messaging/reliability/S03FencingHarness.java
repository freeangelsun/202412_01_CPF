package com.cpf.starter.messaging.reliability;

import com.cpf.core.common.broker.*;
import java.time.*;
import java.util.*;

public final class S03FencingHarness {
    private static final Instant NOW = Instant.parse("2026-08-05T04:00:00Z");
    public static void main(String[] args) {
        publisherRejectsLegacyOutbox();
        publisherRejectsLegacyUnknownPort();
        publisherUsesWorkerFencedMutation();
        reconcilerRejectsLegacyPort();
        reconcilerUsesWorkerFencedSuccessMutation();
        reconcilerUsesWorkerFencedRelease();
        System.out.println("S03_BROKER_FENCING_HARNESS PASS cases=6");
    }

    static void publisherRejectsLegacyOutbox() {
        LegacyPort p = new LegacyPort();
        expect(IllegalArgumentException.class, () -> new CpfBrokerPublisherWorker(
                p, p, e -> CpfBrokerResult.accepted("m", "B", null), fixed(), Duration.ofSeconds(1)));
    }

    static void publisherRejectsLegacyUnknownPort() {
        FencedOutboxOnly p = new FencedOutboxOnly();
        expect(IllegalArgumentException.class, () -> new CpfBrokerPublisherWorker(
                p, p, e -> CpfBrokerResult.accepted("m", "B", null), fixed(), Duration.ofSeconds(1)));
    }

    static void publisherUsesWorkerFencedMutation() {
        FencedPort p = new FencedPort();
        new CpfBrokerPublisherWorker(p, p,
                e -> CpfBrokerResult.accepted("m", "B", null), fixed(), Duration.ofSeconds(1))
                .runOnce("worker-A", 1);
        check("worker-A".equals(p.publishedWorker), "publisher did not fence by worker");
        check(!p.unfencedCalled, "publisher used unfenced mutation");
    }

    static void reconcilerRejectsLegacyPort() {
        LegacyPort p = new LegacyPort();
        expect(IllegalArgumentException.class, () -> new CpfBrokerUnknownResultReconciler(
                p, List.of(), fixed(), Duration.ofSeconds(1)));
    }

    static void reconcilerUsesWorkerFencedSuccessMutation() {
        FencedPort p = new FencedPort(); p.unknownClaim = true;
        new CpfBrokerUnknownResultReconciler(p,
                List.of(e -> CpfBrokerResult.accepted("m", "B", null)), fixed(), Duration.ofSeconds(1))
                .runOnce("worker-B", 1);
        check("worker-B".equals(p.publishedWorker), "reconciler success was not fenced");
        check(!p.unfencedCalled, "reconciler used unfenced completion");
    }

    static void reconcilerUsesWorkerFencedRelease() {
        FencedPort p = new FencedPort(); p.unknownClaim = true;
        new CpfBrokerUnknownResultReconciler(p,
                List.of(e -> new CpfBrokerResult("UNKNOWN", "m", "B", null, NOW, "pending")),
                fixed(), Duration.ofSeconds(1)).runOnce("worker-C", 1);
        check("worker-C".equals(p.releasedWorker), "reconciler release was not fenced");
        check(!p.unfencedCalled, "reconciler used unfenced release");
    }

    static CpfBrokerEnvelope envelope() {
        return new CpfBrokerEnvelope("tx", "seg", "P", "C", "idem", NOW,
                new CpfBrokerMessage("m", "topic", "k", new byte[]{1}, "application/octet-stream", Map.of()),
                Map.of());
    }
    static Clock fixed() { return Clock.fixed(NOW, ZoneOffset.UTC); }
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    static <T extends Throwable> void expect(Class<T> type, Runnable r) {
        try { r.run(); throw new AssertionError("expected " + type.getSimpleName()); }
        catch (Throwable x) { if (!type.isInstance(x)) throw new AssertionError(x); }
    }

    static class LegacyPort implements CpfBrokerOutboxPort, CpfBrokerUnknownResultPort {
        public CpfBrokerResult saveOutbox(CpfBrokerEnvelope e) { return null; }
        public List<CpfBrokerEnvelope> claimPending(String w, int l) { return List.of(envelope()); }
        public void markPublished(String m, CpfBrokerResult r) { }
        public void markUnknown(String m, CpfBrokerResult r, Instant n) { }
        public List<CpfBrokerEnvelope> claimUnknown(String w, int l) { return List.of(envelope()); }
        public void releaseUnknown(String m, String d, Instant n) { }
    }

    static final class FencedOutboxOnly extends LegacyPort {
        @Override public boolean supportsFencedPublishMutation() { return true; }
        @Override public void markPublished(String w, String m, CpfBrokerResult r) { }
    }

    static final class FencedPort extends LegacyPort {
        String publishedWorker; String releasedWorker; boolean unfencedCalled; boolean unknownClaim;
        @Override public boolean supportsFencedPublishMutation() { return true; }
        @Override public boolean supportsFencedUnknownMutation() { return true; }
        @Override public List<CpfBrokerEnvelope> claimUnknown(String w, int l) { return unknownClaim ? List.of(envelope()) : List.of(); }
        @Override public void markPublished(String m, CpfBrokerResult r) { unfencedCalled = true; }
        @Override public void markPublished(String w, String m, CpfBrokerResult r) { publishedWorker = w; }
        @Override public void markUnknown(String m, CpfBrokerResult r, Instant n) { unfencedCalled = true; }
        @Override public void markUnknown(String w, String m, CpfBrokerResult r, Instant n) { }
        @Override public void releaseUnknown(String m, String d, Instant n) { unfencedCalled = true; }
        @Override public void releaseUnknown(String w, String m, String d, Instant n) { releasedWorker = w; }
    }
}
