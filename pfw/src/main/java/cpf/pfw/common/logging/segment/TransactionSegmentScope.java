package cpf.pfw.common.logging.segment;

/**
 * try-with-resources로 거래 구간의 시작과 종료를 안전하게 묶기 위한 scope입니다.
 */
public final class TransactionSegmentScope implements AutoCloseable {
    private final TransactionSegmentService service;
    private final TransactionSegmentRecord record;
    private final long startNanos;
    private boolean closed;

    TransactionSegmentScope(TransactionSegmentService service, TransactionSegmentRecord record) {
        this.service = service;
        this.record = record;
        this.startNanos = System.nanoTime();
    }

    public TransactionSegmentRecord record() {
        return record;
    }

    public String transactionSegmentId() {
        return record.getTransactionSegmentId();
    }

    public String transactionGlobalId() {
        return record.getTransactionGlobalId();
    }

    public void success() {
        close(TransactionSegmentStatus.SUCCESS, null, null);
    }

    public void fail(String failureCode, String failureMessage) {
        close(TransactionSegmentStatus.FAILED, failureCode, failureMessage);
    }

    @Override
    public void close() {
        success();
    }

    private void close(TransactionSegmentStatus status, String failureCode, String failureMessage) {
        if (closed) {
            return;
        }
        closed = true;
        service.finish(record, status, failureCode, failureMessage, (System.nanoTime() - startNanos) / 1_000_000L);
    }
}
