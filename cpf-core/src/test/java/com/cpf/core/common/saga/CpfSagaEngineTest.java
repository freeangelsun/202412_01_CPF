package com.cpf.core.common.saga;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CpfSagaEngineTest {

    @Test
    void successCompletesAllSteps() {
        MemoryStore store = new MemoryStore();
        List<String> calls = new ArrayList<>();
        CpfSagaDefinition definition = new CpfSagaDefinition("ORDER", List.of(
                step("reserve", calls, false, false),
                step("charge", calls, false, false)));

        CpfSagaSnapshot result = new CpfSagaEngine(store, new CpfSagaDefinitionRegistry())
                .execute(definition, "O-1", "TX-1", Map.of());

        assertEquals(CpfSagaStatus.COMPLETED, result.status());
        assertEquals(List.of("execute:reserve", "execute:charge"), calls);
    }

    @Test
    void failureCompensatesCompletedStepsInReverseOrder() {
        MemoryStore store = new MemoryStore();
        List<String> calls = new ArrayList<>();
        CpfSagaDefinition definition = new CpfSagaDefinition("ORDER", List.of(
                step("reserve", calls, false, false),
                step("charge", calls, false, false),
                step("ship", calls, true, false)));

        CpfSagaSnapshot result = new CpfSagaEngine(store, new CpfSagaDefinitionRegistry())
                .execute(definition, "O-2", "TX-2", Map.of());

        assertEquals(CpfSagaStatus.COMPENSATED, result.status());
        assertEquals(List.of(
                "execute:reserve", "execute:charge", "execute:ship",
                "compensate:charge", "compensate:reserve"), calls);
    }

    @Test
    void compensationFailureRequiresManualInterventionAndCanBeRetried() {
        MemoryStore store = new MemoryStore();
        List<String> calls = new ArrayList<>();
        FlakyCompensationStep reserve = new FlakyCompensationStep("reserve", calls);
        CpfSagaDefinition definition = new CpfSagaDefinition("ORDER", List.of(
                reserve,
                step("charge", calls, true, false)));
        CpfSagaDefinitionRegistry registry = new CpfSagaDefinitionRegistry();
        CpfSagaEngine engine = new CpfSagaEngine(store, registry);

        CpfSagaSnapshot failed = engine.execute(definition, "O-3", "TX-3", Map.of());
        assertEquals(CpfSagaStatus.MANUAL_INTERVENTION_REQUIRED, failed.status());

        reserve.allowCompensation = true;
        CpfSagaSnapshot recovered = new CpfSagaManualRecoveryService(store, registry)
                .retryCompensation(failed.sagaId(), "operator-1", "downstream recovered");

        assertEquals(CpfSagaStatus.COMPENSATED, recovered.status());
        assertEquals(1, store.manualAudits.size());
        assertThrows(IllegalArgumentException.class, () ->
                new CpfSagaManualRecoveryService(store, registry)
                        .retryCompensation(failed.sagaId(), "", "reason"));
    }

    private static CpfSagaStep step(String id, List<String> calls, boolean executeFail, boolean compensationFail) {
        return new CpfSagaStep() {
            public String stepId() { return id; }
            public CpfSagaStepResult execute(CpfSagaContext context) {
                calls.add("execute:" + id);
                if (executeFail) throw new IllegalStateException("execute-fail-" + id);
                return CpfSagaStepResult.success(id + "-snapshot");
            }
            public void compensate(CpfSagaContext context, CpfSagaStepResult executedResult) {
                calls.add("compensate:" + id);
                if (compensationFail) throw new IllegalStateException("compensate-fail-" + id);
            }
        };
    }

    private static final class FlakyCompensationStep implements CpfSagaStep {
        private final String id;
        private final List<String> calls;
        private boolean allowCompensation;
        private FlakyCompensationStep(String id, List<String> calls) { this.id = id; this.calls = calls; }
        public String stepId() { return id; }
        public CpfSagaStepResult execute(CpfSagaContext context) {
            calls.add("execute:" + id);
            return CpfSagaStepResult.success(id + "-snapshot");
        }
        public void compensate(CpfSagaContext context, CpfSagaStepResult result) {
            calls.add("compensate:" + id);
            if (!allowCompensation) throw new IllegalStateException("temporary-failure");
        }
    }

    private static final class MemoryStore implements CpfSagaStateStore {
        private final Map<String, MutableSaga> sagas = new LinkedHashMap<>();
        private final List<String> manualAudits = new ArrayList<>();

        public CpfSagaSnapshot create(CpfSagaContext context) {
            MutableSaga saga = new MutableSaga(context);
            sagas.put(context.sagaId(), saga);
            return saga.snapshot();
        }
        public Optional<CpfSagaSnapshot> find(String sagaId) {
            return Optional.ofNullable(sagas.get(sagaId)).map(MutableSaga::snapshot);
        }
        public void markSaga(String sagaId, CpfSagaStatus status, String errorMessage) {
            MutableSaga saga = required(sagaId);
            saga.status = status;
            saga.error = errorMessage;
            saga.version++;
        }
        public void markStep(String sagaId, int stepNo, String stepId, CpfSagaStepStatus status,
                             CpfSagaStepResult result, String errorMessage, boolean compensationAttempt) {
            MutableSaga saga = required(sagaId);
            MutableStep step = saga.steps.computeIfAbsent(stepNo, ignored -> new MutableStep(stepNo, stepId));
            step.status = status;
            step.error = errorMessage;
            if (result != null) {
                step.resultCode = result.resultCode();
                step.resultSnapshot = result.resultSnapshot();
            }
            if (status == CpfSagaStepStatus.RUNNING) step.executeAttempts++;
            if (compensationAttempt && status == CpfSagaStepStatus.COMPENSATING) step.compensationAttempts++;
        }
        public void auditManualAction(String sagaId, String actionType, String operatorId, String reason,
                                      String beforeStatus, String afterStatus) {
            manualAudits.add(String.join("|", sagaId, actionType, operatorId, reason, beforeStatus, afterStatus));
        }
        private MutableSaga required(String id) {
            MutableSaga saga = sagas.get(id);
            if (saga == null) throw new IllegalArgumentException(id);
            return saga;
        }
    }

    private static final class MutableSaga {
        private final CpfSagaContext context;
        private final Map<Integer, MutableStep> steps = new LinkedHashMap<>();
        private CpfSagaStatus status = CpfSagaStatus.RUNNING;
        private int version;
        private String error;
        private MutableSaga(CpfSagaContext context) { this.context = context; }
        private CpfSagaSnapshot snapshot() {
            return new CpfSagaSnapshot(context.sagaId(), context.sagaType(), context.businessKey(),
                    context.transactionId(), status, version, error,
                    steps.values().stream().map(MutableStep::snapshot).toList());
        }
    }

    private static final class MutableStep {
        private final int stepNo;
        private final String stepId;
        private CpfSagaStepStatus status = CpfSagaStepStatus.PENDING;
        private String resultCode;
        private String resultSnapshot;
        private String error;
        private int executeAttempts;
        private int compensationAttempts;
        private MutableStep(int stepNo, String stepId) { this.stepNo = stepNo; this.stepId = stepId; }
        private CpfSagaStepSnapshot snapshot() {
            return new CpfSagaStepSnapshot(stepNo, stepId, status, resultCode, resultSnapshot, error,
                    executeAttempts, compensationAttempts);
        }
    }
}
