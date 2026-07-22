package com.cpf.reference.centercut.application;

import com.cpf.core.common.batch.centercut.CpfCenterCutService;
import com.cpf.core.common.batch.centercut.CpfCenterCutSummary;
import com.cpf.reference.centercut.ReferenceCenterCutConstants;
import com.cpf.reference.centercut.ReferenceCenterCutHandler;
import com.cpf.reference.centercut.ReferenceCenterCutTargetRepository;
import com.cpf.reference.centercut.dto.ReferenceCenterCutExecutionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * REF 업무 DB 기반 center-cut EDU 흐름을 조립합니다.
 */
@Service
public class ReferenceCenterCutEducationService extends com.cpf.reference.common.base.ReferenceBaseService {
    private final CpfCenterCutService centerCutService;
    private final ReferenceCenterCutTargetRepository targetRepository;
    private final ReferenceCenterCutHandler handler;

    public ReferenceCenterCutEducationService(
            CpfCenterCutService centerCutService,
            ReferenceCenterCutTargetRepository targetRepository,
            ReferenceCenterCutHandler handler) {
        this.centerCutService = centerCutService;
        this.targetRepository = targetRepository;
        this.handler = handler;
    }

    /**
     * 업무 DB 대상 테이블을 읽고, 처리 결과를 업무 DB result 테이블에 기록합니다.
     */
    @Transactional(transactionManager = "refTransactionManager")
    public ReferenceCenterCutExecutionResponse runSample(int limit, boolean resetBeforeRun) {
        if (resetBeforeRun) {
            targetRepository.resetSampleTargetsForSmoke();
        }

        CpfCenterCutSummary summary = centerCutService.execute(
                ReferenceCenterCutConstants.JOB_ID,
                Math.max(1, limit),
                targetRepository,
                handler);

        return new ReferenceCenterCutExecutionResponse(
                summary.centerCutJobId(),
                summary.requestedCount(),
                summary.successCount(),
                summary.failedCount(),
                summary.skippedCount(),
                targetRepository.countResultsByStatus(summary.centerCutJobId()),
                targetRepository.findResultSnapshots(summary.centerCutJobId()));
    }
}
