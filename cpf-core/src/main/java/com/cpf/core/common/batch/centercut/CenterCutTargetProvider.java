package com.cpf.core.common.batch.centercut;

import java.util.List;

/**
 * center-cut 처리 대상을 조회하고 상태를 반영하는 업무 adapter 계약입니다.
 */
public interface CenterCutTargetProvider {

    List<CpfCenterCutTarget> findReadyTargets(String centerCutJobId, int limit);

    /**
     * 서비스가 확정한 transactionId/segment 계층을 포함한 RUNNING 대상을 저장합니다.
     */
    void markRunning(CpfCenterCutTarget target);

    void markResult(CpfCenterCutTarget target, CpfCenterCutResult result);
}
