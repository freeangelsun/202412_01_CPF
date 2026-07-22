package com.cpf.external.execution.application;

import com.cpf.external.common.base.ExternalBaseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.cpf.core.common.exception.CpfBusinessException;
import com.cpf.core.common.exception.CpfExternalServiceException;
import com.cpf.core.common.logging.SensitiveDataMasker;
import com.cpf.core.common.logging.TransactionIdGenerator;
import com.cpf.core.common.reconciliation.CpfReconciliationPort;
import com.cpf.core.common.reconciliation.CpfUnknownResultRecord;
import com.cpf.external.execution.domain.ExternalEndpointPolicy;
import com.cpf.external.execution.domain.ExternalExecution;
import com.cpf.external.execution.port.ExternalEndpointPort;
import com.cpf.external.execution.port.ExternalExecutionRepository;
import com.cpf.external.execution.port.ExternalUnknownResultException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

/** 대외 요청의 멱등 처리, 결과 불명 등록, 재조회와 수동 복구를 조정합니다. */
@Service
public class ExternalExecutionService extends ExternalBaseService {

    private final ExternalExecutionRepository repository;
    private final ExternalEndpointPort endpointPort;
    private final CpfReconciliationPort reconciliationPort;
    private final TransactionIdGenerator transactionIdGenerator;
    private final ObjectMapper objectMapper;

    public ExternalExecutionService(
            ExternalExecutionRepository repository,
            ExternalEndpointPort endpointPort,
            CpfReconciliationPort reconciliationPort,
            TransactionIdGenerator transactionIdGenerator,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.endpointPort = endpointPort;
        this.reconciliationPort = reconciliationPort;
        this.transactionIdGenerator = transactionIdGenerator;
        this.objectMapper = objectMapper;
    }

    public ExternalExecution execute(ExecuteCommand command) {
        String requestHash = requestHash(command.payload());
        ExternalExecution existing = repository.findByIdempotencyKey(command.idempotencyKey()).orElse(null);
        if (existing != null) {
            return sameRequestOrConflict(existing, requestHash);
        }

        ExternalEndpointPolicy policy = repository.findEndpointPolicy(command.institutionCode(), command.endpointCode())
                .orElseThrow(() -> new CpfBusinessException("사용 가능한 대외 endpoint 정책이 없습니다."));
        ExternalExecution requested = requested(command, requestHash);
        try {
            repository.insert(requested);
        } catch (DuplicateKeyException exception) {
            ExternalExecution concurrent = repository.findByIdempotencyKey(command.idempotencyKey())
                    .orElseThrow(() -> exception);
            return sameRequestOrConflict(concurrent, requestHash);
        }

        try {
            Map<String, Object> response = endpointPort.execute(
                    policy, requested.externalRequestId(), requested.idempotencyKey(), command.payload());
            repository.complete(requested.executionId(), response);
            return requireExecution(requested.executionId());
        } catch (ExternalUnknownResultException exception) {
            return registerUnknown(requested, exception);
        } catch (RuntimeException exception) {
            String message = SensitiveDataMasker.mask(exception.getMessage());
            repository.fail(requested.executionId(), "EXS-CALL-FAILED", message);
            throw new CpfExternalServiceException("대외기관 호출이 실패했습니다.", exception);
        }
    }

    public ExternalExecution find(String executionId) {
        return requireExecution(executionId);
    }

    public ExternalExecution inquire(String executionId) {
        ExternalExecution execution = requireUnknown(executionId);
        ExternalEndpointPolicy policy = repository.findEndpointPolicy(
                        execution.institutionCode(), execution.endpointCode())
                .orElseThrow(() -> new CpfBusinessException("결과 조회용 endpoint 정책이 없습니다."));
        Map<String, Object> result = endpointPort.inquire(policy, execution.externalRequestId());
        String status = String.valueOf(result.getOrDefault("status", "UNKNOWN")).toUpperCase();
        if ("COMPLETED".equals(status) || "SUCCESS".equals(status)) {
            reconcile(executionId, new ReconcileCommand("COMPLETED", "EXS_RECONCILER", "기관 결과 조회로 성공을 확정했습니다."));
        } else if ("FAILED".equals(status)) {
            reconcile(executionId, new ReconcileCommand("FAILED", "EXS_RECONCILER", "기관 결과 조회로 실패를 확정했습니다."));
        }
        return requireExecution(executionId);
    }

    public ExternalExecution reconcile(String executionId, ReconcileCommand command) {
        ExternalExecution execution = requireUnknown(executionId);
        ExternalExecution.Status target = parseRecoveryStatus(command.outcome());
        if (command.operatorId() == null || command.operatorId().isBlank()) {
            throw new CpfBusinessException("복구 작업자 ID는 필수입니다.");
        }
        if (command.reason() == null || command.reason().isBlank()) {
            throw new CpfBusinessException("복구 감사 사유는 필수입니다.");
        }
        repository.reconcile(executionId, target, command.operatorId(), command.reason());
        reconciliationPort.resolve(execution.unknownResultId(), target.name(), command.operatorId(), command.reason());
        return requireExecution(executionId);
    }

    private ExternalExecution registerUnknown(ExternalExecution requested, ExternalUnknownResultException exception) {
        String failureMessage = SensitiveDataMasker.mask(exception.getMessage());
        CpfUnknownResultRecord unknown = reconciliationPort.register(new CpfUnknownResultRecord(
                null,
                "EXTERNAL_CALL",
                "CHECK_PENDING",
                transactionIdGenerator.generate(),
                null,
                requested.externalRequestId(),
                exception.failureCode(),
                failureMessage,
                "EXTERNAL_RESULT_INQUIRY",
                Instant.now(),
                null));
        repository.markUnknown(
                requested.executionId(), unknown.unknownId(), exception.failureCode(), failureMessage);
        return requireExecution(requested.executionId());
    }

    private ExternalExecution requested(ExecuteCommand command, String requestHash) {
        Instant now = Instant.now();
        return new ExternalExecution(
                "EXE-" + UUID.randomUUID(),
                normalized(command.institutionCode()),
                normalized(command.endpointCode()),
                command.externalRequestId() == null || command.externalRequestId().isBlank()
                        ? "EXT-" + UUID.randomUUID()
                        : command.externalRequestId().trim(),
                command.idempotencyKey().trim(),
                requestHash,
                ExternalExecution.Status.REQUESTED,
                Map.of(),
                null,
                null,
                null,
                now,
                now);
    }

    private ExternalExecution sameRequestOrConflict(ExternalExecution existing, String requestHash) {
        if (!existing.requestHash().equals(requestHash)) {
            throw new CpfBusinessException("같은 멱등 키에 서로 다른 요청 본문을 사용할 수 없습니다.");
        }
        return existing;
    }

    private ExternalExecution requireExecution(String executionId) {
        return repository.findByExecutionId(executionId)
                .orElseThrow(() -> new CpfBusinessException("대외 실행 정보를 찾을 수 없습니다."));
    }

    private ExternalExecution requireUnknown(String executionId) {
        ExternalExecution execution = requireExecution(executionId);
        if (execution.status() != ExternalExecution.Status.UNKNOWN_RESULT) {
            throw new CpfBusinessException("결과 불명 상태의 실행만 복구할 수 있습니다.");
        }
        return execution;
    }

    private ExternalExecution.Status parseRecoveryStatus(String outcome) {
        try {
            ExternalExecution.Status status = ExternalExecution.Status.valueOf(normalized(outcome));
            if (status != ExternalExecution.Status.COMPLETED && status != ExternalExecution.Status.FAILED) {
                throw new IllegalArgumentException();
            }
            return status;
        } catch (RuntimeException exception) {
            throw new CpfBusinessException("복구 결과는 COMPLETED 또는 FAILED여야 합니다.");
        }
    }

    private String requestHash(Map<String, Object> payload) {
        try {
            byte[] canonical = objectMapper.writer()
                    .with(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                    .writeValueAsString(payload == null ? Map.of() : payload)
                    .getBytes(StandardCharsets.UTF_8);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical));
        } catch (JsonProcessingException | NoSuchAlgorithmException exception) {
            throw new IllegalStateException("대외 요청 해시를 생성할 수 없습니다.", exception);
        }
    }

    private String normalized(String value) {
        if (value == null || value.isBlank()) {
            throw new CpfBusinessException("필수 대외 식별자가 비어 있습니다.");
        }
        return value.trim().toUpperCase();
    }

    public record ExecuteCommand(
            String institutionCode,
            String endpointCode,
            String externalRequestId,
            String idempotencyKey,
            Map<String, Object> payload) {
    }

    public record ReconcileCommand(String outcome, String operatorId, String reason) {
    }
}
