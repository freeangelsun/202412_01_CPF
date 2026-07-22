package cpf.bat.edu.ondemand;

import java.util.List;
import java.util.Map;

/**
 * 운영 API를 직접 복제하지 않고 {@link BatOnDemandService}를 통해 온디맨드 배치를 사용하는 교육 샘플입니다.
 *
 * <p>신규 실행 접수는 멱등키를 가진 {@link BatOnDemandRequest}를 전달합니다. 실패한 동일
 * JobInstance를 이어서 처리할 때는 {@link #restart(String, String, String)}를 사용하고,
 * 같은 파라미터로 새 JobInstance를 만들 때만 {@link #rerun(String, String, String)}을 사용합니다.
 * 두 동작을 하나의 재시도 버튼으로 합치면 운영자가 처리 의미를 오해할 수 있으므로 분리합니다.</p>
 */
public class BatOnDemandEducationSample {
    private final BatOnDemandService onDemandService;

    public BatOnDemandEducationSample(BatOnDemandService onDemandService) {
        this.onDemandService = onDemandService;
    }

    /** 202 응답으로 돌려줄 접수 상태를 만들고 실제 실행은 worker에 위임합니다. */
    public BatOnDemandStatus submit(BatOnDemandRequest request) {
        return onDemandService.submit(request);
    }

    /** 접수 ID로 현재 CPF 상태와 Spring Batch 실행 ID를 함께 조회합니다. */
    public BatOnDemandStatus status(String executionRequestId) {
        return onDemandService.status(executionRequestId);
    }

    /** JobExplorer가 제공하는 StepExecution read/write/skip 건수를 운영 조회 형태로 반환합니다. */
    public List<Map<String, Object>> steps(String executionRequestId) {
        return onDemandService.steps(executionRequestId);
    }

    /** 실패한 기존 JobInstance를 checkpoint부터 이어서 실행합니다. */
    public BatOnDemandStatus restart(String executionRequestId, String operatorId, String auditReason) {
        return onDemandService.restart(executionRequestId, operatorId, auditReason);
    }

    /** 과거 파라미터를 기준으로 완전히 새로운 실행을 접수합니다. */
    public BatOnDemandStatus rerun(String executionRequestId, String operatorId, String auditReason) {
        return onDemandService.rerun(executionRequestId, operatorId, auditReason);
    }
}
