package com.cpf.foundation.execution;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;
import com.cpf.foundation.time.spi.CpfBusinessDateProvider;
import com.cpf.foundation.tracking.CpfSubjectCollector;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * Core Context 값에 ID/업무일/시각을 주입해 root/child 실행을 생성하는 Foundation 서비스입니다.
 *
 * <p>Core 값 객체는 현재 시각과 신규 ID를 생성하지 않습니다. 이 클래스도 Registry/Component 저장소가
 * 아니며, 호출자가 이미 검증한 Boundary 의미만 받아 immutable {@link CpfContext}를 생성합니다.</p>
 */
public final class CpfContextExecutionFactory {
    private final CpfTransactionIdGenerator transactionIds;
    private final CpfExecutionIdGenerator executionIds;
    private final CpfBusinessDateProvider businessDates;
    private final Clock clock;
    private final CpfSubjectCollector subjectCollector;

    public CpfContextExecutionFactory(
            CpfTransactionIdGenerator transactionIds,
            CpfExecutionIdGenerator executionIds,
            CpfBusinessDateProvider businessDates,
            Clock clock) {
        this(transactionIds, executionIds, businessDates, clock, null);
    }

    public CpfContextExecutionFactory(
            CpfTransactionIdGenerator transactionIds,
            CpfExecutionIdGenerator executionIds,
            CpfBusinessDateProvider businessDates,
            Clock clock,
            CpfSubjectCollector subjectCollector) {
        this.transactionIds = Objects.requireNonNull(transactionIds, "transactionIds");
        this.executionIds = Objects.requireNonNull(executionIds, "executionIds");
        this.businessDates = Objects.requireNonNull(businessDates, "businessDates");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.subjectCollector = subjectCollector;
    }

    /** 기존 Boundary/테스트에서 사용하던 3-provider 생성 패턴의 Foundation 호환 생성자입니다. */
    public CpfContextExecutionFactory(
            CpfTransactionIdGenerator transactionIds,
            CpfExecutionIdGenerator executionIds,
            CpfBusinessDateProvider businessDates) {
        this(transactionIds, executionIds, businessDates, Clock.systemUTC());
    }

    /** 이미 존재하는 거래에서 child 실행만 생성하는 경량 생성자입니다. */
    public CpfContextExecutionFactory(CpfExecutionIdGenerator executionIds, Clock clock) {
        this(() -> { throw new IllegalStateException("root transaction id generator is not configured"); },
                executionIds,
                () -> LocalDate.now(clock),
                clock);
    }

    /** Root 생성 요청. HTTP/Broker 등 원문 Runtime 객체를 담지 않습니다. */
    public record RootSpec(
            String correlationId,
            String standardExecutionId,
            CpfContext.CpfExecutionType executionType,
            CpfContext.CpfTransactionOriginKind originKind,
            String originSystemId,
            String originTransactionId,
            CpfContext.CpfOperationContext operation,
            CpfContext.CpfIdentityContext identity,
            CpfContext.CpfTenantContext tenant,
            Instant deadline) { }

    /** Child 생성 요청. 부모 transactionId는 절대 교체하지 않습니다. */
    public record ChildSpec(
            String standardExecutionId,
            CpfContext.CpfExecutionType executionType,
            int attempt,
            Instant deadline,
            CpfContext.CpfOperationContext operation) { }

    public CpfContext newRoot(RootSpec spec) {
        return newRoot(spec, businessDates.currentBusinessDate());
    }

    public CpfContext newRoot(RootSpec spec, LocalDate businessDate) {
        Objects.requireNonNull(spec, "spec");
        String transactionId = requiredId("transactionId", transactionIds.newTransactionId());
        return newRootWithTransactionId(transactionId, spec, Objects.requireNonNull(businessDate, "businessDate"));
    }

    /** 간단한 내부 Root 생성 Golden Path입니다. */
    public CpfContext newRoot(
            String correlationId,
            String standardExecutionId,
            CpfContext.CpfIdentityContext identity,
            CpfContext.CpfTenantContext tenant,
            Instant deadline) {
        return newRoot(new RootSpec(
                correlationId,
                standardExecutionId,
                CpfContext.CpfExecutionType.INTERNAL,
                CpfContext.CpfTransactionOriginKind.INTERNAL,
                null,
                null,
                null,
                identity,
                tenant,
                deadline));
    }

    private CpfContext newRootWithTransactionId(String transactionId, RootSpec spec, LocalDate businessDate) {
        Instant now = clock.instant();
        String executionId = requiredId("executionId", executionIds.newExecutionId());
        String segmentId = requiredId("segmentId", executionIds.newSegmentId());
        CpfContext.CpfTransactionContext transaction = new CpfContext.CpfTransactionContext(
                transactionId,
                transactionId,
                null,
                spec.correlationId(),
                null,
                null,
                spec.originSystemId(),
                businessDate,
                now,
                spec.originKind() == null ? CpfContext.CpfTransactionOriginKind.INTERNAL : spec.originKind(),
                spec.originSystemId(),
                spec.originTransactionId());
        CpfContext.CpfExecutionContext execution = new CpfContext.CpfExecutionContext(
                spec.standardExecutionId(),
                executionId,
                executionId,
                null,
                segmentId,
                null,
                spec.executionType() == null ? CpfContext.CpfExecutionType.INTERNAL : spec.executionType(),
                1,
                0,
                now,
                spec.deadline(),
                CpfContext.CpfCancellationMode.DEADLINE_ENFORCED);
        CpfContext context = new CpfContext(transaction, execution, spec.operation(), spec.identity(), spec.tenant());
        if (subjectCollector != null) subjectCollector.collect(context);
        return context;
    }

    /** 부모 transaction/rootTransaction/correlation 의미를 유지하고 execution/segment만 새로 발급합니다. */
    public CpfContext child(CpfContext parent, ChildSpec spec) {
        Objects.requireNonNull(parent, "parent");
        Objects.requireNonNull(spec, "spec");
        Instant now = clock.instant();
        CpfContext.CpfExecutionContext child = parent.execution().child(
                spec.standardExecutionId(),
                requiredId("executionId", executionIds.newExecutionId()),
                requiredId("segmentId", executionIds.newSegmentId()),
                spec.executionType() == null ? CpfContext.CpfExecutionType.INTERNAL : spec.executionType(),
                Math.max(1, spec.attempt()),
                now,
                spec.deadline());
        return parent.child(child, spec.operation());
    }

    public CpfContextSnapshot childSnapshot(CpfContextSnapshot parent, ChildSpec spec) {
        Objects.requireNonNull(parent, "parent");
        return CpfContextSnapshot.capture(child(parent.context(), spec), clock.instant());
    }

    /**
     * 서명/신뢰 경계에서 이미 검증한 transactionId를 수용해 새 로컬 execution을 만듭니다.
     * Retry/Reconcile에서도 transactionId를 새로 발급하지 않습니다.
     */
    public CpfContext fromTrustedPropagation(
            String transactionId,
            String rootTransactionId,
            String correlationId,
            LocalDate businessDate,
            Instant sourceStartedAt,
            CpfContext.CpfTransactionOriginKind originKind,
            String originSystemId,
            String originTransactionId,
            String standardExecutionId,
            String parentExecutionId,
            String rootExecutionId,
            String parentSegmentId,
            CpfContext.CpfExecutionType executionType,
            int attempt,
            int callDepth,
            CpfContext.CpfOperationContext operation,
            CpfContext.CpfIdentityContext identity,
            CpfContext.CpfTenantContext tenant,
            Instant deadline) {
        String tx = requiredId("transactionId", transactionId);
        Instant now = clock.instant();
        String executionId = requiredId("executionId", executionIds.newExecutionId());
        String segmentId = requiredId("segmentId", executionIds.newSegmentId());
        String rootExec = normalize(rootExecutionId);
        if (rootExec == null) rootExec = executionId;
        CpfContext.CpfTransactionContext transaction = new CpfContext.CpfTransactionContext(
                tx,
                normalize(rootTransactionId) == null ? tx : rootTransactionId,
                null,
                correlationId,
                null,
                null,
                originSystemId,
                Objects.requireNonNull(businessDate, "businessDate"),
                sourceStartedAt == null ? now : sourceStartedAt,
                originKind == null ? CpfContext.CpfTransactionOriginKind.INTEGRATION : originKind,
                originSystemId,
                originTransactionId);
        CpfContext.CpfExecutionContext execution = new CpfContext.CpfExecutionContext(
                standardExecutionId,
                executionId,
                rootExec,
                parentExecutionId,
                segmentId,
                parentSegmentId,
                executionType == null ? CpfContext.CpfExecutionType.INTEGRATION : executionType,
                Math.max(1, attempt),
                Math.max(0, callDepth),
                now,
                deadline,
                CpfContext.CpfCancellationMode.DEADLINE_ENFORCED);
        CpfContext context = new CpfContext(transaction, execution, operation, identity, tenant);
        if (subjectCollector != null) subjectCollector.collect(context);
        return context;
    }

    private static String requiredId(String name, String value) {
        String normalized = normalize(value);
        if (normalized == null) throw new IllegalStateException(name + " generator returned blank value");
        return normalized;
    }

    private static String normalize(String value) {
        if (value == null) return null;
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }
}
