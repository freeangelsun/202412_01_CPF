package cpf.adm.edu.batch;

import java.util.Map;

/**
 * ADM 배치 관제 화면/API가 다루는 실행 이력 조회 조건 샘플입니다.
 */
public class AdmBatchEducationSample {

    public Map<String, String> executionQuery(String jobName, String businessDate) {
        return Map.of(
                "jobName", jobName,
                "businessDate", businessDate,
                "includeSteps", "true",
                "includeFailureReason", "true");
    }
}
