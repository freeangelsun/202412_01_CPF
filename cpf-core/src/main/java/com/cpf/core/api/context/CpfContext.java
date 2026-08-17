package com.cpf.core.api.context;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * CPF 전체 Capability가 공유하는 기술중립 실행 Context 값입니다.
 *
 * <p>HTTP Header, 인증 Token, Broker 객체, DB Connection, MDC/Trace Runtime 같은
 * 전송·Provider 객체는 포함하지 않습니다. 시간과 식별자는 Foundation/Boundary Owner가
 * 결정한 값을 전달하며 Core 값 객체가 현재 시각이나 신규 ID를 생성하지 않습니다.</p>
 */
public record CpfContext(
        CpfTransactionContext transaction,
        CpfExecutionContext execution,
        CpfOperationContext operation,
        CpfIdentityContext identity,
        CpfTenantContext tenant) {

    public static final int CURRENT_SCHEMA_VERSION = 1;

    public CpfContext {
        Objects.requireNonNull(transaction, "transaction");
        Objects.requireNonNull(execution, "execution");
    }

    /** 이전 schema-aware Consumer를 위한 값 호환 생성자입니다. Runtime wiring은 제공하지 않습니다. */
    public CpfContext(int contextSchemaVersion, CpfTransactionContext transaction, CpfExecutionContext execution,
                      CpfOperationContext operation, CpfIdentityContext identity, CpfTenantContext tenant) {
        this(transaction, execution, operation, identity, tenant);
        if (contextSchemaVersion < 1) throw new IllegalArgumentException("contextSchemaVersion");
    }

    public int contextSchemaVersion() { return CURRENT_SCHEMA_VERSION; }
    /** transactionId 작업을 CPF 표준 계약에 따라 수행한다. */
    public String transactionId() { return transaction.transactionId(); }
    public String traceId() { return transaction.traceId(); }
    public String executionId() { return execution.executionId(); }
    public String segmentId() { return execution.segmentId(); }
    public LocalDate businessDate() { return transaction.businessDate(); }
    public String tenantId() { return tenant == null ? null : tenant.tenantId(); }
    public String subjectId() { return identity == null ? null : identity.subjectId(); }
    /** actorId 작업을 CPF 표준 계약에 따라 수행한다. */
    public String actorId() { return identity == null ? null : identity.actorId(); }
    /** Legacy business channel metadata. It is not a canonical CPF system-routing value. */
    public String channelCode() { return transaction.channelCode(); }
    public String originalSystemCode() { return transaction.originalSystemCode(); }
    public String systemCode() { return transaction.systemCode(); }
    public String callerSystemCode() { return transaction.callerSystemCode(); }
    public String targetSystemCode() { return transaction.targetSystemCode(); }
    /** 현재 논리 거래가 실행 중인 Canonical operationId를 반환합니다. */
    public String operationId() { return operation == null ? null : operation.operationId(); }
    /** 현재 호출 대상의 Canonical operationId를 반환합니다. */
    public String targetOperationId() { return operationId(); }
    /** 현재 거래의 Idempotency Key를 반환합니다. */
    public String idempotencyKey() { return operation == null ? null : operation.idempotencyKey(); }
    /** 비동기·외부 연계의 상관관계 식별자를 반환합니다. */
    public String correlationId() { return transaction.correlationId(); }

    /**
     * Same-JVM Domain hop에서 wire Header 없이도 Canonical caller/target/operation 의미를 유지합니다.
     * 새 실행 ID 생성은 Foundation owner가 담당하므로 이 메서드는 기존 execution을 변경하지 않습니다.
     */
    public CpfContext localDomainHop(String targetSystemCode, String targetOperationId) {
        String target = required("targetSystemCode", targetSystemCode, 128);
        String operationId = required("targetOperationId", targetOperationId, 160);
        String caller = firstNonBlank(transaction.systemCode(), transaction.originSystemId());
        CpfTransactionContext nextTransaction = new CpfTransactionContext(
                transaction.transactionId(), transaction.rootTransactionId(), transaction.parentTransactionId(),
                transaction.correlationId(), transaction.traceId(), transaction.channelCode(), caller,
                transaction.businessDate(), transaction.startedAt(), transaction.originKind(),
                transaction.originSystemId(), transaction.originTransactionId(), target, target);
        CpfOperationContext previous = operation;
        CpfOperationContext nextOperation = new CpfOperationContext(
                operationId, previous == null ? operationId : previous.operationName(),
                previous == null ? null : previous.commandId(), previous == null ? null : previous.idempotencyKey(),
                previous == null ? CpfIdempotencyScope.CURRENT_OPERATION : previous.idempotencyScope(),
                previous == null ? CpfIdempotencyMode.NONE : previous.idempotencyMode(),
                previous == null ? null : previous.payloadFingerprint(),
                previous == null ? null : previous.operationId(),
                previous == null ? 1L : previous.transactionSequence() + 1L);
        return new CpfContext(nextTransaction, execution, nextOperation, identity, tenant);
    }

    /** Handler/OpenAPI에서 resolve된 실제 Operation ID를 현재 Context에 반영합니다. */
    public CpfContext withResolvedOperation(String resolvedOperationId, String operationName) {
        String resolved = required("operationId", resolvedOperationId, 160);
        CpfOperationContext previous = operation;
        CpfOperationContext next = new CpfOperationContext(
                resolved, operationName == null || operationName.isBlank() ? resolved : operationName,
                previous == null ? null : previous.commandId(), previous == null ? null : previous.idempotencyKey(),
                previous == null ? CpfIdempotencyScope.CURRENT_OPERATION : previous.idempotencyScope(),
                previous == null ? CpfIdempotencyMode.NONE : previous.idempotencyMode(),
                previous == null ? null : previous.payloadFingerprint(),
                previous == null || previous.operationId() == null || previous.operationId().equals(resolved)
                        ? null : previous.operationId(),
                previous == null ? 1L : previous.transactionSequence());
        return new CpfContext(transaction, execution, next, identity, tenant);
    }

    /** 동일 거래 의미를 유지하며 실행 단위만 자식 실행으로 변경합니다. */
    public CpfContext child(CpfExecutionContext child, CpfOperationContext childOperation) {
        return new CpfContext(transaction, Objects.requireNonNull(child),
                childOperation == null ? operation : childOperation, identity, tenant);
    }

    /** 인증/테넌트 Boundary가 검증한 의미만 교체합니다. */
    public CpfContext withIdentityAndTenant(CpfIdentityContext nextIdentity, CpfTenantContext nextTenant) {
        return new CpfContext(transaction, execution, operation, nextIdentity, nextTenant);
    }

    /** 시스템 전체 거래·추적·호출 원천·업무일자 의미입니다. */
    public record CpfTransactionContext(
            String transactionId,
            String rootTransactionId,
            String parentTransactionId,
            String correlationId,
            String traceId,
            String channelCode,
            String callerSystemCode,
            LocalDate businessDate,
            Instant startedAt,
            CpfTransactionOriginKind originKind,
            String originSystemId,
            String originTransactionId,
            String systemCode,
            String targetSystemCode) {
        public CpfTransactionContext {
            transactionId = required("transactionId", transactionId, 160);
            rootTransactionId = required("rootTransactionId", rootTransactionId, 160);
            parentTransactionId = optional(parentTransactionId, 160);
            correlationId = optional(correlationId, 160);
            traceId = optional(traceId, 64);
            channelCode = optional(channelCode, 64);
            callerSystemCode = optional(callerSystemCode, 128);
            Objects.requireNonNull(businessDate, "businessDate");
            Objects.requireNonNull(startedAt, "startedAt");
            if (originKind == null) originKind = CpfTransactionOriginKind.INTERNAL;
            originSystemId = optional(originSystemId, 160);
            originTransactionId = optional(originTransactionId, 160);
            systemCode = optional(systemCode, 128);
            targetSystemCode = optional(targetSystemCode, 128);
        }

        /** transactionId를 최초 생성한 원본 System Code를 반환합니다. */
        public String originalSystemCode() { return originSystemId; }

        /** 이전 full-field Consumer를 위한 호환 생성자입니다. */
        public CpfTransactionContext(
                String transactionId, String rootTransactionId, String parentTransactionId, String correlationId,
                String traceId, String channelCode, String callerSystemCode, LocalDate businessDate, Instant startedAt,
                CpfTransactionOriginKind originKind, String originSystemId, String originTransactionId) {
            this(transactionId, rootTransactionId, parentTransactionId, correlationId, traceId, channelCode,
                    callerSystemCode, businessDate, startedAt, originKind, originSystemId, originTransactionId,
                    originSystemId, originSystemId);
        }

        /** 이전 9필드 Consumer가 이미 결정한 값만 전달하는 호환 생성자입니다. */
        public CpfTransactionContext(
                String transactionId, String rootTransactionId, String parentTransactionId, String correlationId,
                LocalDate businessDate, Instant startedAt, CpfTransactionOriginKind originKind,
                String originSystemId, String originTransactionId) {
            this(transactionId, rootTransactionId, parentTransactionId, correlationId,
                    null, null, originSystemId, businessDate, Objects.requireNonNull(startedAt, "startedAt"), originKind,
                    originSystemId, originTransactionId, originSystemId, originSystemId);
        }
    }

    /** 현재 실행 구간의 부모관계·재시도·deadline 의미입니다. */
    public record CpfExecutionContext(
            String standardExecutionId, String executionId, String rootExecutionId, String parentExecutionId,
            String segmentId, String parentSegmentId, CpfExecutionType executionType, int attempt, int callDepth,
            Instant startedAt, Instant deadline, CpfCancellationMode cancellationMode) {
        public CpfExecutionContext {
            standardExecutionId = optional(standardExecutionId, 160);
            executionId = required("executionId", executionId, 160);
            rootExecutionId = required("rootExecutionId", rootExecutionId, 160);
            parentExecutionId = optional(parentExecutionId, 160);
            segmentId = required("segmentId", segmentId, 160);
            parentSegmentId = optional(parentSegmentId, 160);
            if (executionType == null) executionType = CpfExecutionType.INTERNAL;
            if (attempt < 1) throw new IllegalArgumentException("attempt");
            if (callDepth < 0) throw new IllegalArgumentException("callDepth");
            Objects.requireNonNull(startedAt, "startedAt");
            if (cancellationMode == null) cancellationMode = CpfCancellationMode.DEADLINE_ENFORCED;
            if (deadline != null && deadline.isBefore(startedAt)) throw new IllegalArgumentException("deadline before startedAt");
        }

        /** Foundation이 결정한 ID와 시각으로 자식 실행을 만듭니다. */
        public CpfExecutionContext child(String standardId, String childExecutionId, String childSegmentId,
                                         CpfExecutionType type, int childAttempt, Instant now, Instant childDeadline) {
            return new CpfExecutionContext(standardId, childExecutionId, rootExecutionId, executionId,
                    childSegmentId, segmentId, type, Math.max(1, childAttempt), callDepth + 1,
                    Objects.requireNonNull(now, "now"), childDeadline, cancellationMode);
        }
    }

    /** 하나의 명령/업무 Operation의 멱등성·추적 의미입니다. */
    public record CpfOperationContext(
            String operationId, String operationName, String commandId, String idempotencyKey,
            CpfIdempotencyScope idempotencyScope, CpfIdempotencyMode idempotencyMode,
            String payloadFingerprint, String parentOperationId, long transactionSequence) {
        public CpfOperationContext {
            operationId = optional(operationId, 160);
            operationName = optional(operationName, 160);
            commandId = optional(commandId, 160);
            idempotencyKey = optional(idempotencyKey, 256);
            if (idempotencyScope == null) idempotencyScope = CpfIdempotencyScope.CURRENT_OPERATION;
            if (idempotencyMode == null) idempotencyMode = CpfIdempotencyMode.NONE;
            payloadFingerprint = optional(payloadFingerprint, 256);
            parentOperationId = optional(parentOperationId, 160);
            if (transactionSequence < 1) transactionSequence = 1L;
            if (idempotencyMode == CpfIdempotencyMode.REQUIRED && idempotencyKey == null) {
                throw new IllegalArgumentException("required idempotencyKey missing");
            }
        }

        /** 이전 8필드 Consumer는 첫 Operation sequence=1로 승계합니다. */
        public CpfOperationContext(
                String operationId, String operationName, String commandId, String idempotencyKey,
                CpfIdempotencyScope idempotencyScope, CpfIdempotencyMode idempotencyMode,
                String payloadFingerprint, String parentOperationId) {
            this(operationId, operationName, commandId, idempotencyKey, idempotencyScope, idempotencyMode,
                    payloadFingerprint, parentOperationId, 1L);
        }
    }

    /** 인증된 주체의 최소 신뢰 의미입니다. Raw credential/role 목록은 포함하지 않습니다. */
    public record CpfIdentityContext(
            String subjectId, String actorId, CpfPrincipalType principalType, String authenticationContextId,
            String delegationId, String assuranceLevel, Instant authenticatedAt) {
        public CpfIdentityContext {
            subjectId = optional(subjectId, 160);
            actorId = optional(actorId, 160);
            principalType = Objects.requireNonNull(principalType, "principalType");
            authenticationContextId = optional(authenticationContextId, 160);
            delegationId = optional(delegationId, 160);
            assuranceLevel = optional(assuranceLevel, 64);
            if (principalType != CpfPrincipalType.ANONYMOUS && subjectId == null) {
                throw new IllegalArgumentException("subjectId is required");
            }
        }
        /** CpfIdentityContext 작업을 CPF 표준 계약에 따라 수행한다. */
        public CpfIdentityContext(String subjectId, String actorId, CpfPrincipalType principalType) {
            this(subjectId, actorId, principalType, null, null, null, null);
        }
    }

    /** 멀티테넌시 식별 의미입니다. */
    public record CpfTenantContext(String tenantId, String tenantRealm) {
        public CpfTenantContext {
            tenantId = optional(tenantId, 128);
            tenantRealm = optional(tenantRealm, 96);
            if (tenantRealm != null && tenantId == null) throw new IllegalArgumentException("tenantRealm requires tenantId");
        }
        public CpfTenantContext(String tenantId) { this(tenantId, null); }
    }

    /** CpfCancellationMode 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public enum CpfCancellationMode { DEADLINE_ENFORCED, COOPERATIVE, NON_CANCELLABLE }
    public enum CpfExecutionType { API, BATCH, MESSAGE, SCHEDULED, ASYNC, INTEGRATION, INTERNAL }
    public enum CpfIdempotencyMode { NONE, OPTIONAL, REQUIRED }
    public enum CpfIdempotencyScope { CURRENT_OPERATION, TRANSACTION, BUSINESS_KEY }
    public enum CpfPrincipalType { ANONYMOUS, USER, SYSTEM, SERVICE, OPERATOR }
    public enum CpfTransactionOriginKind { HTTP, MESSAGE, BATCH, SCHEDULE, INTERNAL, INTEGRATION, RECOVERY }

    private static String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private static String required(String name, String value, int max) {
        String normalized = optional(value, max);
        if (normalized == null) throw new IllegalArgumentException(name);
        return normalized;
    }

    private static String optional(String value, int max) {
        if (value == null) return null;
        String normalized = value.trim();
        if (normalized.isEmpty()) return null;
        if (normalized.length() > max) throw new IllegalArgumentException("context value too long");
        if (normalized.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("context value contains control character");
        }
        return normalized;
    }
}
