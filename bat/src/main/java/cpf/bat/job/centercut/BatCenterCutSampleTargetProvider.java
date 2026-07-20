package cpf.bat.centercut;

import cpf.pfw.common.batch.centercut.CenterCutTargetProvider;
import cpf.pfw.common.batch.centercut.CpfCenterCutResult;
import cpf.pfw.common.batch.centercut.CpfCenterCutStatus;
import cpf.pfw.common.batch.centercut.CpfCenterCutTarget;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BAT center-cut smoke Job에서 사용하는 학습용 대상 provider입니다.
 *
 * <p>운영 업무에서는 이 클래스를 그대로 쓰지 않고 업무 DB의 target/item/result 테이블을 조회하는
 * provider로 교체합니다. 이 provider는 center-cut 실행 흐름과 상태 전이를 smoke 수준에서 검증하기 위한
 * 최소 구현입니다.</p>
 */
@Component
public class BatCenterCutSampleTargetProvider implements CenterCutTargetProvider {
    private static final String SAMPLE_JOB_ID = "CPF_BAT_CENTER_CUT_JOB";

    private final Map<String, CpfCenterCutTarget> targets = new LinkedHashMap<>();
    private final Map<String, CpfCenterCutResult> results = new LinkedHashMap<>();

    public BatCenterCutSampleTargetProvider() {
        reset();
    }

    @Override
    public synchronized List<CpfCenterCutTarget> findReadyTargets(String centerCutJobId, int limit) {
        return targets.values().stream()
                .filter(target -> centerCutJobId.equals(target.centerCutJobId()))
                .filter(target -> target.status() == CpfCenterCutStatus.READY)
                .limit(Math.max(1, limit))
                .toList();
    }

    @Override
    public synchronized void markRunning(CpfCenterCutTarget target, String childTransactionGlobalId) {
        targets.put(
                target.targetId(),
                new CpfCenterCutTarget(
                        target.targetId(),
                        target.centerCutJobId(),
                        target.businessKey(),
                        target.businessDate(),
                        target.payload(),
                        target.parentTransactionGlobalId(),
                        childTransactionGlobalId,
                        target.retryCount(),
                        CpfCenterCutStatus.RUNNING));
    }

    @Override
    public synchronized void markResult(CpfCenterCutTarget target, CpfCenterCutResult result) {
        targets.put(
                target.targetId(),
                new CpfCenterCutTarget(
                        target.targetId(),
                        target.centerCutJobId(),
                        target.businessKey(),
                        target.businessDate(),
                        target.payload(),
                        target.parentTransactionGlobalId(),
                        result.childTransactionGlobalId(),
                        target.retryCount(),
                        result.status()));
        results.put(target.targetId(), result);
    }

    public synchronized Map<String, Object> snapshot() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("targets", new ArrayList<>(targets.values()));
        snapshot.put("results", new ArrayList<>(results.values()));
        return snapshot;
    }

    public synchronized void reset() {
        targets.clear();
        results.clear();
        targets.put("CENTER-CUT-001", sampleTarget("CENTER-CUT-001", "ORDER-20260701-001"));
        targets.put("CENTER-CUT-002", sampleTarget("CENTER-CUT-002", "ORDER-20260701-002"));
        targets.put("CENTER-CUT-003", sampleTarget("CENTER-CUT-003", "ORDER-20260701-003"));
    }

    private CpfCenterCutTarget sampleTarget(String targetId, String businessKey) {
        return new CpfCenterCutTarget(
                targetId,
                SAMPLE_JOB_ID,
                businessKey,
                LocalDate.of(2026, 7, 1),
                "{\"businessKey\":\"" + businessKey + "\"}",
                "20260701110000000BATparent0000001",
                null,
                0,
                CpfCenterCutStatus.READY);
    }
}
