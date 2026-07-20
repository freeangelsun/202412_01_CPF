package cpf.bat.edu.job;

import java.time.LocalDate;
import java.util.Map;

/**
 * 배치 파라미터 검증 기준을 보여주는 샘플입니다.
 */
public class BatJobParameterValidationEducationSample {

    public Map<String, String> validate(Map<String, String> parameters) {
        String businessDate = parameters.get("businessDate");
        String requestedBy = parameters.get("requestedBy");
        if (businessDate == null || businessDate.length() != 8) {
            throw new IllegalArgumentException("businessDate는 yyyyMMdd 형식이어야 합니다.");
        }
        LocalDate.parse(businessDate, java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        if (requestedBy == null || requestedBy.isBlank()) {
            throw new IllegalArgumentException("requestedBy는 필수입니다.");
        }
        return Map.copyOf(parameters);
    }
}
