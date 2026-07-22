package com.cpf.external.execution.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpf.core.common.exception.CpfBusinessException;
import com.cpf.core.common.logging.TransactionIdGenerator;
import com.cpf.core.common.reconciliation.CpfReconciliationPort;
import com.cpf.core.common.reconciliation.CpfUnknownResultRecord;
import com.cpf.external.execution.domain.ExternalEndpointPolicy;
import com.cpf.external.execution.domain.ExternalExecution;
import com.cpf.external.execution.port.ExternalEndpointPort;
import com.cpf.external.execution.port.ExternalExecutionRepository;
import com.cpf.external.execution.port.ExternalUnknownResultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExternalExecutionServiceTest {

    private InMemoryRepository repository;
    private RecordingEndpoint endpoint;
    private RecordingReconciliation reconciliation;
    private ExternalExecutionService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRepository();
        endpoint = new RecordingEndpoint();
        reconciliation = new RecordingReconciliation();
        service = new ExternalExecutionService(
                repository,
                endpoint,
                reconciliation,
                new TransactionIdGenerator("EXS", "exsAP01", 7,
                        Clock.fixed(Instant.parse("2026-07-22T01:02:03.004Z"), ZoneOffset.UTC)),
                new ObjectMapper());
    }

    @Test
    void 정상_호출과_같은_멱등_요청은_한번만_전송한다() {
        ExternalExecution first = service.execute(command("KEY-1", Map.of("amount", 1000)));
        ExternalExecution replay = service.execute(command("KEY-1", Map.of("amount", 1000)));

        assertThat(first.status()).isEqualTo(ExternalExecution.Status.COMPLETED);
        assertThat(replay.executionId()).isEqualTo(first.executionId());
        assertThat(endpoint.executeCount.get()).isEqualTo(1);
    }

    @Test
    void 같은_멱등_키의_다른_본문은_충돌로_차단한다() {
        service.execute(command("KEY-2", Map.of("amount", 1000)));

        assertThatThrownBy(() -> service.execute(command("KEY-2", Map.of("amount", 2000))))
                .isInstanceOf(CpfBusinessException.class)
                .hasMessageContaining("멱등 키");
    }

    @Test
    void timeout은_결과_불명으로_등록하고_감사_사유로_복구한다() {
        endpoint.unknown = true;
        ExternalExecution unknown = service.execute(command("KEY-3", Map.of("amount", 1000)));

        assertThat(unknown.status()).isEqualTo(ExternalExecution.Status.UNKNOWN_RESULT);
        assertThat(unknown.unknownResultId()).isEqualTo("UNK-1");

        ExternalExecution recovered = service.reconcile(
                unknown.executionId(),
                new ExternalExecutionService.ReconcileCommand("COMPLETED", "operator01", "기관 원장 대조 완료"));

        assertThat(recovered.status()).isEqualTo(ExternalExecution.Status.COMPLETED);
        assertThat(reconciliation.resolvedReason).isEqualTo("기관 원장 대조 완료");
    }

    @Test
    void 수동_복구는_작업자와_감사_사유를_필수로_한다() {
        endpoint.unknown = true;
        ExternalExecution unknown = service.execute(command("KEY-4", Map.of("amount", 1000)));

        assertThatThrownBy(() -> service.reconcile(
                unknown.executionId(),
                new ExternalExecutionService.ReconcileCommand("FAILED", "operator01", "")))
                .isInstanceOf(CpfBusinessException.class)
                .hasMessageContaining("감사 사유");
    }

    private ExternalExecutionService.ExecuteCommand command(String key, Map<String, Object> payload) {
        return new ExternalExecutionService.ExecuteCommand("BANK-A", "TRANSFER", null, key, payload);
    }

    private static final class RecordingEndpoint implements ExternalEndpointPort {
        private final AtomicInteger executeCount = new AtomicInteger();
        private boolean unknown;

        @Override
        public Map<String, Object> execute(
                ExternalEndpointPolicy policy,
                String externalRequestId,
                String idempotencyKey,
                Map<String, Object> payload) {
            executeCount.incrementAndGet();
            if (unknown) {
                throw new ExternalUnknownResultException("TIMEOUT", "응답 대기 시간이 초과되었습니다.", null);
            }
            return Map.of("status", "COMPLETED", "externalRequestId", externalRequestId);
        }

        @Override
        public Map<String, Object> inquire(ExternalEndpointPolicy policy, String externalRequestId) {
            return Map.of("status", "COMPLETED");
        }
    }

    private static final class RecordingReconciliation implements CpfReconciliationPort {
        private String resolvedReason;

        @Override
        public CpfUnknownResultRecord register(CpfUnknownResultRecord record) {
            return new CpfUnknownResultRecord(
                    "UNK-1", record.unknownType(), record.unknownStatus(), record.transactionGlobalId(),
                    record.segmentId(), record.externalKey(), record.failureCode(), record.failureMessage(),
                    record.nextAction(), record.detectedAt(), null);
        }

        @Override
        public java.util.List<CpfUnknownResultRecord> find(String unknownType, String status, int limit) {
            return java.util.List.of();
        }

        @Override
        public void resolve(String unknownId, String status, String operatorId, String auditReason) {
            resolvedReason = auditReason;
        }
    }

    private static final class InMemoryRepository implements ExternalExecutionRepository {
        private final Map<String, ExternalExecution> byExecution = new LinkedHashMap<>();
        private final Map<String, String> byIdempotency = new LinkedHashMap<>();

        @Override
        public Optional<ExternalEndpointPolicy> findEndpointPolicy(String institutionCode, String endpointCode) {
            return Optional.of(new ExternalEndpointPolicy(
                    institutionCode, endpointCode, "EXS-SIMULATOR", "/execute",
                    "/result/{externalRequestId}", 1000, 0));
        }

        @Override
        public Optional<ExternalExecution> findByIdempotencyKey(String idempotencyKey) {
            return Optional.ofNullable(byIdempotency.get(idempotencyKey)).map(byExecution::get);
        }

        @Override
        public Optional<ExternalExecution> findByExecutionId(String executionId) {
            return Optional.ofNullable(byExecution.get(executionId));
        }

        @Override
        public void insert(ExternalExecution execution) {
            byExecution.put(execution.executionId(), execution);
            byIdempotency.put(execution.idempotencyKey(), execution.executionId());
        }

        @Override
        public void complete(String executionId, Map<String, Object> response) {
            update(executionId, ExternalExecution.Status.COMPLETED, response, null, null, null);
        }

        @Override
        public void fail(String executionId, String failureCode, String failureMessage) {
            update(executionId, ExternalExecution.Status.FAILED, Map.of(), null, failureCode, failureMessage);
        }

        @Override
        public void markUnknown(String executionId, String unknownResultId, String failureCode, String failureMessage) {
            update(executionId, ExternalExecution.Status.UNKNOWN_RESULT, Map.of(), unknownResultId, failureCode, failureMessage);
        }

        @Override
        public void reconcile(String executionId, ExternalExecution.Status status, String operatorId, String reason) {
            ExternalExecution before = byExecution.get(executionId);
            update(executionId, status, before.response(), before.unknownResultId(), before.failureCode(), before.failureMessage());
        }

        private void update(
                String executionId,
                ExternalExecution.Status status,
                Map<String, Object> response,
                String unknownId,
                String failureCode,
                String failureMessage) {
            ExternalExecution before = byExecution.get(executionId);
            byExecution.put(executionId, new ExternalExecution(
                    before.executionId(), before.institutionCode(), before.endpointCode(), before.externalRequestId(),
                    before.idempotencyKey(), before.requestHash(), status, response, unknownId,
                    failureCode, failureMessage, before.createdAt(), Instant.now()));
        }
    }
}
