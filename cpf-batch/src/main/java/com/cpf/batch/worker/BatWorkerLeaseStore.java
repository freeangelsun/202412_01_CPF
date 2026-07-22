package cpf.bat.worker;

import java.util.Optional;

/** BAT worker agent가 저장 기술과 분리된 상태로 lease를 다루는 영속 port입니다. */
public interface BatWorkerLeaseStore {
    void register(BatWorkerIdentity identity, BatWorkerProperties properties);

    String heartbeat(BatWorkerIdentity identity, String workerStatus, BatWorkerLease currentLease);

    int recoverExpiredLeases(String requestUser);

    Optional<BatWorkerLease> claim(BatWorkerIdentity identity, BatWorkerProperties properties);

    boolean markRunning(BatWorkerLease lease);

    boolean renew(BatWorkerLease lease, int leaseSeconds);

    boolean complete(BatWorkerLease lease, String executionStatus, Long springBatchExecutionId, String failureMessage);

    void markStopped(BatWorkerIdentity identity, String workerStatus);
}
