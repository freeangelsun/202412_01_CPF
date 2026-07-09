package cpf.bat.edu.job;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 단일 업무 단계를 수행하는 Tasklet Job 교육 샘플입니다.
 */
public class BatTaskletJobEducationSample {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    public Map<String, String> buildRunPlan(String jobName, String businessDate) {
        if (jobName == null || jobName.isBlank()) {
            throw new IllegalArgumentException("jobName은 필수입니다.");
        }
        LocalDate.parse(businessDate, DATE_FORMATTER);
        return Map.of(
                "jobName", jobName,
                "businessDate", businessDate,
                "stepType", "TASKLET",
                "idempotencyKey", jobName + ":" + businessDate);
    }
}
