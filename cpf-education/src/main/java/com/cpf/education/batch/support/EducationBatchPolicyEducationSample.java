package com.cpf.education.batch.support;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * BAT Runtime 구현을 복제하지 않고 운영 계약의 핵심 정책을 설명하는 EDU 교육 샘플입니다.
 *
 * <p>실제 실행·조회 명령은 {@code CpfBatchOperationsPort}로 BAT Owner에 위임합니다.
 * 이 클래스는 Job 개발자가 알아야 할 lock, restart, schedule, transaction 및
 * 결과불명 대사 원칙만 불변 값으로 노출합니다.</p>
 */
@Component
public class EducationBatchPolicyEducationSample {

    public LockPolicy lockPolicy() {
        return new LockPolicy(
                "cpf-batch",
                "동일 Job/업무일자 실행은 BAT Owner의 lease와 fencing token으로 직렬화합니다.",
                "lease 상실 이후의 stale worker 쓰기는 fencing token 검증으로 거부합니다.",
                "강제 해제는 ADM 승인·사유·감사 로그를 거쳐 CpfBatchOperationsPort로 요청합니다.");
    }

    /** restartPolicy 작업을 CPF 표준 계약에 따라 수행한다. */
    public RestartPolicy restartPolicy() {
        return new RestartPolicy(
                List.of("FAILED", "STOPPED", "UNKNOWN_RESULT"),
                "restart는 동일 JobInstance와 파라미터를 유지하고 마지막 commit checkpoint 다음부터 처리합니다.",
                "rerun은 새 JobInstance를 생성하므로 restart와 같은 운영 버튼으로 합치지 않습니다.",
                "UNKNOWN_RESULT는 성공이나 실패로 추정하지 않고 외부 결과를 대사한 뒤 재처리합니다.");
    }

    /** schedulePolicy 작업을 CPF 표준 계약에 따라 수행한다. */
    public SchedulePolicy schedulePolicy() {
        return new SchedulePolicy(
                "cpf-batch-scheduler",
                "단일 leader lease와 fencing token을 획득한 Scheduler만 실행 요청을 생성합니다.",
                "scheduleId와 예정시각의 멱등키로 중복 trigger를 거부합니다.",
                "업무일·휴일 계산은 CMN Calendar 계약을 사용하고 강제 실행은 감사 사유를 남깁니다.");
    }

    /** lifecyclePolicy 작업을 CPF 표준 계약에 따라 수행한다. */
    public LifecyclePolicy lifecyclePolicy() {
        return new LifecyclePolicy(
                "Chunk commit 이전 실패는 해당 Chunk를 rollback하고, 이미 commit된 이전 Chunk는 보존합니다.",
                "Item validation 실패의 skip/retry 여부는 Job 정책으로 명시하고 실패 데이터와 사유를 남깁니다.",
                "jobId, businessDate 및 정규화된 parameter로 중복 실행 키를 구성합니다.",
                "Timeout·연결 단절처럼 결과를 확정할 수 없는 호출은 UNKNOWN_RESULT와 대사 후보로 분리합니다.",
                "FAILED와 UNKNOWN_RESULT 후보를 중복 제거해 대사하고 확인된 상태만 restart 또는 보상 처리합니다.");
    }

    /** LockPolicy 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public record LockPolicy(
            String owner,
            String acquisition,
            String fencing,
            String forcedRelease) {
    }

    public record RestartPolicy(
            List<String> retryableStates,
            String restart,
            String rerun,
            String unknownResult) {
        public RestartPolicy {
            retryableStates = List.copyOf(retryableStates);
        }
    }

    /** SchedulePolicy 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public record SchedulePolicy(
            String owner,
            String leadership,
            String duplicatePrevention,
            String calendar) {
    }

    public record LifecyclePolicy(
            String transactionBoundary,
            String itemFailure,
            String duplicateExecution,
            String unknownResult,
            String reconciliation) {
    }
}
