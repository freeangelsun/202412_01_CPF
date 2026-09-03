package com.cpf.platform.operations.observability.internal.logging.segment;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.platform.operations.observability.spi.logging.segment.TransactionSegmentRecord;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * OUTBOUND 거래 구간이 **호출 대상 operation** 을 기록하는지 검증합니다.
 *
 * <p>컬럼 이름이 {@code target_operation_id} 인데도 호출자 자신의 operation 이 들어가던 결함이 있었다.
 * Batch→Domain 응답유실 검증에서 실제로 {@code BAT_CENTER_CUT_WORK} 가 기록되어, DB 만 보고는
 * "이 구간이 {@code MBR_SAMPLE_TX_CREATE} 를 호출했다" 를 확인할 수 없었다.</p>
 *
 * <p>{@code CpfDomainClientRouter} 가 원격 호출 직전에 {@code withTargetOperation(operationId)} 로
 * Context 를 바인딩하므로 OUTBOUND 는 그 값을 우선해야 한다. INBOUND 는 현재 operation 을 유지한다.</p>
 */
class TransactionSegmentTargetOperationTest {

    private static final String CALLER_OPERATION = "BAT_CENTER_CUT_WORK";
    private static final String TARGET_OPERATION = "MBR_SAMPLE_TX_CREATE";
    private static final String TRANSACTION_ID = "20260903051952448BATMSYS7TD0000001";

    @Test
    void outboundSegmentRecordsTheInvokedTargetOperation() throws Exception {
        assertEquals(TARGET_OPERATION,
                capture(TransactionSegmentRole.EXTERNAL, TransactionSegmentDirection.OUTBOUND)
                        .getTargetOperationId());
    }

    @Test
    void inboundSegmentKeepsTheCurrentOperation() throws Exception {
        assertEquals(CALLER_OPERATION,
                capture(TransactionSegmentRole.MAIN, TransactionSegmentDirection.INBOUND)
                        .getTargetOperationId());
    }

    private TransactionSegmentRecord capture(
            TransactionSegmentRole role, TransactionSegmentDirection direction) throws Exception {
        List<TransactionSegmentRecord> inserted = new ArrayList<>();
        TransactionSegmentService service = new TransactionSegmentService(
                new RecordingPersistenceService(inserted),
                Clock.fixed(Instant.parse("2026-09-03T05:19:52Z"), ZoneOffset.UTC));
        try (AutoCloseable _ = CpfContexts.bind(
                CpfContextSnapshot.capture(callerContext().withTargetOperation(TARGET_OPERATION)))) {
            service.start(role, direction, "BAT", "BAT", "MBR",
                    "/_cpf/domain/MBR/" + TARGET_OPERATION, "Service Call MBR attempt 1");
        }
        assertEquals(1, inserted.size(), "segment must be persisted exactly once");
        return inserted.getFirst();
    }

    private static CpfContext callerContext() {
        Instant now = Instant.parse("2026-09-03T05:19:52Z");
        CpfContext.CpfTransactionContext transaction = new CpfContext.CpfTransactionContext(
                TRANSACTION_ID, TRANSACTION_ID, null, TRANSACTION_ID, TRANSACTION_ID,
                "BAT", "BAT", null, null, null, null, null, null,
                LocalDate.of(2026, 9, 3), now,
                CpfContext.CpfTransactionOriginKind.BATCH, "BAT", null);
        CpfContext.CpfExecutionContext execution = new CpfContext.CpfExecutionContext(
                "exec-1", "exec-1", "exec-1", null, "seg-1", "seg-0",
                CpfContext.CpfExecutionType.BATCH, 1, 1, now, null,
                CpfContext.CpfCancellationMode.DEADLINE_ENFORCED);
        CpfContext.CpfOperationContext operation = new CpfContext.CpfOperationContext(
                CALLER_OPERATION, "BAT Center-Cut Work", "bk-1", "exec-1:bk-1",
                CpfContext.CpfIdempotencyScope.CURRENT_OPERATION,
                CpfContext.CpfIdempotencyMode.REQUIRED, null, null, null, 1L);
        return new CpfContext(transaction, execution, operation, null, null);
    }

    /** DB 없이 저장 시도를 가로채는 테스트 seam 이다. 영속화 자체는 이 계약의 대상이 아니다. */
    private static final class RecordingPersistenceService extends TransactionSegmentPersistenceService {
        private final List<TransactionSegmentRecord> inserted;

        private RecordingPersistenceService(List<TransactionSegmentRecord> inserted) {
            super(null, null);
            this.inserted = inserted;
        }

        @Override
        public void insert(TransactionSegmentRecord record) {
            inserted.add(record);
        }
    }
}
