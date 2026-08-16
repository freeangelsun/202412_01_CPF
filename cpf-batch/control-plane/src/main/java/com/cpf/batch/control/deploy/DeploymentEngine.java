package com.cpf.batch.control.deploy;

import com.cpf.batch.api.CommandState;
import com.cpf.batch.api.DeploymentCellManifest;
import com.cpf.batch.api.DeploymentRequest;
import com.cpf.batch.api.DeploymentResult;
import com.cpf.batch.api.DeploymentStrategy;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.batch.spi.DeploymentTargetAdapter;
import com.cpf.batch.spi.RuntimeHealthProbe;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** 배포 승인, Cell Lock, 단계별 Side Effect, 보상 및 결과불명 대사의 단일 상태기계입니다. */
@Service
public final class DeploymentEngine {
    private static final Set<CommandState> RELEASABLE_STATES = Set.of(
            CommandState.SUCCEEDED, CommandState.FAILED, CommandState.ROLLED_BACK);

    private final List<DeploymentTargetAdapter> adapters;
    private final RuntimeHealthProbe health;
    private final CompatibilityService compatibility;
    private final JdbcTemplate jdbc;
    private final DeploymentExecutionRepository executions;
    private final DeploymentCellLock cellLock;
    private final CpfVendorSqlCatalog sql;

    @Autowired
    public DeploymentEngine(
            List<DeploymentTargetAdapter> adapters,
            RuntimeHealthProbe health,
            CompatibilityService compatibility,
            JdbcTemplate jdbc,
            DeploymentExecutionRepository executions,
            DeploymentCellLock cellLock,
            CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this(adapters, health, compatibility, jdbc, executions, cellLock, sqlCatalogProvider.forModule("bat"));
    }

    DeploymentEngine(
            List<DeploymentTargetAdapter> adapters,
            RuntimeHealthProbe health,
            CompatibilityService compatibility,
            JdbcTemplate jdbc,
            DeploymentExecutionRepository executions,
            DeploymentCellLock cellLock,
            CpfVendorSqlCatalog sql) {
        this.adapters = List.copyOf(adapters);
        this.health = Objects.requireNonNull(health, "health");
        this.compatibility = Objects.requireNonNull(compatibility, "compatibility");
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
        this.executions = Objects.requireNonNull(executions, "executions");
        this.cellLock = Objects.requireNonNull(cellLock, "cellLock");
        this.sql = Objects.requireNonNull(sql, "sql");
    }

    public DeploymentResult deploy(DeploymentRequest request) {
        Instant startedAt = Instant.now();
        DeploymentResult invalid = validate(request, startedAt);
        if (invalid != null) return invalid;
        DeploymentCellManifest manifest = request.manifest();

        Optional<Map<String, Object>> existing;
        try {
            existing = executions.begin(request);
        } catch (DeploymentIdempotencyConflictException conflict) {
            return direct(request, CommandState.FAILED, "IDEMPOTENCY_CONFLICT", conflict.getMessage(), List.of(), startedAt);
        } catch (RuntimeException storeFailure) {
            return direct(request, CommandState.UNKNOWN_RESULT, "DEPLOYMENT_EXECUTION_STORE",
                    "Deployment execution could not be durably started", List.of(), startedAt);
        }
        if (existing.isPresent()) return fromExisting(request, existing.get(), startedAt);

        DeploymentResult lockFailure = acquire(request, startedAt);
        if (lockFailure != null) return lockFailure;

        List<DeploymentResult.InstanceResult> results = new ArrayList<>();
        Sequence sequence = new Sequence();
        int healthy;
        try {
            healthy = currentHealthy(manifest.serviceId());
        } catch (RuntimeException queryFailure) {
            return releaseAfterOutcome(request, finishSafely(request, CommandState.FAILED,
                    "HEALTH_INVENTORY_QUERY", "Healthy instance inventory could not be loaded", results, startedAt));
        }

        try {
            for (DeploymentCellManifest.Instance instance : ordered(manifest)) {
                if (healthy - 1 < manifest.deployment().minHealthy()) {
                    return releaseAfterOutcome(request,
                            compensateKnown(request, results, startedAt, "MIN_HEALTHY", sequence,
                                    "Minimum healthy instance policy would be violated"));
                }
                DeploymentTargetAdapter adapter;
                try {
                    adapter = adapter(manifest, instance);
                } catch (RuntimeException missingAdapter) {
                    return releaseAfterOutcome(request,
                            compensateKnown(request, results, startedAt, "ADAPTER_RESOLUTION", sequence,
                                    "No deployment adapter is available for " + instance.instanceId()));
                }

                DeploymentResult.InstanceResult drain = invoke(instance, "DRAIN", () -> adapter.drain(manifest, instance));
                record(request, sequence.next(), drain, results);
                if (unknown(drain)) return retainUnknown(request, "DRAIN_UNKNOWN", drain.message(), results, startedAt);
                if (!success(drain)) return releaseAfterOutcome(request,
                        compensateKnown(request, results, startedAt, "DRAIN", sequence, drain.message()));
                healthy = Math.max(0, healthy - 1);

                DeploymentResult.InstanceResult install = invoke(instance, "INSTALL", () -> adapter.deploy(manifest, instance));
                record(request, sequence.next(), install, results);
                if (unknown(install)) return retainUnknown(request, "INSTALL_UNKNOWN", install.message(), results, startedAt);
                if (!success(install)) return releaseAfterOutcome(request,
                        compensateKnown(request, results, startedAt, "INSTALL", sequence, install.message()));

                DeploymentResult.InstanceResult start = invoke(instance, "START", () -> adapter.start(manifest, instance));
                record(request, sequence.next(), start, results);
                if (unknown(start)) return retainUnknown(request, "START_UNKNOWN", start.message(), results, startedAt);
                if (!success(start)) return releaseAfterOutcome(request,
                        compensateKnown(request, results, startedAt, "START", sequence, start.message()));

                RuntimeHealthProbe.Health readiness = health.probe(
                        instance, manifest.deployment().healthPath(), manifest.deployment().healthTimeoutSeconds());
                if (!readiness.ready()) {
                    DeploymentResult.InstanceResult failed = new DeploymentResult.InstanceResult(
                            instance.instanceId(), CommandState.FAILED, "READINESS", safe(readiness.detail()));
                    record(request, sequence.next(), failed, results);
                    return releaseAfterOutcome(request,
                            compensateKnown(request, results, startedAt, "READINESS", sequence, failed.message()));
                }

                if (manifest.deployment().functionalSmokeRequired()) {
                    RuntimeHealthProbe.Health smoke = health.smoke(instance, manifest.deployment().healthTimeoutSeconds());
                    if (!smoke.ready()) {
                        DeploymentResult.InstanceResult failed = new DeploymentResult.InstanceResult(
                                instance.instanceId(), CommandState.FAILED, "FUNCTIONAL_SMOKE", safe(smoke.detail()));
                        record(request, sequence.next(), failed, results);
                        return releaseAfterOutcome(request,
                                compensateKnown(request, results, startedAt, "FUNCTIONAL_SMOKE", sequence, failed.message()));
                    }
                }

                DeploymentResult.InstanceResult resume = invoke(instance, "ADMISSION", () -> adapter.resume(manifest, instance));
                record(request, sequence.next(), resume, results);
                if (unknown(resume)) return retainUnknown(request, "ADMISSION_UNKNOWN", resume.message(), results, startedAt);
                if (!success(resume)) return releaseAfterOutcome(request,
                        compensateKnown(request, results, startedAt, "ADMISSION", sequence, resume.message()));
                healthy++;
            }
            return releaseAfterOutcome(request,
                    finishSafely(request, CommandState.SUCCEEDED, null, "Deployment completed", results, startedAt));
        } catch (DeploymentLedgerPersistenceException storeFailure) {
            return direct(request, CommandState.UNKNOWN_RESULT, "DEPLOYMENT_EXECUTION_STORE",
                    "Deployment side effect could not be durably recorded; reconcile before retry", results, startedAt);
        } catch (RuntimeException unexpected) {
            return releaseAfterOutcome(request,
                    compensateKnown(request, results, startedAt, "UNEXPECTED", sequence,
                            "Unexpected deployment failure: " + unexpected.getClass().getSimpleName()));
        }
    }

    public DeploymentResult rollbackApproved(DeploymentRequest request) {
        Instant startedAt = Instant.now();
        DeploymentResult invalid = validateApproval(request, startedAt);
        if (invalid != null) return invalid;
        Optional<Map<String, Object>> existing;
        try {
            existing = executions.begin(request);
        } catch (DeploymentIdempotencyConflictException conflict) {
            return direct(request, CommandState.FAILED, "IDEMPOTENCY_CONFLICT", conflict.getMessage(), List.of(), startedAt);
        } catch (RuntimeException storeFailure) {
            return direct(request, CommandState.UNKNOWN_RESULT, "DEPLOYMENT_EXECUTION_STORE",
                    "Rollback execution could not be durably started", List.of(), startedAt);
        }
        if (existing.isPresent()) return fromExisting(request, existing.get(), startedAt);
        DeploymentResult lockFailure = acquire(request, startedAt);
        if (lockFailure != null) return lockFailure;

        List<DeploymentResult.InstanceResult> results = new ArrayList<>();
        Sequence sequence = new Sequence();
        boolean failed = false;
        try {
            for (DeploymentCellManifest.Instance instance : request.manifest().instances()) {
                DeploymentTargetAdapter adapter = adapter(request.manifest(), instance);
                DeploymentResult.InstanceResult drain = invoke(instance, "DRAIN", () -> adapter.drain(request.manifest(), instance));
                record(request, sequence.next(), drain, results);
                if (unknown(drain)) return retainUnknown(request, "ROLLBACK_DRAIN_UNKNOWN", drain.message(), results, startedAt);
                if (!success(drain)) { failed = true; continue; }

                DeploymentResult.InstanceResult rollback = invoke(instance, "ROLLBACK", () -> adapter.rollback(request.manifest(), instance));
                record(request, sequence.next(), rollback, results);
                if (unknown(rollback)) return retainUnknown(request, "ROLLBACK_UNKNOWN", rollback.message(), results, startedAt);
                if (!success(rollback)) { failed = true; continue; }

                RuntimeHealthProbe.Health readiness = health.probe(instance,
                        request.manifest().deployment().healthPath(), request.manifest().deployment().healthTimeoutSeconds());
                if (!readiness.ready()) {
                    record(request, sequence.next(), new DeploymentResult.InstanceResult(instance.instanceId(),
                            CommandState.FAILED, "ROLLBACK_READINESS", safe(readiness.detail())), results);
                    failed = true;
                    continue;
                }

                DeploymentResult.InstanceResult resume = invoke(instance, "ADMISSION", () -> adapter.resume(request.manifest(), instance));
                record(request, sequence.next(), resume, results);
                if (unknown(resume)) return retainUnknown(request, "ROLLBACK_ADMISSION_UNKNOWN", resume.message(), results, startedAt);
                if (!success(resume)) failed = true;
            }
            CommandState state = failed ? CommandState.PARTIALLY_ROLLED_BACK : CommandState.ROLLED_BACK;
            DeploymentResult result = finishSafely(request, state, failed ? "ROLLBACK_PARTIAL" : null,
                    failed ? "Rollback incomplete" : "Rollback completed", results, startedAt);
            return releaseAfterOutcome(request, result);
        } catch (DeploymentLedgerPersistenceException storeFailure) {
            return direct(request, CommandState.UNKNOWN_RESULT, "DEPLOYMENT_EXECUTION_STORE",
                    "Rollback side effect could not be durably recorded; reconcile before retry", results, startedAt);
        } catch (RuntimeException unexpected) {
            return retainUnknown(request, "ROLLBACK_UNEXPECTED",
                    "Rollback failed with an unresolved result: " + unexpected.getClass().getSimpleName(), results, startedAt);
        }
    }

    public DeploymentReconciliation reconcileLockResult(
            String deploymentId,
            String requestedBy,
            String approvedBy,
            String approvalRequestId,
            String reason) {
        requireApproval(deploymentId, requestedBy, approvedBy, approvalRequestId, reason);
        Map<String, Object> execution = executions.findByDeploymentId(deploymentId)
                .orElseThrow(() -> new IllegalArgumentException("DEPLOYMENT_EXECUTION_NOT_FOUND"));
        String cellId = text(execution.get("cell_id"));
        String previousState = textOr(execution.get("execution_state"), "UNKNOWN_RESULT");
        String failureStage = text(execution.get("failure_stage"));
        List<Map<String, Object>> rows = executions.instanceResults(deploymentId);
        List<DeploymentReconciliation.SideEffect> effects = rows.stream().map(row ->
                new DeploymentReconciliation.SideEffect(number(row.get("sequence_no")), text(row.get("instance_id")),
                        text(row.get("stage_code")), text(row.get("result_state")), safe(text(row.get("result_message")))))
                .toList();
        LockObservation lock = observeLock(cellId);
        if (!"UNKNOWN_RESULT".equals(previousState) || !failureStage.startsWith("DEPLOYMENT_LOCK")) {
            return new DeploymentReconciliation(deploymentId, cellId, previousState,
                    "NOT_LOCK_UNKNOWN_RESULT", lock.owner(), effects.size(), effects,
                    "Execution is not a lock-related unknown result", Instant.now());
        }
        if (!effects.isEmpty()) {
            return new DeploymentReconciliation(deploymentId, cellId, previousState,
                    "SIDE_EFFECT_RECONCILIATION_REQUIRED", lock.owner(), effects.size(), effects,
                    "Adapter side effects exist; automatic lock-only recovery is prohibited", Instant.now());
        }
        if (!lock.available()) {
            return new DeploymentReconciliation(deploymentId, cellId, previousState,
                    "LOCK_STORE_UNAVAILABLE", lock.owner(), 0, List.of(),
                    "Lock store remains unavailable; execution is still unknown", Instant.now());
        }

        String state;
        String detail;
        if (deploymentId.equals(lock.owner())) {
            cellLock.release(cellId, deploymentId);
            state = "LOCK_RELEASED_NOT_STARTED";
            detail = "Lock was held by the unknown request and was released; no adapter side effect was recorded";
        } else if (lock.owner() == null || lock.owner().isBlank()) {
            state = "LOCK_NOT_HELD_NOT_STARTED";
            detail = "No lock and no adapter side effect were found";
        } else {
            state = "LOCK_OWNED_BY_OTHER_NOT_STARTED";
            detail = "Another deployment owns the cell lock; the unknown request had no adapter side effect";
        }
        executions.reconcileTerminal(deploymentId, CommandState.FAILED, "DEPLOYMENT_LOCK_RECONCILED", detail,
                requestedBy.trim(), approvedBy.trim(), approvalRequestId.trim(), reason.trim());
        return new DeploymentReconciliation(deploymentId, cellId, previousState, state, lock.owner(), 0, List.of(), detail, Instant.now());
    }

    private DeploymentResult compensateKnown(
            DeploymentRequest request,
            List<DeploymentResult.InstanceResult> results,
            Instant startedAt,
            String failureStage,
            Sequence sequence,
            String failureDetail) {
        boolean compensationFailed = false;
        boolean compensationUnknown = false;
        boolean hadSideEffect = false;
        Map<String, InstanceSideEffects> effects = new LinkedHashMap<>();
        for (DeploymentResult.InstanceResult result : results) {
            InstanceSideEffects side = effects.computeIfAbsent(result.instanceId(), ignored -> new InstanceSideEffects());
            if ("DRAIN".equals(result.stage()) && success(result)) { side.drained = true; hadSideEffect = true; }
            if ("INSTALL".equals(result.stage()) && success(result)) { side.artifactChanged = true; hadSideEffect = true; }
            if ("ADMISSION".equals(result.stage()) && success(result)) side.drained = false;
        }

        List<DeploymentCellManifest.Instance> reverse = new ArrayList<>(request.manifest().instances());
        Collections.reverse(reverse);
        for (DeploymentCellManifest.Instance instance : reverse) {
            InstanceSideEffects side = effects.get(instance.instanceId());
            if (side == null) continue;
            DeploymentTargetAdapter target = adapter(request.manifest(), instance);
            if (side.artifactChanged) {
                DeploymentResult.InstanceResult rollback = invoke(instance, "ROLLBACK",
                        () -> target.rollback(request.manifest(), instance));
                record(request, sequence.next(), rollback, results);
                if (unknown(rollback)) { compensationUnknown = true; continue; }
                if (!success(rollback)) { compensationFailed = true; continue; }
                DeploymentResult.InstanceResult resume = invoke(instance, "RESUME_COMPENSATION",
                        () -> target.resume(request.manifest(), instance));
                record(request, sequence.next(), resume, results);
                if (unknown(resume)) compensationUnknown = true;
                else if (!success(resume)) compensationFailed = true;
            } else if (side.drained) {
                DeploymentResult.InstanceResult resume = invoke(instance, "RESUME_COMPENSATION",
                        () -> target.resume(request.manifest(), instance));
                record(request, sequence.next(), resume, results);
                if (unknown(resume)) compensationUnknown = true;
                else if (!success(resume)) compensationFailed = true;
            }
        }

        CommandState state;
        String message;
        if (compensationUnknown) {
            state = CommandState.UNKNOWN_RESULT;
            message = "Deployment failed and compensation result is unknown; reconcile before retry";
        } else if (compensationFailed) {
            state = CommandState.PARTIALLY_ROLLED_BACK;
            message = "Deployment failed and compensation is incomplete";
        } else if (hadSideEffect) {
            state = CommandState.ROLLED_BACK;
            message = "Deployment failed and known side effects were compensated";
        } else {
            state = CommandState.FAILED;
            message = "Deployment failed before a durable side effect was completed";
        }
        if (failureDetail != null && !failureDetail.isBlank()) message += ": " + safe(failureDetail);
        return finishSafely(request, state, failureStage, message, results, startedAt);
    }

    private DeploymentResult.InstanceResult invoke(
            DeploymentCellManifest.Instance instance,
            String expectedStage,
            Supplier<DeploymentResult.InstanceResult> operation) {
        try {
            DeploymentResult.InstanceResult result = operation.get();
            if (result == null || result.state() == CommandState.EXECUTING
                    || !instance.instanceId().equals(result.instanceId())) {
                return new DeploymentResult.InstanceResult(instance.instanceId(), CommandState.UNKNOWN_RESULT,
                        expectedStage, "Deployment adapter returned a non-terminal or mismatched result");
            }
            return new DeploymentResult.InstanceResult(instance.instanceId(), result.state(), expectedStage, safe(result.message()));
        } catch (RuntimeException failure) {
            return new DeploymentResult.InstanceResult(instance.instanceId(), CommandState.UNKNOWN_RESULT,
                    expectedStage, "Deployment adapter result is unknown: " + failure.getClass().getSimpleName());
        }
    }

    private void record(
            DeploymentRequest request,
            int sequence,
            DeploymentResult.InstanceResult result,
            List<DeploymentResult.InstanceResult> all) {
        all.add(result);
        try {
            executions.instance(request.deploymentId(), sequence, result);
        } catch (RuntimeException failure) {
            throw new DeploymentLedgerPersistenceException("Deployment instance result was not persisted", failure);
        }
    }

    private DeploymentResult finishSafely(
            DeploymentRequest request,
            CommandState state,
            String stage,
            String message,
            List<DeploymentResult.InstanceResult> results,
            Instant startedAt) {
        try {
            executions.finish(request.deploymentId(), state, stage, message);
            return direct(request, state, stage, message, results, startedAt);
        } catch (RuntimeException failure) {
            return direct(request, CommandState.UNKNOWN_RESULT, "DEPLOYMENT_EXECUTION_STORE",
                    "Deployment final state could not be durably persisted", results, startedAt);
        }
    }

    private DeploymentResult retainUnknown(
            DeploymentRequest request,
            String stage,
            String message,
            List<DeploymentResult.InstanceResult> results,
            Instant startedAt) {
        return finishSafely(request, CommandState.UNKNOWN_RESULT, stage,
                safe(message) + "; cell lock retained for reconciliation", results, startedAt);
    }

    private DeploymentResult releaseAfterOutcome(DeploymentRequest request, DeploymentResult result) {
        if (!RELEASABLE_STATES.contains(result.state())) return result;
        try {
            cellLock.release(request.manifest().cellId(), request.deploymentId());
            return result;
        } catch (RuntimeException releaseFailure) {
            String message = "Deployment outcome is known but cell lock release is unknown";
            try {
                executions.finish(request.deploymentId(), CommandState.UNKNOWN_RESULT,
                        "DEPLOYMENT_LOCK_RELEASE", message);
            } catch (RuntimeException ignored) {
                // Both execution store and lock store are unavailable. Return fail-closed unknown result.
            }
            return direct(request, CommandState.UNKNOWN_RESULT, "DEPLOYMENT_LOCK_RELEASE",
                    message, result.instances(), result.startedAt());
        }
    }

    private DeploymentResult acquire(DeploymentRequest request, Instant startedAt) {
        try {
            if (cellLock.acquire(request.manifest().cellId(), request.deploymentId()) == DeploymentCellLock.Acquisition.ACQUIRED) {
                return null;
            }
            return finishSafely(request, CommandState.FAILED, "DEPLOYMENT_LOCK",
                    "Cell is already locked", List.of(), startedAt);
        } catch (RuntimeException failure) {
            return finishSafely(request, CommandState.UNKNOWN_RESULT, "DEPLOYMENT_LOCK_STORE",
                    "Deployment lock store is unavailable: " + failure.getClass().getSimpleName(),
                    List.of(), startedAt);
        }
    }

    private DeploymentResult validate(DeploymentRequest request, Instant startedAt) {
        DeploymentResult approval = validateApproval(request, startedAt);
        if (approval != null) return approval;
        CompatibilityService.Result result;
        try {
            result = compatibility.evaluate(request.manifest().artifact(), request.manifest().environment());
        } catch (RuntimeException failure) {
            return direct(request, CommandState.FAILED, "CAN_DEPLOY",
                    "Compatibility evidence could not be evaluated", List.of(), startedAt);
        }
        if (!result.allowed()) return direct(request, CommandState.FAILED, "CAN_DEPLOY",
                result.reason(), List.of(), startedAt);
        return null;
    }

    private DeploymentResult validateApproval(DeploymentRequest request, Instant startedAt) {
        if (request == null || request.manifest() == null || request.manifest().deployment() == null) {
            return new DeploymentResult(request == null ? null : request.deploymentId(), CommandState.FAILED,
                    "REQUEST", "Deployment request, manifest and policy are required", null, null,
                    List.of(), startedAt, Instant.now());
        }
        if (blank(request.deploymentId()) || blank(request.idempotencyKey()) || blank(request.approvalRequestId())
                || blank(request.requestedBy()) || blank(request.approvedBy()) || blank(request.reason())
                || request.requestedBy().trim().equals(request.approvedBy().trim())) {
            return direct(request, CommandState.FAILED, "APPROVAL",
                    "Deployment id, idempotency key, verified approval id, requester/approver separation and reason are required",
                    List.of(), startedAt);
        }
        if (request.expectedVersion() < 0) return direct(request, CommandState.FAILED, "EXPECTED_VERSION",
                "Expected version must not be negative", List.of(), startedAt);
        if (request.expiresAt() == null || !request.expiresAt().isAfter(Instant.now())) {
            return direct(request, CommandState.FAILED, "EXPIRY", "Approval is missing or expired", List.of(), startedAt);
        }
        return null;
    }

    private DeploymentResult fromExisting(DeploymentRequest request, Map<String, Object> existing, Instant startedAt) {
        CommandState state;
        try {
            state = CommandState.valueOf(textOr(existing.get("execution_state"), "UNKNOWN_RESULT"));
        } catch (IllegalArgumentException invalidState) {
            state = CommandState.UNKNOWN_RESULT;
        }
        return direct(request, state, text(existing.get("failure_stage")),
                textOr(existing.get("result_message"), "Existing idempotent deployment"), List.of(), startedAt,
                text(existing.get("from_version")), textOr(existing.get("to_version"), request.manifest().artifact().version()));
    }

    private DeploymentResult direct(
            DeploymentRequest request,
            CommandState state,
            String stage,
            String message,
            List<DeploymentResult.InstanceResult> results,
            Instant startedAt) {
        return direct(request, state, stage, message, results, startedAt, null,
                request == null || request.manifest() == null || request.manifest().artifact() == null
                        ? null : request.manifest().artifact().version());
    }

    private DeploymentResult direct(
            DeploymentRequest request,
            CommandState state,
            String stage,
            String message,
            List<DeploymentResult.InstanceResult> results,
            Instant startedAt,
            String fromVersion,
            String toVersion) {
        return new DeploymentResult(request == null ? null : request.deploymentId(), state, stage, safe(message),
                fromVersion, toVersion, List.copyOf(results), startedAt, Instant.now());
    }

    private List<DeploymentCellManifest.Instance> ordered(DeploymentCellManifest manifest) {
        if (manifest.deployment().strategy() == DeploymentStrategy.CANARY && manifest.instances().size() > 1) {
            List<DeploymentCellManifest.Instance> result = new ArrayList<>();
            result.add(manifest.instances().getFirst());
            result.addAll(manifest.instances().subList(1, manifest.instances().size()));
            return result;
        }
        return manifest.instances();
    }

    private DeploymentTargetAdapter adapter(DeploymentCellManifest manifest, DeploymentCellManifest.Instance instance) {
        return adapters.stream().filter(candidate -> candidate.supports(instance, manifest.runtimeMode())).findFirst()
                .orElseThrow(() -> new IllegalStateException("No deployment adapter for " + instance.instanceId()));
    }

    private int currentHealthy(String serviceId) {
        Integer count = jdbc.queryForObject(sql.required("deploy-runtime-healthy-count"), Integer.class, serviceId);
        if (count == null || count < 0) throw new IllegalStateException("Healthy instance count is unavailable");
        return count;
    }

    private LockObservation observeLock(String cellId) {
        try {
            return new LockObservation(true, cellLock.owner(cellId));
        } catch (RuntimeException failure) {
            return new LockObservation(false, "LOCK_STORE_UNAVAILABLE");
        }
    }

    private static void requireApproval(
            String deploymentId, String requestedBy, String approvedBy, String approvalRequestId, String reason) {
        if (blank(deploymentId) || blank(requestedBy) || blank(approvedBy) || blank(approvalRequestId)
                || blank(reason) || reason.trim().length() < 5 || requestedBy.trim().equals(approvedBy.trim())) {
            throw new SecurityException("DEPLOYMENT_RECONCILIATION_APPROVAL_REQUIRED");
        }
    }

    private static boolean success(DeploymentResult.InstanceResult result) {
        return result.state() == CommandState.SUCCEEDED || result.state() == CommandState.ROLLED_BACK;
    }

    private static boolean unknown(DeploymentResult.InstanceResult result) {
        return result.state() == CommandState.UNKNOWN_RESULT || result.state() == CommandState.EXECUTING;
    }

    private static String safe(String value) {
        String safe = SensitiveTextSanitizer.sanitize(value == null ? "" : value);
        return safe.length() > 512 ? safe.substring(0, 512) : safe;
    }

    private static String text(Object value) { return value == null ? "" : value.toString().trim(); }
    private static String textOr(Object value, String fallback) { String text = text(value); return text.isBlank() ? fallback : text; }
    private static int number(Object value) { return value instanceof Number number ? number.intValue() : 0; }
    private static boolean blank(String value) { return value == null || value.isBlank(); }

    private static final class InstanceSideEffects { boolean drained; boolean artifactChanged; }
    private static final class Sequence { private int value; int next() { return ++value; } }
    private record LockObservation(boolean available, String owner) { }

    private static final class DeploymentLedgerPersistenceException extends RuntimeException {
        DeploymentLedgerPersistenceException(String message, Throwable cause) { super(message, cause); }
    }
}
