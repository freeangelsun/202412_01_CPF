package com.cpf.reference.edu.runtime;

import com.cpf.reference.edu.runtime.application.*;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.persistence.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Standalone, dependency-free acceptance test for all 135 executable Manual EDU requirements.
 * Product runtime remains JDBC-first. The nested memory repository exists only in test scope so
 * the exhaustive source/contract checks finish quickly; file durability is exercised separately.
 */
public final class EduManual135SelfTestMain {
    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("cpf-edu135-");
        EduCapabilityRegistry registry = EduFullReferenceTestRegistry.create();
        if (registry.all().size() != 135) throw new AssertionError("handler count=" + registry.all().size());

        int contract=0, validation=0, authorization=0, normal=0, duplicate=0;
        int representativeFailure=0, exhaustiveFailure=0, recovery=0, persistence=0, concurrency=0;
        Map<EduCapabilityKind, AbstractEduCapabilityHandler> representatives = new EnumMap<>(EduCapabilityKind.class);

        for (var handler : registry.all()) {
            representatives.putIfAbsent(handler.definition().kind(), handler);
            MemoryRepository repository = new MemoryRepository();
            var service = new EduExecutionService(registry, repository, TestEduBusinessConsumers.registry(), Clock.systemUTC(), "selftest");
            var command = command(handler, EduFailurePoint.NONE, "normal");

            handler.validate(command);
            if (!handler.implementationPackage().equals(handler.getClass().getPackageName())) {
                throw new AssertionError("package mismatch " + handler.definition().requirementId());
            }
            if (handler.businessStates().isEmpty() || handler.exceptionScenarios().isEmpty()
                    || handler.requiredVerification().isEmpty() || handler.targetKeys(command).isEmpty()) {
                throw new AssertionError("contract incomplete " + handler.definition().requirementId());
            }
            contract++;

            var invalid = handler.invalidPayloadExample(command.payload());
            try {
                handler.validate(copy(command, invalid, command.roles(), command.dataScope(), EduFailurePoint.NONE, "invalid"));
                throw new AssertionError("invalid payload accepted " + handler.definition().requirementId());
            } catch (EduValidationException expected) { validation++; }

            try {
                handler.validate(copy(command, command.payload(), Set.of(), command.dataScope(), EduFailurePoint.NONE, "no-role"));
                throw new AssertionError("missing role accepted " + handler.definition().requirementId());
            } catch (EduAuthorizationException expected) { authorization++; }

            var ok = service.execute(handler.definition().requirementId(), command);
            ok = acknowledgeIfNeeded(service, ok);
            if (ok.state() != EduExecutionState.SUCCEEDED) {
                throw new AssertionError(handler.definition().requirementId() + " normal=" + ok.state());
            }
            if (service.targets(ok.operationId()).size() != handler.targetKeys(command).size()) {
                throw new AssertionError("target plan " + handler.definition().requirementId());
            }
            if (service.audits(ok.operationId()).isEmpty()) {
                throw new AssertionError("audit missing " + handler.definition().requirementId());
            }
            normal++;

            var dup = service.execute(handler.definition().requirementId(), command);
            if (!dup.operationId().equals(ok.operationId())) {
                throw new AssertionError("idempotency " + handler.definition().requirementId());
            }
            duplicate++;

            EduFailurePoint representative = handler.definition().supportedFailures().stream()
                    .filter(point -> point != EduFailurePoint.NONE).findFirst().orElseThrow();
            var failedService = new EduExecutionService(registry, new MemoryRepository(), TestEduBusinessConsumers.registry(), Clock.systemUTC(), "failure");
            var failed = failedService.execute(handler.definition().requirementId(), command(handler, representative, "failure"));
            if (failed.state() == EduExecutionState.SUCCEEDED) {
                throw new AssertionError("failure not injected " + handler.definition().requirementId() + " " + representative);
            }
            representativeFailure++;
            var fixed = recover(failedService, failed);
            if (fixed.state() != EduExecutionState.SUCCEEDED) {
                throw new AssertionError("recovery " + handler.definition().requirementId() + " " + representative + "=" + fixed.state());
            }
            recovery++;
        }

        // Every declared failure point is executed for one concrete requirement in each functional family.
        for (var handler : representatives.values()) {
            for (var point : handler.definition().supportedFailures()) {
                if (point == EduFailurePoint.NONE) continue;
                var service = new EduExecutionService(registry, new MemoryRepository(), TestEduBusinessConsumers.registry(), Clock.systemUTC(), "exhaustive");
                var failed = service.execute(handler.definition().requirementId(), command(handler, point, "exhaustive-" + point));
                if (failed.state() == EduExecutionState.SUCCEEDED) {
                    throw new AssertionError("declared failure not executable " + handler.definition().requirementId() + " " + point);
                }
                var fixed = recover(service, failed);
                if (fixed.state() != EduExecutionState.SUCCEEDED) {
                    throw new AssertionError("declared recovery failed " + handler.definition().requirementId() + " " + point + "=" + fixed.state());
                }
                exhaustiveFailure++;
            }
        }

        // Durable restart/readback is exercised with the real file repository for every family.
        for (var handler : representatives.values()) {
            Path directory = root.resolve("durable-" + handler.definition().kind());
            var first = new EduExecutionService(registry, new FileEduOperationRepository(directory), TestEduBusinessConsumers.registry(), Clock.systemUTC(), "durable");
            var created = acknowledgeIfNeeded(first, first.execute(handler.definition().requirementId(), command(handler, EduFailurePoint.NONE, "durable")));
            var restarted = new EduExecutionService(registry, new FileEduOperationRepository(directory), TestEduBusinessConsumers.registry(), Clock.systemUTC(), "durable");
            if (!restarted.require(created.operationId()).equals(created)
                    || restarted.audits(created.operationId()).isEmpty()
                    || restarted.targets(created.operationId()).isEmpty()) {
                throw new AssertionError("durability " + handler.definition().requirementId());
            }
            persistence++;
        }

        // Multi-thread duplicate submission is exercised once per family against one shared atomic repository.
        for (var handler : representatives.values()) {
            MemoryRepository repository = new MemoryRepository();
            String suffix = "concurrent-" + handler.definition().kind();
            var concurrentCommand = command(handler, EduFailurePoint.NONE, suffix);
            ExecutorService pool = Executors.newFixedThreadPool(8);
            try {
                List<Future<String>> ids = new ArrayList<>();
                for (int i=0; i<8; i++) {
                    final int worker = i;
                    ids.add(pool.submit(() -> new EduExecutionService(registry, repository, TestEduBusinessConsumers.registry(), Clock.systemUTC(),
                            "concurrent-" + worker).execute(handler.definition().requirementId(), concurrentCommand).operationId()));
                }
                Set<String> unique = new HashSet<>();
                for (var future : ids) unique.add(future.get(20, TimeUnit.SECONDS));
                if (unique.size() != 1) throw new AssertionError("concurrent duplicate " + handler.definition().requirementId());
                concurrency++;
            } finally { pool.shutdownNow(); }
        }

        System.out.printf("[CPF][QA37][EDU135][PASS] contract=%d validation=%d authorization=%d normal=%d duplicate=%d representativeFailure=%d exhaustiveFailure=%d recovery=%d persistence=%d concurrency=%d root=%s%n",
                contract, validation, authorization, normal, duplicate, representativeFailure,
                exhaustiveFailure, recovery, persistence, concurrency, root);
    }

    private static EduOperationRecord recover(EduExecutionService service, EduOperationRecord failed) {
        EduOperationRecord recovered = service.retry(failed.operationId(), "operator", "self-test recovery");
        if (recovered.state() == EduExecutionState.WAITING_EXTERNAL
                || recovered.state() == EduExecutionState.UNKNOWN_RESULT
                || recovered.state() == EduExecutionState.RECONCILING) {
            recovered = service.acknowledgeExternal(recovered.operationId(), "operator", "self-test acknowledgement");
        }
        return recovered;
    }

    private static EduOperationRecord acknowledgeIfNeeded(EduExecutionService service, EduOperationRecord operation) {
        if (operation.state() == EduExecutionState.WAITING_EXTERNAL
                || operation.state() == EduExecutionState.UNKNOWN_RESULT
                || operation.state() == EduExecutionState.RECONCILING) {
            return service.acknowledgeExternal(operation.operationId(), "operator", "normal acknowledgement");
        }
        return operation;
    }

    private static EduExecutionCommand copy(EduExecutionCommand base, Map<String,Object> payload,
                                             Set<String> roles, String scope, EduFailurePoint point, String suffix) {
        return new EduExecutionCommand(base.businessKey(), base.idempotencyKey() + "-" + suffix,
                base.expectedVersion(), base.actorId(), roles, scope, base.requestReason(),
                UUID.randomUUID().toString(), base.traceId(), payload, point, true, true);
    }

    private static EduExecutionCommand command(AbstractEduCapabilityHandler handler, EduFailurePoint point, String suffix) {
        Map<String,Object> payload = new LinkedHashMap<>();
        for (String field : handler.definition().requiredFields()) payload.put(field, sample(field));
        return new EduExecutionCommand("business-" + handler.definition().requirementId(),
                suffix + "-" + handler.definition().requirementId(), 0, "selftest",
                Set.of(handler.definition().requiredRole()), "TENANT-A", "selftest",
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), payload,
                point, true, true);
    }

    private static Object sample(String field) {
        return switch (field) {
            case "amount" -> "1000.00";
            case "pageSize", "chunkSize", "batchSize", "gridSize", "queueSize", "contentLength",
                    "requestedUnits", "memberCount", "partitionCount", "timeout", "duration", "threshold" -> 4;
            case "port" -> 18080;
            case "trafficWeight", "weight", "maxUnavailable" -> 10;
            case "checksum" -> "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
            case "dbVendor", "databaseVendor", "vendor" -> "postgresql";
            case "route", "healthPath" -> "/edu/health";
            case "artifactPath", "installDir", "fileName" -> "build/edu/input.dat";
            case "endpoint", "endpointAlias", "callbackUrl" -> "https://partner.example/edu";
            case "validFrom", "effectiveFrom" -> "2026-01-01";
            case "validTo", "effectiveTo" -> "2026-12-31";
            case "fromVersion", "blueVersion" -> "1.0.0";
            case "toVersion", "greenVersion" -> "1.1.0";
            case "sort" -> "updatedAt,desc";
            case "businessDate", "effectiveDate", "targetDate" -> "2026-08-01";
            default -> field + "-value";
        };
    }

    /** Test-only atomic repository. No product profile can select this implementation. */
    private static final class MemoryRepository implements EduOperationRepository {
        private final Map<String,EduOperationRecord> operations = new LinkedHashMap<>();
        private final Map<String,String> idempotency = new HashMap<>();
        private final Map<String,List<EduAuditRecord>> audits = new HashMap<>();
        private final Map<String,LinkedHashMap<String,EduTargetRecord>> targets = new HashMap<>();
        private final Map<String,LinkedHashMap<String,EduOutboxRecord>> outbox = new HashMap<>();
        private final Map<String,Lease> leases = new HashMap<>();

        public synchronized EduCreateResult create(EduOperationRecord record) {
            String key = record.requirementId() + "|" + record.idempotencyKey();
            String existingId = idempotency.get(key);
            if (existingId != null) {
                EduOperationRecord existing = operations.get(existingId);
                if (!existing.payloadHash().equals(record.payloadHash())) {
                    throw new EduConflictException("Idempotency key payload mismatch");
                }
                return new EduCreateResult(existing, true);
            }
            operations.put(record.operationId(), record);
            idempotency.put(key, record.operationId());
            return new EduCreateResult(record, false);
        }
        public synchronized Optional<EduOperationRecord> find(String operationId) { return Optional.ofNullable(operations.get(operationId)); }
        public synchronized Optional<EduOperationRecord> findByIdempotency(String requirementId, String key) {
            return Optional.ofNullable(idempotency.get(requirementId + "|" + key)).map(operations::get);
        }
        public synchronized List<EduOperationRecord> findByRequirement(String requirementId, int limit) {
            return operations.values().stream().filter(value -> value.requirementId().equals(requirementId))
                    .sorted(Comparator.comparing(EduOperationRecord::createdAt).reversed()).limit(limit).toList();
        }
        public synchronized EduOperationRecord save(EduOperationRecord record, long expectedVersion) {
            EduOperationRecord old = operations.get(record.operationId());
            if (old == null) throw new NoSuchElementException(record.operationId());
            if (old.recordVersion() != expectedVersion) {
                throw new EduConflictException("record version conflict expected=" + expectedVersion + " actual=" + old.recordVersion());
            }
            operations.put(record.operationId(), record);
            return record;
        }
        public synchronized void appendAudit(EduAuditRecord audit) { audits.computeIfAbsent(audit.operationId(), ignored -> new ArrayList<>()).add(audit); }
        public synchronized List<EduAuditRecord> audits(String operationId) { return List.copyOf(audits.getOrDefault(operationId, List.of())); }
        public synchronized void saveTarget(EduTargetRecord target) { targets.computeIfAbsent(target.operationId(), ignored -> new LinkedHashMap<>()).put(target.targetId(), target); }
        public synchronized List<EduTargetRecord> targets(String operationId) { return List.copyOf(targets.getOrDefault(operationId, new LinkedHashMap<>()).values()); }
        public synchronized void enqueue(EduOutboxRecord event) { outbox.computeIfAbsent(event.operationId(), ignored -> new LinkedHashMap<>()).put(event.eventId(), event); }
        public synchronized void saveOutbox(EduOutboxRecord event) { enqueue(event); }
        public synchronized List<EduOutboxRecord> outbox(String operationId) { return List.copyOf(outbox.getOrDefault(operationId, new LinkedHashMap<>()).values()); }
        public synchronized long claimLease(String key, String owner, Instant expiresAt) {
            Lease lease = leases.get(key);
            Instant now = Instant.now();
            if (lease != null && lease.expiresAt().isAfter(now) && !lease.owner().equals(owner)) {
                throw new EduConflictException("lease held by " + lease.owner());
            }
            long token = lease == null ? 1 : lease.fencingToken() + 1;
            leases.put(key, new Lease(owner, token, expiresAt));
            return token;
        }
        private record Lease(String owner, long fencingToken, Instant expiresAt) {}
    }
}
