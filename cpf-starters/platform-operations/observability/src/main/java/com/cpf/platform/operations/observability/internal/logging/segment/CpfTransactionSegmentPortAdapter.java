package com.cpf.platform.operations.observability.internal.logging.segment;

import com.cpf.platform.operations.observability.spi.logging.segment.TransactionSegmentRecord;
import com.cpf.platform.operations.observability.api.logging.CpfTransactionSegmentPort;
import java.util.Objects;
import org.springframework.stereotype.Component;

/** Observability 내부 Segment Runtime을 공개 Port에 연결합니다. */
@Component
public final class CpfTransactionSegmentPortAdapter implements CpfTransactionSegmentPort {
    private final TransactionSegmentService delegate;

    public CpfTransactionSegmentPortAdapter(TransactionSegmentService delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public SegmentScope start(Role role, Direction direction, String moduleCode, String sourceModuleCode,
            String targetModuleCode, String apiPath, String transactionName) {
        TransactionSegmentScope scope = delegate.start(
                TransactionSegmentRole.valueOf(role.name()),
                TransactionSegmentDirection.valueOf(direction.name()),
                moduleCode, sourceModuleCode, targetModuleCode, apiPath, transactionName);
        return new SegmentScope() {
            private boolean closed;
            @Override public String transactionSegmentId() { return scope.transactionSegmentId(); }
            @Override public String transactionId() { return scope.transactionId(); }
            @Override public void update(SegmentAttributes a) {
                if (a == null || closed) return;
                TransactionSegmentRecord r = scope.record();
                r.setSelectedInstanceId(a.selectedInstanceId());
                if (a.attemptNo() != null) r.setAttemptNo(a.attemptNo());
                if (a.retry() != null) r.setRetryYn(a.retry() ? "Y" : "N");
                if (a.failover() != null) r.setFailoverYn(a.failover() ? "Y" : "N");
                r.setCircuitState(a.circuitState());
                r.setDownstreamHttpStatus(a.downstreamStatus());
                r.setResultState(a.resultState());
                r.setUnknownResultId(a.unknownResultId());
            }
            @Override public void success() { if (!closed) { closed = true; scope.success(); } }
            @Override public void fail(String code, String message) { if (!closed) { closed = true; scope.fail(code, message); } }
        };
    }
}
