package cpf.xyz.edu.validation;

import java.util.Set;

/**
 * DTO validation과 enum/status validation 기준 샘플입니다.
 */
public class XyzValidationEducationSample {
    private static final Set<String> STATUSES = Set.of("READY", "ACTIVE", "STOPPED");

    public String validateStatus(String status) {
        if (!STATUSES.contains(status)) {
            throw new IllegalArgumentException("허용되지 않은 상태입니다.");
        }
        return status;
    }
}
