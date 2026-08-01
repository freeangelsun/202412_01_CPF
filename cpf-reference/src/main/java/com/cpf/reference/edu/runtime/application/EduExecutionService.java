package com.cpf.reference.edu.runtime.application;

import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.persistence.EduOperationRepository;
import com.cpf.reference.edu.runtime.consumer.*;

import java.time.Clock;
import java.time.Instant;
import java.util.*;

/**
 * Durable execution engine shared by the 135 executable customer EDU scenarios.
 * The scenario-specific handler owns input and workflow policy; this engine owns
 * idempotency, optimistic concurrency, leases/fencing, target-level outcomes,
 * durable outbox, audit, retry, reconciliation and compensation semantics.
 */
public final class EduExecutionService {
    private final EduCapabilityRegistry registry;
    private final EduOperationRepository repository;
    private final EduBusinessConsumerRegistry consumers;
    private final Clock clock;
    private final String instanceId;

    public EduExecutionService(EduCapabilityRegistry registry,
                               EduOperationRepository repository,
                               EduBusinessConsumerRegistry consumers,
                               Clock clock,
                               String instanceId) {
        this.registry = Objects.requireNonNull(registry);
        this.repository = Objects.requireNonNull(repository);
        this.consumers = Objects.requireNonNull(consumers);
        this.clock = Objects.requireNonNull(clock);
        this.instanceId = requireText(instanceId, "instanceId");
    }

    public EduOperationRecord execute(String requirementId, EduExecutionCommand command) {
        AbstractEduCapabilityHandler handler = registry.require(requirementId);
        handler.validate(command);
        Instant now = clock.instant();
        String payloadHash = EduPayloadHasher.hash(command.payload());
        String operationId = UUID.randomUUID().toString();
        EduOperationRecord requested = new EduOperationRecord(
                operationId, requirementId, command.businessKey(), command.idempotencyKey(),
                payloadHash, command.actorId(), String.join(",", new TreeSet<>(command.roles())),
                command.dataScope(), EduExecutionState.REQUESTED, command.expectedVersion(),
                0, 0, 0, handler.definition().maxRetries(), command.failurePoint(),
                "REQUESTED", "accepted", command.requestId(), command.traceId(),
                command.payload(), Map.of(), now, now, null);

        EduCreateResult created = repository.create(requested);
        if (created.duplicate()) {
            return created.operation();
        }
        audit(created.operation(), "CREATE", null, EduExecutionState.REQUESTED, command);
        try {
            return run(handler, created.operation(), command);
        } catch (EduInjectedFailureException failure) {
            return markFailure(created.operation().operationId(), failure.point(), command);
        }
    }

    private EduOperationRecord run(AbstractEduCapabilityHandler handler,
                                   EduOperationRecord initial,
                                   EduExecutionCommand command) {
        EduOperationRecord current = transition(initial, EduExecutionState.VALIDATED,
                "VALIDATED", "input, role and scope validated", Map.of(), command);
        long fence = current.fencingToken();

        if (handler.definition().steps().contains(EduWorkflowStep.APPROVAL)
                && !command.autoApprove()) {
            return transition(current, EduExecutionState.WAITING_APPROVAL,
                    "APPROVAL_REQUIRED", "approval is required",
                    Map.of("requiredRole", handler.definition().requiredRole()), command);
        }

        if (handler.definition().leaseRequired()) {
            inject(command, EduFailurePoint.LEASE_LOST);
            fence = repository.claimLease(requirementLease(handler.definition(), command),
                    instanceId, clock.instant().plusSeconds(60));
            current = transition(current, EduExecutionState.CLAIMED,
                    "LEASE_CLAIMED", "lease claimed",
                    Map.of("fencingToken", fence), fence, current.retryCount(), command);
        }

        current = transition(current, EduExecutionState.IN_PROGRESS,
                "IN_PROGRESS", "workflow started",
                Map.of("steps", handler.definition().steps().stream().map(Enum::name).toList()),
                fence, current.retryCount(), command);

        List<String> targetKeys = handler.targetKeys(command);
        if (targetKeys.isEmpty()) {
            throw new EduValidationException(handler.definition().requirementId() + " target plan is empty");
        }
        int targetCount = targetKeys.size();
        List<EduTargetRecord> plannedTargets = new ArrayList<>();
        for (int i = 0; i < targetCount; i++) {
            String targetKey = targetKeys.get(i);
            boolean failed = command.failurePoint() == EduFailurePoint.PARTIAL_TARGET_FAILURE
                    && i == targetCount - 1;
            plannedTargets.add(new EduTargetRecord(
                    UUID.randomUUID().toString(), current.operationId(), targetKey,
                    failed ? "FAILED" : "APPLIED", Map.of(),
                    failed ? Map.of() : Map.of("businessKey", command.businessKey(),
                            "targetKey", targetKey, "fencingToken", fence),
                    failed ? "INJECTED_PARTIAL" : "",
                    failed ? "partial target failure" : "",
                    failed ? 0 : 1, clock.instant()));
        }

        inject(command, EduFailurePoint.TIMEOUT);
        inject(command, EduFailurePoint.PROCESS_KILL);
        inject(command, EduFailurePoint.BEFORE_COMMIT);

        EduConsumerBinding binding = handler.consumerBinding();
        boolean remoteEffect = handler.definition().externalEffect() || binding.type() == EduConsumerType.HTTP || binding.type() == EduConsumerType.OUTBOX;
        if (remoteEffect) inject(command, EduFailurePoint.BEFORE_EXTERNAL_SEND);
        EduBusinessConsumerResult consumerResult = consumers.invoke(binding, command, fence);
        if (remoteEffect && consumerResult.effectCommitted()) inject(command, EduFailurePoint.AFTER_EXTERNAL_SEND);

        for (EduTargetRecord target : plannedTargets) {
            repository.saveTarget(target);
        }
        Map<String, Object> result = new LinkedHashMap<>(handler.buildBusinessResult(command, fence));
        result.put("targetCount", targetCount);
        result.put("payloadHash", current.payloadHash());
        result.put("workflowSteps", handler.definition().steps().stream().map(Enum::name).toList());
        result.put("consumerType", binding.type().name());
        result.put("consumerOwner", binding.ownerModule());
        result.put("consumerEntryPoint", binding.entryPoint());
        result.put("consumerCode", consumerResult.code());
        result.put("consumerData", consumerResult.data());

        current = transition(current, EduExecutionState.IN_PROGRESS,
                "DB_COMMITTED", "business state committed", result,
                fence, current.retryCount(), command);
        inject(command, EduFailurePoint.AFTER_COMMIT);

        if (handler.definition().externalEffect() || consumerResult.externalPending()) {
            EduOutboxRecord event = new EduOutboxRecord(
                    UUID.randomUUID().toString(), current.operationId(), destination(handler.definition()),
                    command.businessKey(), Map.of(
                    "requirementId", handler.definition().requirementId(),
                    "operationId", current.operationId(),
                    "payloadHash", current.payloadHash()),
                    "READY", 0, clock.instant(), "", fence, clock.instant(), clock.instant());
            repository.enqueue(event);
            current = transition(current, EduExecutionState.WAITING_EXTERNAL,
                    "OUTBOX_READY", "durable outbox event created", result,
                    fence, current.retryCount(), command);

            repository.saveOutbox(new EduOutboxRecord(
                    event.eventId(), event.operationId(), event.destination(), event.eventKey(),
                    event.payload(), "SENT_UNKNOWN", event.attemptCount() + 1,
                    event.nextAttemptAt(), instanceId, fence, event.createdAt(), clock.instant()));

            if (!command.autoAcknowledge()) {
                return current;
            }
            acknowledgeOutbox(current.operationId(), command.actorId(), fence);
        }

        if (command.failurePoint() == EduFailurePoint.PARTIAL_TARGET_FAILURE) {
            return transition(current, EduExecutionState.PARTIAL_SUCCESS,
                    "PARTIAL_SUCCESS", "one or more targets failed", result,
                    fence, current.retryCount(), command);
        }

        current = transition(current, EduExecutionState.SUCCEEDED,
                "SUCCEEDED", "workflow completed", result,
                fence, current.retryCount(), command);
        inject(command, EduFailurePoint.RESPONSE_LOST);
        return current;
    }

    public EduOperationRecord retry(String operationId, String actor, String reason) {
        EduOperationRecord old = require(operationId);
        if (old.retryCount() >= old.maxRetries()) {
            return transition(old, EduExecutionState.FAILED_PERMANENT,
                    "RETRY_EXHAUSTED", "retry limit exhausted", old.result(),
                    old.fencingToken(), old.retryCount(),
                    commandFor(old, actor, reason, EduFailurePoint.NONE));
        }
        if (old.state() == EduExecutionState.UNKNOWN_RESULT
                || old.state() == EduExecutionState.PARTIAL_SUCCESS
                || old.state() == EduExecutionState.WAITING_EXTERNAL) {
            return reconcile(operationId, actor, reason);
        }
        if (old.state() != EduExecutionState.FAILED_RETRYABLE) {
            throw new EduConflictException("state cannot retry: " + old.state());
        }
        EduExecutionCommand command = commandFor(old, actor, reason, EduFailurePoint.NONE);
        EduOperationRecord reopened = transition(old, EduExecutionState.IN_PROGRESS,
                "RETRYING", "retry started", old.result(), old.fencingToken(),
                old.retryCount() + 1, command);
        return run(registry.require(old.requirementId()), reopened, command);
    }

    public EduOperationRecord reconcile(String operationId, String actor, String reason) {
        EduOperationRecord old = require(operationId);
        if (!(old.state() == EduExecutionState.UNKNOWN_RESULT
                || old.state() == EduExecutionState.PARTIAL_SUCCESS
                || old.state() == EduExecutionState.WAITING_EXTERNAL
                || old.state() == EduExecutionState.FAILED_RETRYABLE)) {
            throw new EduConflictException("state cannot reconcile: " + old.state());
        }
        EduExecutionCommand command = commandFor(old, actor, reason, EduFailurePoint.NONE);
        EduOperationRecord reconciling = transition(old, EduExecutionState.RECONCILING,
                "RECONCILING", "reconciliation started", old.result(),
                old.fencingToken(), old.retryCount(), command);

        long reprocessed = 0;
        for (EduTargetRecord target : repository.targets(operationId)) {
            if ("FAILED".equals(target.state())) {
                repository.saveTarget(new EduTargetRecord(
                        target.targetId(), target.operationId(), target.targetKey(), "APPLIED",
                        target.beforeValue(), Map.of("reprocessed", true,
                        "businessKey", old.businessKey()), "", "",
                        target.version() + 1, clock.instant()));
                reprocessed++;
            }
        }

        AbstractEduCapabilityHandler handler = registry.require(old.requirementId());
        List<EduOutboxRecord> events = repository.outbox(operationId);
        if (handler.definition().externalEffect() && events.isEmpty()) {
            repository.enqueue(new EduOutboxRecord(
                    UUID.randomUUID().toString(), old.operationId(), destination(handler.definition()),
                    old.businessKey(), Map.of("requirementId", old.requirementId(),
                    "operationId", old.operationId(), "payloadHash", old.payloadHash()),
                    "READY", 0, clock.instant(), "", old.fencingToken(),
                    clock.instant(), clock.instant()));
            events = repository.outbox(operationId);
        }

        boolean deliveryPending = events.stream()
                .anyMatch(event -> "READY".equals(event.state()) || "SENT_UNKNOWN".equals(event.state()));
        Map<String, Object> reconciledResult = merge(old.result(), Map.of("reprocessedTargets", reprocessed));
        if (deliveryPending) {
            return transition(reconciling, EduExecutionState.WAITING_EXTERNAL,
                    "DELIVERY_PENDING", "outbox delivery requires acknowledgement",
                    reconciledResult, old.fencingToken(), old.retryCount(), command);
        }
        return transition(reconciling, EduExecutionState.SUCCEEDED,
                "RECONCILED", "state reconciled", reconciledResult,
                old.fencingToken(), old.retryCount(), command);
    }

    public EduOperationRecord acknowledgeExternal(String operationId, String actor, String reason) {
        EduOperationRecord old = require(operationId);
        if (old.state() != EduExecutionState.WAITING_EXTERNAL
                && old.state() != EduExecutionState.UNKNOWN_RESULT
                && old.state() != EduExecutionState.RECONCILING) {
            throw new EduConflictException("state cannot acknowledge: " + old.state());
        }
        acknowledgeOutbox(operationId, actor, old.fencingToken());
        EduExecutionCommand command = commandFor(old, actor, reason, EduFailurePoint.NONE);
        return transition(old, EduExecutionState.SUCCEEDED,
                "EXTERNAL_ACKNOWLEDGED", "external result acknowledged", old.result(),
                old.fencingToken(), old.retryCount(), command);
    }

    private void acknowledgeOutbox(String operationId, String actor, long fencingToken) {
        for (EduOutboxRecord event : repository.outbox(operationId)) {
            repository.saveOutbox(new EduOutboxRecord(
                    event.eventId(), event.operationId(), event.destination(), event.eventKey(),
                    event.payload(), "ACKED", event.attemptCount() + 1,
                    event.nextAttemptAt(), actor, fencingToken,
                    event.createdAt(), clock.instant()));
        }
    }

    public EduOperationRecord compensate(String operationId, String actor, String reason) {
        EduOperationRecord old = require(operationId);
        AbstractEduCapabilityHandler handler = registry.require(old.requirementId());
        if (!handler.definition().compensationSupported()) {
            throw new EduConflictException("compensation not supported");
        }
        EduExecutionCommand command = commandFor(old, actor, reason, EduFailurePoint.NONE);
        EduOperationRecord compensating = transition(old, EduExecutionState.COMPENSATING,
                "COMPENSATING", "compensation started", old.result(),
                old.fencingToken(), old.retryCount(), command);
        for (EduTargetRecord target : repository.targets(operationId)) {
            repository.saveTarget(new EduTargetRecord(
                    target.targetId(), target.operationId(), target.targetKey(), "COMPENSATED",
                    target.afterValue(), target.beforeValue(), "", "",
                    target.version() + 1, clock.instant()));
        }
        return transition(compensating, EduExecutionState.COMPENSATED,
                "COMPENSATED", "business effects compensated",
                merge(old.result(), Map.of("compensated", true)),
                old.fencingToken(), old.retryCount(), command);
    }

    public EduOperationRecord cancel(String operationId, String actor, String reason) {
        EduOperationRecord old = require(operationId);
        if (old.state().terminal()) {
            throw new EduConflictException("terminal operation cannot cancel");
        }
        EduExecutionCommand command = commandFor(old, actor, reason, EduFailurePoint.NONE);
        return transition(old, EduExecutionState.CANCELLED,
                "CANCELLED", reason, old.result(), old.fencingToken(),
                old.retryCount(), command);
    }

    public EduOperationRecord require(String operationId) {
        return repository.find(operationId).orElseThrow(() -> new NoSuchElementException(operationId));
    }

    public List<EduOperationRecord> find(String requirementId, int limit) {
        return repository.findByRequirement(requirementId, limit);
    }

    public List<EduAuditRecord> audits(String operationId) { return repository.audits(operationId); }
    public List<EduTargetRecord> targets(String operationId) { return repository.targets(operationId); }
    public List<EduOutboxRecord> outbox(String operationId) { return repository.outbox(operationId); }

    private EduOperationRecord markFailure(String operationId,
                                           EduFailurePoint point,
                                           EduExecutionCommand command) {
        EduOperationRecord old = require(operationId);
        EduExecutionState state = switch (point) {
            case AFTER_COMMIT, AFTER_EXTERNAL_SEND, RESPONSE_LOST -> EduExecutionState.UNKNOWN_RESULT;
            case PARTIAL_TARGET_FAILURE -> EduExecutionState.PARTIAL_SUCCESS;
            default -> EduExecutionState.FAILED_RETRYABLE;
        };
        return transition(old, state, "INJECTED_" + point.name(),
                "failure injected at " + point, old.result(), old.fencingToken(),
                old.retryCount(), command);
    }

    private EduOperationRecord transition(EduOperationRecord old,
                                          EduExecutionState next,
                                          String code,
                                          String message,
                                          Map<String, Object> result,
                                          EduExecutionCommand command) {
        return transition(old, next, code, message, result,
                old.fencingToken(), old.retryCount(), command);
    }

    private EduOperationRecord transition(EduOperationRecord old,
                                          EduExecutionState next,
                                          String code,
                                          String message,
                                          Map<String, Object> result,
                                          long fencingToken,
                                          int retryCount,
                                          EduExecutionCommand command) {
        EduOperationRecord nextRecord = old.transition(next, code, message, result,
                fencingToken, retryCount, clock.instant());
        EduOperationRecord saved = repository.save(nextRecord, old.recordVersion());
        audit(saved, code, old.state(), next, command);
        return saved;
    }

    private void audit(EduOperationRecord operation,
                       String action,
                       EduExecutionState before,
                       EduExecutionState after,
                       EduExecutionCommand command) {
        repository.appendAudit(new EduAuditRecord(
                UUID.randomUUID().toString(), operation.operationId(), operation.requirementId(),
                action, before == null ? "" : before.name(), after.name(),
                command.actorId(), command.requestReason(), command.traceId(), clock.instant()));
    }

    private static String requirementLease(EduCapabilityDefinition definition,
                                           EduExecutionCommand command) {
        return definition.requirementId() + "|" + command.businessKey();
    }

    private static String destination(EduCapabilityDefinition definition) {
        return switch (definition.kind()) {
            case OPERATIONS -> "cpf.reference.operations.edu.operation";
            case BACKOFFICE -> "cpf.reference.backoffice.edu.operation";
            case GATEWAY -> "cpf.reference.gateway.edu.operation";
            case BATCH -> "cpf.reference.batch.edu.operation";
            case OPS -> "cpf.reference.ops.edu.operation";
            default -> "cpf.reference.edu.operation";
        };
    }

    private static void inject(EduExecutionCommand command, EduFailurePoint point) {
        if (command.failurePoint() == point) {
            throw new EduInjectedFailureException(point);
        }
    }

    private EduExecutionCommand commandFor(EduOperationRecord operation,
                                           String actor,
                                           String reason,
                                           EduFailurePoint failurePoint) {
        return new EduExecutionCommand(
                operation.businessKey(),
                operation.idempotencyKey() + "-action-" + UUID.randomUUID(),
                operation.expectedBusinessVersion(),
                requireText(actor, "actor"),
                Set.of(registry.require(operation.requirementId()).definition().requiredRole()),
                operation.dataScope(), requireText(reason, "reason"),
                UUID.randomUUID().toString(), operation.traceId(), operation.payload(),
                failurePoint, true, true);
    }

    private static Map<String, Object> merge(Map<String, Object> left, Map<String, Object> right) {
        Map<String, Object> merged = new LinkedHashMap<>(left);
        merged.putAll(right);
        return merged;
    }

    private static int parsePositiveInt(Object value, String name) {
        try {
            int parsed = Integer.parseInt(String.valueOf(value));
            if (parsed < 1 || parsed > 10_000) {
                throw new EduValidationException(name + " must be 1..10000");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new EduValidationException(name + " must be an integer");
        }
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new EduValidationException(name + " is required");
        }
        return value;
    }
}
