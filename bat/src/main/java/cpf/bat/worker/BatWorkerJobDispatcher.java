package cpf.bat.worker;

/** claim된 배치 실행을 실제 Spring Batch Job에 전달하는 application port입니다. */
public interface BatWorkerJobDispatcher {
    DispatchResult dispatch(BatWorkerLease lease);

    record DispatchResult(String status, Long springBatchExecutionId, String failureMessage) {
        public static DispatchResult completed(Long executionId) {
            return new DispatchResult("COMPLETED", executionId, null);
        }

        public static DispatchResult failed(Long executionId, String failureMessage) {
            return new DispatchResult("FAILED", executionId, failureMessage);
        }
    }
}
