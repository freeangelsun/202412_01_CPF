package cpf.xyz.edu.service;

import cpf.pfw.common.batch.centercut.CpfCenterCutService;
import cpf.pfw.common.batch.centercut.CpfCenterCutSummary;
import cpf.xyz.edu.centercut.XyzCenterCutConstants;
import cpf.xyz.edu.centercut.XyzCenterCutHandler;
import cpf.xyz.edu.centercut.XyzCenterCutTargetRepository;
import cpf.xyz.edu.dto.XyzCenterCutExecutionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * XYZ 업무 DB 기반 center-cut EDU 흐름을 조립합니다.
 */
@Service
public class XyzCenterCutEducationService {
    private final CpfCenterCutService centerCutService;
    private final XyzCenterCutTargetRepository targetRepository;
    private final XyzCenterCutHandler handler;

    public XyzCenterCutEducationService(
            CpfCenterCutService centerCutService,
            XyzCenterCutTargetRepository targetRepository,
            XyzCenterCutHandler handler) {
        this.centerCutService = centerCutService;
        this.targetRepository = targetRepository;
        this.handler = handler;
    }

    /**
     * 업무 DB 대상 테이블을 읽고, 처리 결과를 업무 DB result 테이블에 기록합니다.
     */
    @Transactional(transactionManager = "xyzTransactionManager")
    public XyzCenterCutExecutionResponse runSample(int limit, boolean resetBeforeRun) {
        if (resetBeforeRun) {
            targetRepository.resetSampleTargetsForSmoke();
        }

        CpfCenterCutSummary summary = centerCutService.execute(
                XyzCenterCutConstants.JOB_ID,
                Math.max(1, limit),
                targetRepository,
                handler);

        return new XyzCenterCutExecutionResponse(
                summary.centerCutJobId(),
                summary.requestedCount(),
                summary.successCount(),
                summary.failedCount(),
                summary.skippedCount(),
                targetRepository.countResultsByStatus(summary.centerCutJobId()),
                targetRepository.findResultSnapshots(summary.centerCutJobId()));
    }
}
