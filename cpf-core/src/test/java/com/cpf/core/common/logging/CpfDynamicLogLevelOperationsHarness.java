package com.cpf.core.common.logging;

import com.cpf.core.api.logging.CpfLogLevel;
import com.cpf.core.api.logging.DynamicLogLevelRequest;
import com.cpf.core.api.logging.DynamicLogLevelRule;
import com.cpf.core.api.logging.DynamicLogLevelRuntimeSnapshot;
import com.cpf.core.api.logging.DynamicLogLevelVersionConflictException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class CpfDynamicLogLevelOperationsHarness {
    private CpfDynamicLogLevelOperationsHarness() {}

    public static void main(String[] args) throws Exception {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        DefaultCpfDynamicLogLevelOperations operations =
                new DefaultCpfDynamicLogLevelOperations(clock, Duration.ofHours(1), 2_048, 8);

        DynamicLogLevelRule business = operations.register(request(
                null, "PAY", null, CpfLogLevel.DEBUG, Duration.ofMinutes(10),
                "password=secret", "operator"));
        assertTrue(business.reason().contains("password=***"), "reason must be masked");
        assertTrue(!business.toString().contains("password=secret"),
                "dynamic log rule toString must redact reason");
        DynamicLogLevelRuntimeSnapshot afterRegister = operations.snapshot();
        assertEquals(1L, afterRegister.version(), "register must advance version exactly once");
        assertEquals(1, afterRegister.auditRecords().size(), "register audit missing");
        assertEquals("REGISTER", afterRegister.auditRecords().get(0).action(), "audit action mismatch");
        assertTrue(afterRegister.auditRecords().get(0).reasonMasked().contains("password=***"),
                "audit reason must be masked");

        DynamicLogLevelRule exact = operations.register(request(
                "tx-1", "PAY", " adm ", CpfLogLevel.TRACE, Duration.ofMinutes(5),
                "incident", "operator"));
        long versionAfterExact = operations.snapshot().version();
        boolean staleRejected = false;
        try {
            operations.remove(exact.ruleId(), versionAfterExact - 1L, "operator", "stale removal");
        } catch (DynamicLogLevelVersionConflictException expected) {
            staleRejected = expected.actualVersion() == versionAfterExact;
        }
        assertTrue(staleRejected, "stale expectedVersion must fail closed");
        Optional<DynamicLogLevelRule> resolved = operations.resolve("tx-1", "PAY", "ADM");
        assertEquals(exact.ruleId(), resolved.orElseThrow().ruleId(), "most specific rule must win");

        CpfTraceSamplingPolicy sampling = new CpfTraceSamplingPolicy(operations);
        sampling.replace(1L, 0.0d, java.util.Map.of(), java.util.Map.of(), true);
        assertTrue(sampling.shouldSample("tx-1", "PAY", "ADM", true),
                "dynamic rule must force detailed trace sampling");
        assertFalse(sampling.shouldSample("tx-2", "OTHER", "ADM", true),
                "unmatched success must obey zero sampling rate");

        clock.advance(Duration.ofMinutes(5));
        DynamicLogLevelRule afterExactExpiry = operations.resolve("tx-1", "PAY", "ADM").orElseThrow();
        assertEquals(business.ruleId(), afterExactExpiry.ruleId(),
                "exact rule must expire at expiresAt and fall back to the active business rule");
        assertTrue(operations.resolve("tx-2", "PAY", "ADM").isPresent(),
                "business rule remains active");

        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), clock.getZone());
        DynamicLogLevelRule replacement = new DynamicLogLevelRule(
                "replace-1", null, "REFUND", null, CpfLogLevel.INFO,
                "reason", "operator", now, now.plusMinutes(1));
        long beforeReplaceVersion = operations.snapshot().version();
        operations.replaceAll(List.of(replacement), beforeReplaceVersion, "operator", "replace policy");
        DynamicLogLevelRuntimeSnapshot afterReplace = operations.snapshot();
        assertEquals(beforeReplaceVersion + 1L, afterReplace.version(), "replace must advance CAS version");
        assertEquals("REPLACE_ALL", afterReplace.auditRecords().get(afterReplace.auditRecords().size() - 1).action(),
                "replace audit missing");
        assertEquals(1, operations.findActiveRules().size(), "replaceAll must be atomic");

        boolean duplicateRejected = false;
        try {
            operations.replaceAll(List.of(replacement, replacement));
        } catch (IllegalArgumentException expected) {
            duplicateRejected = true;
        }
        assertTrue(duplicateRejected, "duplicate rule IDs must fail closed");

        int count = 64;
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(count);
        List<Throwable> failures = java.util.Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < count; i++) {
            final int index = i;
            executor.execute(() -> {
                try {
                    start.await();
                    operations.register(request(
                            "concurrent-" + index, null, null, CpfLogLevel.DEBUG,
                            Duration.ofMinutes(1), "reason", "operator"));
                } catch (Throwable failure) {
                    failures.add(failure);
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS), "concurrent registration timed out");
        executor.shutdownNow();
        assertTrue(failures.isEmpty(), "concurrent registrations failed: " + failures);
        assertEquals(count + 1, operations.findActiveRules().size(),
                "atomic snapshots must not lose concurrent updates");

        boolean ttlRejected = false;
        try {
            operations.register(request(
                    "too-long", null, null, CpfLogLevel.DEBUG,
                    Duration.ofHours(2), "reason", "operator"));
        } catch (IllegalArgumentException expected) {
            ttlRejected = true;
        }
        assertTrue(ttlRejected, "TTL above configured maximum must fail closed");

        DynamicLogLevelRuntimeSnapshot boundedAudit = operations.snapshot();
        assertEquals(8, boundedAudit.maximumAuditRecords(), "configured audit bound mismatch");
        assertEquals(8, boundedAudit.auditRecords().size(), "audit history must remain bounded");
        assertTrue(boundedAudit.droppedAuditRecordCount() > 0L,
                "discarded oldest audit records must be observable");
        assertEquals(boundedAudit.version(),
                boundedAudit.auditRecords().get(boundedAudit.auditRecords().size() - 1).committedVersion(),
                "latest committed audit record must be retained");

        CpfTraceSamplingPolicy boundedSampling = new CpfTraceSamplingPolicy(null, 2);
        boundedSampling.replace(1L, 0.1d, java.util.Map.of("ADM", 0.5d),
                java.util.Map.of("PAY", 1.0d), true);
        assertEquals(2, boundedSampling.maximumOverrideEntries(), "sampling override bound mismatch");
        boolean samplingCapacityRejected = false;
        try {
            boundedSampling.replace(2L, 0.1d,
                    java.util.Map.of("ADM", 0.5d, "BZA", 0.4d),
                    java.util.Map.of("PAY", 1.0d), true);
        } catch (IllegalArgumentException expected) {
            samplingCapacityRejected = true;
        }
        assertTrue(samplingCapacityRejected, "sampling override capacity must fail closed");
        java.util.LinkedHashMap<String, Double> duplicateOverrides = new java.util.LinkedHashMap<>();
        duplicateOverrides.put(" adm ", 0.5d);
        duplicateOverrides.put("ADM", 0.4d);
        boolean duplicateOverrideRejected = false;
        try {
            boundedSampling.replace(2L, 0.1d, duplicateOverrides, java.util.Map.of(), true);
        } catch (IllegalArgumentException expected) {
            duplicateOverrideRejected = true;
        }
        assertTrue(duplicateOverrideRejected, "canonical duplicate sampling keys must be rejected");

        System.out.println("CPF_DYNAMIC_LOG_LEVEL_HARNESS_PASS");
    }

    private static DynamicLogLevelRequest request(
            String transactionId,
            String businessTransactionId,
            String moduleId,
            CpfLogLevel level,
            Duration ttl,
            String reason,
            String user) {
        DynamicLogLevelRequest request = new DynamicLogLevelRequest();
        request.setTransactionId(transactionId);
        request.setBusinessTransactionId(businessTransactionId);
        request.setModuleId(moduleId);
        request.setLogLevel(level);
        request.setTtl(ttl);
        request.setReason(reason);
        request.setRequestUser(user);
        return request;
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(message + " expected=" + expected + " actual=" + actual);
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

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            if (!ZoneOffset.UTC.equals(zone)) throw new IllegalArgumentException("UTC only");
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
