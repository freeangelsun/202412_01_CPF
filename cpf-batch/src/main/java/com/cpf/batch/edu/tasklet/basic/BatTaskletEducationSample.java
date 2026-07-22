package cpf.bat.edu.tasklet.basic;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BAT Tasklet 개발 교육 샘플입니다.
 *
 * <p>운영 Job 구현은 실제 업무 패키지에 두고, 이 클래스는 개발자가 배치 파라미터,
 * 멱등키, 로그 정책을 어떤 기준으로 분리해야 하는지 학습하는 예제로 유지합니다.</p>
 */
public class BatTaskletEducationSample {
    private static final DateTimeFormatter BUSINESS_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    /**
     * 교육용 실행 파라미터를 검증하고 표준 로그/멱등키 예시를 반환합니다.
     */
    public Map<String, String> buildExecutionGuide(String jobName, String businessDate, String requestedBy) {
        if (jobName == null || jobName.isBlank()) {
            throw new IllegalArgumentException("jobName은 필수입니다.");
        }
        if (businessDate == null || businessDate.isBlank()) {
            throw new IllegalArgumentException("businessDate는 필수입니다.");
        }
        if (requestedBy == null || requestedBy.isBlank()) {
            throw new IllegalArgumentException("requestedBy는 필수입니다.");
        }

        LocalDate.parse(businessDate, BUSINESS_DATE_FORMATTER);

        Map<String, String> guide = new LinkedHashMap<>();
        guide.put("jobName", jobName);
        guide.put("businessDate", businessDate);
        guide.put("requestedBy", requestedBy);
        guide.put("idempotencyKey", jobName + ":" + businessDate);
        guide.put("logPolicy", "jobExecutionId 단위로 로그 파일을 분리합니다.");
        return guide;
    }
}
