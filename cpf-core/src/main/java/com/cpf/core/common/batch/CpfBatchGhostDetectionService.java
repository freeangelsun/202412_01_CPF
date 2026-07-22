package com.cpf.core.common.batch;

/**
 * heartbeat timeout 기준으로 RUNNING 배치의 ghost 후보를 자동 감지하는 공통 서비스입니다.
 *
 * <p>이 서비스는 실행 중인 Job을 직접 중지하거나 lock을 해제하지 않습니다. 후보를 감지해
 * cpf_batch_ghost_event에 기록하고, 실제 조치는 ADM의 권한검사와 감사 사유를 거쳐 수행합니다.</p>
 */
public class CpfBatchGhostDetectionService {
    private final CpfBatchOperationRepository repository;
    private final CpfBatchHeartbeatService heartbeatService;

    public CpfBatchGhostDetectionService(
            CpfBatchOperationRepository repository,
            CpfBatchHeartbeatService heartbeatService) {
        this.repository = repository;
        this.heartbeatService = heartbeatService;
    }

    public int detectGhostCandidates() {
        return detectGhostCandidates(heartbeatService.heartbeatTimeoutSeconds());
    }

    public int detectGhostCandidates(int heartbeatTimeoutSeconds) {
        return repository.detectGhostCandidates(heartbeatTimeoutSeconds, "CPF_GHOST_DETECTOR");
    }
}
